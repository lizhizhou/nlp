import java.nio.ByteOrder
import org.apache.spark.sql.SparkSession
import com.intel.analytics.bigdl.nn._
import com.intel.analytics.bigdl.numeric.NumericFloat
import com.intel.analytics.bigdl.example.utils._
import com.intel.analytics.bigdl.nn.{ClassNLLCriterion, _}
import com.intel.analytics.bigdl.utils.{Engine, LoggerFilter, T}
import org.apache.log4j.{Level => Levle4j, Logger => Logger4j}
import org.slf4j.{Logger, LoggerFactory}
import scopt.OptionParser

import scala.collection.mutable.{ArrayBuffer, Map => MMap}
import scala.language.existentials

import java.io.File
import java.util

import com.intel.analytics.bigdl._
import com.intel.analytics.bigdl.dataset._
import com.intel.analytics.bigdl.nn.{ClassNLLCriterion, _}
import com.intel.analytics.bigdl.optim._
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.utils.{Engine, LoggerFilter, T}
import com.intel.analytics.bigdl.example.utils.SimpleTokenizer._
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.{ArrayBuffer, Map => MMap}
import scala.language.existentials
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
 * This example use a (pre-trained GloVe embedding) to convert word to vector,
 * and uses it to train a text classification model on the 20 Newsgroup dataset
 * with 20 different categories. This model can achieve around 90% accuracy after
 * 2 epochs training.
 */
class TextClassifier() extends Serializable{
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  val gloveDir = s"/home/bigdata/Downloads/glove.6B/"
  val textDataDir = s"/home/bigdata/Downloads/20news-18828/"
  val maxWordsNum = 5000
  val partitionNum = 4
  val embeddingDim = 200
  val maxSequenceLength = 500
  val trainingSplit = 0.8
  val batchSize = 128
  val learningRate = 0.01
  var classNum = -1

  /**
   * Load the pre-trained word2Vec
   * @return A map from word to vector
   */
  def buildWord2Vec(word2Meta: Map[String, WordMeta]): Map[Float, Array[Float]] = {
    log.info("Indexing word vectors.")
    val preWord2Vec = MMap[Float, Array[Float]]()
    val filename = s"$gloveDir/glove.6B.200d.txt"
    for (line <- Source.fromFile(filename, "ISO-8859-1").getLines) {
      val values = line.split(" ")
      val word = values(0)
      if (word2Meta.contains(word)) {
        val coefs = values.slice(1, values.length).map(_.toFloat)
        preWord2Vec.put(word2Meta(word).index.toFloat, coefs)
      }
    }
    log.info(s"Found ${preWord2Vec.size} word vectors.")
    preWord2Vec.toMap
  }

  /**
   * Load the pre-trained word2Vec
   * @return A map from word to vector
   */
  def buildWord2VecWithIndex(word2Meta: Map[String, Int]): Map[Float, Array[Float]] = {
    log.info("Indexing word vectors.")
    val preWord2Vec = MMap[Float, Array[Float]]()
    val filename = s"$gloveDir/glove.6B.200d.txt"
    for (line <- Source.fromFile(filename, "ISO-8859-1").getLines) {
      val values = line.split(" ")
      val word = values(0)
      if (word2Meta.contains(word)) {
        val coefs = values.slice(1, values.length).map(_.toFloat)
        preWord2Vec.put(word2Meta(word).toFloat, coefs)
      }
    }
    log.info(s"Found ${preWord2Vec.size} word vectors.")
    preWord2Vec.toMap
  }


  /**
   * Load the training data from the given baseDir
   * @return An array of sample
   */
  private def loadRawData(): ArrayBuffer[(String, Float)] = {
    val texts = ArrayBuffer[String]()
    val labels = ArrayBuffer[Float]()
    // category is a string name, label is it's index
    val categoryToLabel = new util.HashMap[String, Int]()
    val categoryPathList = new File(textDataDir).listFiles().filter(_.isDirectory).toList.sorted

    categoryPathList.foreach { categoryPath =>
      val label_id = categoryToLabel.size() + 1 // one-base index
      categoryToLabel.put(categoryPath.getName(), label_id)
      val textFiles = categoryPath.listFiles()
        .filter(_.isFile).filter(_.getName.forall(Character.isDigit(_))).sorted
      textFiles.foreach { file =>
        val source = Source.fromFile(file, "ISO-8859-1")
        val text = try source.getLines().toList.mkString("\n") finally source.close()
        texts.append(text)
        labels.append(label_id)
      }
    }
    this.classNum = labels.toSet.size
    log.info(s"Found ${texts.length} texts.")
    log.info(s"Found ${classNum} classes")
    texts.zip(labels)
  }

  /**
   * Go through the whole data set to gather some meta info for the tokens.
   * Tokens would be discarded if the frequency ranking is less then maxWordsNum
   */
  def analyzeTexts(dataRdd: RDD[(String, Float)])
  : (Map[String, WordMeta], Map[Float, Array[Float]]) = {
    // Remove the top 10 words roughly, you might want to fine tuning this.
    val frequencies = dataRdd.flatMap{case (text: String, label: Float) =>
      SimpleTokenizer.toTokens(text)
    }.map(word => (word, 1)).reduceByKey(_ + _)
      .sortBy(- _._2).collect().slice(10, maxWordsNum)

    val indexes = Range(1, frequencies.length)
    val word2Meta = frequencies.zip(indexes).map{item =>
      (item._1._1, WordMeta(item._1._2, item._2))}.toMap
    (word2Meta, buildWord2Vec(word2Meta))
  }

  /**
   * Create train and val RDDs from input
   */
  def getData(sc: SparkContext): (Array[RDD[(Array[Array[Float]], Float)]],
    Map[String, WordMeta],
    Map[Float, Array[Float]]) = {

    val sequenceLen = maxSequenceLength

    // For large dataset, you might want to get such RDD[(String, Float)] from HDFS
    val dataRdd = sc.parallelize(loadRawData(), partitionNum)
    val (word2Meta, word2Vec) = analyzeTexts(dataRdd)
    val word2MetaBC = sc.broadcast(word2Meta)
    val word2VecBC = sc.broadcast(word2Vec)
    val vectorizedRdd = dataRdd
      .map { case (text, label) => (toTokens(text, word2MetaBC.value), label) }
      .map { case (tokens, label) => (shaping(tokens, sequenceLen), label) }
      .map { case (tokens, label) => (vectorization(
        tokens, embeddingDim, word2VecBC.value), label)
      }

    (vectorizedRdd.randomSplit(
      Array(trainingSplit, 1 - trainingSplit)), word2Meta, word2Vec)

  }

  // TODO: Replace SpatialConv and SpatialMaxPolling with 1D implementation
  /**
   * Return a text classification model with the specific num of
   * class
   */
  def buildModel(classNum: Int): Sequential[Float] = {
    val model = Sequential[Float]()

    model.add(TemporalConvolution(embeddingDim, 256, 5))
      .add(ReLU())
      .add(TemporalMaxPooling(maxSequenceLength - 5 + 1))
      .add(Squeeze(2))
      .add(Linear(256, 128))
      .add(Dropout(0.2))
      .add(ReLU())
      .add(Linear(128, classNum))
      .add(LogSoftMax())
    model
  }


  /**
   * Start to train the text classification model
   */
  def train(): Unit = {
    val conf = Engine.createSparkConf()
      .setAppName("Text classification")
      .set("spark.task.maxFailures", "1")
    val sc = new SparkContext(conf)
    Engine.init
    val sequenceLen = maxSequenceLength

    // For large dataset, you might want to get such RDD[(String, Float)] from HDFS
    val dataRdd = sc.parallelize(loadRawData(), partitionNum)
    val (word2Meta, word2Vec) = analyzeTexts(dataRdd)
    val word2MetaBC = sc.broadcast(word2Meta)
    val word2VecBC = sc.broadcast(word2Vec)
    val vectorizedRdd = dataRdd
      .map {case (text, label) => (toTokens(text, word2MetaBC.value), label)}
      .map {case (tokens, label) => (shaping(tokens, sequenceLen), label)}
      .map {case (tokens, label) => (vectorization(
        tokens, embeddingDim, word2VecBC.value), label)}
    val sampleRDD = vectorizedRdd.map {case (input: Array[Array[Float]], label: Float) =>
      Sample(
        featureTensor = Tensor(input.flatten, Array(sequenceLen, embeddingDim)),
        label = label)
    }

    val Array(trainingRDD, valRDD) = sampleRDD.randomSplit(
      Array(trainingSplit, 1 - trainingSplit))

    val optimizer = Optimizer(
      model = buildModel(classNum),
      sampleRDD = trainingRDD,
      criterion = new ClassNLLCriterion[Float](),
      batchSize = batchSize
    )

    optimizer
      .setOptimMethod(new Adagrad(learningRate = learningRate,
        learningRateDecay = 0.001))
      .setValidation(Trigger.everyEpoch, valRDD, Array(new Top1Accuracy[Float]), batchSize)
      .setEndWhen(Trigger.maxEpoch(20))
      .optimize()
    sc.stop()
  }

  /**
   * Train the text classification model with train and val RDDs
   */
  def trainFromData(sc: SparkContext, rdds: Array[RDD[(Array[Array[Float]], Float)]])
  : Module[Float] = {

    // create rdd from input directory
    val trainingRDD = rdds(0).map { case (input: Array[Array[Float]], label: Float) =>
      Sample(
        featureTensor = Tensor(input.flatten, Array(maxSequenceLength, embeddingDim))
          .transpose(1, 2).contiguous(),
        label = label)
    }

    val valRDD = rdds(1).map { case (input: Array[Array[Float]], label: Float) =>
      Sample(
        featureTensor = Tensor(input.flatten, Array(maxSequenceLength, embeddingDim))
          .transpose(1, 2).contiguous(),
        label = label)
    }

    // train
    val optimizer = Optimizer(
      model = buildModel(classNum),
      sampleRDD = trainingRDD,
      criterion = new ClassNLLCriterion[Float](),
      batchSize = batchSize
    )

    optimizer
      .setOptimMethod(new Adagrad(learningRate = learningRate, learningRateDecay = 0.0002))
      .setValidation(Trigger.everyEpoch, valRDD, Array(new Top1Accuracy[Float]), batchSize)
      .setEndWhen(Trigger.maxEpoch(1))
      .optimize()
  }
}


abstract class AbstractTextClassificationParams extends Serializable {
  def baseDir: String = "./"
  def maxSequenceLength: Int = 1000
  def maxWordsNum: Int = 20000
  def trainingSplit: Double = 0.8
  def batchSize: Int = 128
  def embeddingDim: Int = 100
  def partitionNum: Int = 4
  def learningRate: Double = 0.01
}


/**
 * @param baseDir The root directory which containing the training and embedding data
 * @param maxSequenceLength number of the tokens
 * @param maxWordsNum maximum word to be included
 * @param trainingSplit percentage of the training data
 * @param batchSize size of the mini-batch
 * @param learningRate learning rate
 * @param embeddingDim size of the embedding vector
 */
case class TextClassificationParams(override val baseDir: String = "./",
  override val maxSequenceLength: Int = 500,
  override val maxWordsNum: Int = 5000,
  override val trainingSplit: Double = 0.8,
  override val batchSize: Int = 128,
  override val embeddingDim: Int = 200,
  override val learningRate: Double = 0.01,
  override val partitionNum: Int = 4)
  extends AbstractTextClassificationParams

class bigdl {
  val textClassification = new TextClassifier()
  def infer(rdd:RDD[String]):RDD[Integer] = {
      return rdd.map( x => x.hashCode())
  }
  def train()
  {
    val textClassification = new TextClassifier()
    textClassification.train()
  }
  def load()
  {
    
  }
}
  
object bigdl {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  LoggerFilter.redirectSparkInfoLogs()
  Logger4j.getLogger("com.intel.analytics.bigdl.optim").setLevel(Levle4j.INFO)

  def unittest() = {

    val textClassification = new TextClassifier()
    textClassification.train()
    
  }

  def main(args: Array[String]): Unit = {
    val conf = Engine.createSparkConf()
      .setAppName("Text classification")
      .set("spark.task.maxFailures", "1")
    val sc = new SparkContext(conf)
    Engine.init
    val modelPath = "/tmp/model/model.pb"
    val binPath = "/tmp/model/model.bin"
    val inputs = Seq("Placeholder")
    val outputs = Seq("output")
    val model = Module.loadTF(modelPath, Seq("Placeholder"),
      Seq("output"), ByteOrder.LITTLE_ENDIAN, Some(binPath))
    val image = Tensor(1).fill(0.1f)
    val label = 1f
    val sample = Sample(image, label)
    val sampledata = sc.parallelize(Seq(sample))
      model.predict(sampledata)

  }
}