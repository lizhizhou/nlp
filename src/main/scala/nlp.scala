import java.io.File

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import com.databricks.spark.corenlp.functions._
import com.arangodb.spark.{ArangoSpark, ReadOptions, WriteOptions}
import com.arangodb.ArangoDB

import scala.beans.BeanProperty
import scala.collection.mutable.WrappedArray
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import com.intel.analytics.bigdl.utils.{Engine, LoggerFilter, T}
import com.navercorp.Node2vec
import com.softwaremill.debug.DebugMacros._
import scala.util.Try
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.feature.Word2Phrase
//import com.mayabot.mynlp.fasttext._

object NLP {

  def main(args: Array[String]) {
    System.setProperty("corenlp.props", "StanfordCoreNLP-chinese.properties")
    //System.setProperty("corenlp.props", "StanfordCoreNLP.properties")
    val conf = Engine.createSparkConf()
      .set("spark.arangodb.hosts", "127.0.0.1:8529")
      .set("spark.arangodb.user", "root")
      .set("spark.arangodb.password", "lab123")
      .set("es.index.auto.create", "true")
      .set("spark.neo4j.bolt.user", "neo4j")
      .set("spark.neo4j.bolt.password", "lab123")
    val spark = SparkSession.builder.appName("Simple Application").config(conf).getOrCreate()
    val sc = spark.sparkContext
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    def hasColumn(df: DataFrame, path: String) = Try(df(path)).isSuccess
    //val userComment = spark.read.parquet("/data/user_follow/")
//    val userComment = spark.read.parquet("/tmp/user_follow")
//    val triplet = userComment.select($"user_id", $"follow_source", $"target_user_id")
//    triplet.show
 
//    debug()
//    import com.github.johnreedlol.Pos
//    Pos.err("Standard error") 
//    Pos.out("Hello World")
//    return

    //RDF.unitTest(spark); return
    //ElasticsearchGraphX.unitTest(spark)
    //GrokPattern.unitTest()
    //ArrangoGraphX.unitTest(spark)
    
//    val office = Office(spark)
//    val textrdd = office.openWord(Seq("/home/bigdata/test.docx"):_ *)
//    val input = textrdd.map { x => (x.hashCode(),x) }.toDF("id", "text")
//    println(textrdd.foreach { println })
    //val text = "<xml>Stanford University is located in California. It is a great university.</xml>"
//    val text = "<xml>克林顿说，华盛顿将逐步落实对韩国的经济援助。</xml>"
//    val input = Seq(
//      (text.hashCode(), text)).toDF("id", "text")
//        input.show()
//
//    val output = input
//      .select(cleanxml('text).as('doc))
//      .select(explode(ssplit('doc)).as('sen))
//      .select('sen, tokenize('sen).as('words), ner('sen).as('nerTags))
//    output.show(truncate = false)

//    val triplet =  input.select(cleanxml('text).as('doc))
//      .select(explode(ssplit('doc)).as('sen))
//       .select(openie('sen).as('openie))
//    val tripleRow = triplet.rdd.map(x => (x.getAs[WrappedArray[GenericRowWithSchema]]("openie")))
//    .flatMap { iter => 
//      for (x <- iter) yield  Row(x(0), x(1), x(2))
//    }
//    val tripleRow = triplet.rdd
//    println(tripleRow.count)
//    //tripleRow.foreach { println }
//    val tg = TripleGraphX[String,String](spark, "object", "subject", "relation")
//    val triple = spark.createDataFrame(tripleRow.distinct(), tg.getSchema())
  
    //val triple = sqlContext.read.json("/home/bigdata/microeco.json")
    //val triple = sqlContext.read.json("/home/bigdata/chinese.json")
    

//    val tf = tg.toTriple(tg.toGraphX(triple))
//    tf.show(10)
//    triple.show(10)
//    val ag = ArrangoGraphX(spark)
//    ag.toArrango(tg.toGraphX(triple), "test", "node2vec", "concept", "link")
//    tg.toTriple(ag.toGraphX("test", "concept", "link")).show(10)

//    Node2vec.setParams(sc).loadFromGraph(tg.toGraphX(triple))
//      .initTransitionProb()
//      .randomWalk()
//        //.saveRandomPath("path")
//      .embedding()
//        .save("model")

//    val ng = Neo4jGraphX(spark)
//    ng.toNeo4j(tg.toGraphX(triple))
//    val neo = ng.toGraphX()
//    println("Count of edge " + neo.edges.count)
     
    //val sqlContext = new SQLContext(sc)
    //val df = sqlContext.read
    //    .format("com.crealytics.spark.excel")
    //    .option("sheetName", "Daily") // Required
    //    .option("useHeader", "true") // Required
    //    .option("treatEmptyValuesAsNulls", "false") // Optional, default: true
    //    .option("inferSchema", "false") // Optional, default: false
    //    .option("addColorColumns", "true") // Optional, default: false
    //    .option("startColumn", 0) // Optional, default: 0
    //    .option("endColumn", 99) // Optional, default: Int.MaxValue
    //    .option("timestampFormat", "MM-dd-yyyy HH:mm:ss") // Optional, default: yyyy-mm-dd hh:mm:ss[.fffffffff]
    //    .option("maxRowsInMemory", 20) // Optional, default None. If set, uses a streaming reader which can help with big files
    //    .option("excerptSize", 10) // Optional, default: 10. If set and if schema inferred, number of rows to infer schema from
    //    .schema(myCustomSchema) // Optional, default: Either inferred schema, or all columns are Strings
    //    .load("Worktime.xlsx")
    //
//    tf.write
//      .format("com.crealytics.spark.excel")
//      .option("sheetName", "Daily")
//      .option("useHeader", "true")
//      .option("dateFormat", "yy-mmm-d") // Optional, default: yy-m-d h:mm
//      .option("timestampFormat", "mm-dd-yyyy hh:mm:ss") // Optional, default: yyyy-mm-dd hh:mm:ss.000
//      .mode("overwrite")
//      .save("/home/bigdata/triple.xlsx")
    //
    //import org.apache.spark.sql.SQLContext
    //import com.databricks.spark.xml._
    //
    //val sqlContext = new SQLContext(sc)
    //val df = sqlContext.read
    //  .option("rowTag", "book")
    //  .xml("books.xml")
    //
    //val selectedData = df.select("author", "_id")
    //selectedData.write
    //  .option("rootTag", "books")
    //  .option("rowTag", "book")
    //  .xml("newbooks.xml")
    
     import com.huaban.analysis.jieba.JiebaSegmenter
     import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
     import scala.collection.JavaConversions._
     import org.apache.spark.sql.functions.{col, udf}
     def jieba(s:String) =  {
         val segmenter = new JiebaSegmenter()
         segmenter.process(s, SegMode.SEARCH).toList.map(_.word + " ").foldLeft("")(_+_).toString
     }

     val sentences: Array[String] = Array[String]("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", "我不喜欢日本和服。", "雷猴回归人间。", "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", "结果婚的和尚未结过婚的")
     for (sentence <- sentences) {
       println(
           jieba(sentence))
     }
     val jieba_udf = udf((s: String) => jieba(s))
    val note = Seq(
      (0,"未办登记只举办结婚仪式可起诉离婚吗"),
      (1,"未办登记只举办结婚仪式可起诉离婚吗"),
      (2,"无偿居间介绍买卖毒品的行为应如何定性"),
      (3,"吸毒男动态持有大量毒品的行为该如何认定"),
      (4,"如何区分是非法种植毒品原植物罪还是非法制造毒品罪"),
      (5,"为毒贩贩卖毒品提供帮助构成贩卖毒品罪"),
      (6,"将自己吸食的毒品原价转让给朋友吸食的行为该如何认定"),
      (7,"为获报酬帮人购买毒品的行为该如何认定"),
      (8,"毒贩出狱后再次够买毒品途中被抓的行为认定"),
      (9,"虚夸毒品功效劝人吸食毒品的行为该如何认定"),
      (10,"妻子下落不明丈夫又与他人登记结婚是否为无效婚姻"),
      (11,"一方未签字办理的结婚登记是否有效"),
      (12,"夫妻双方1990年按农村习俗举办婚礼没有结婚证 一方可否起诉离婚"),
      (13,"结婚前对方父母出资购买的住房写我们二人的名字有效吗"),
      (14,"身份证被别人冒用无法登记结婚怎么办？"),
      (15,"同居后又与他人登记结婚是否构成重婚罪"),
      (16,"同居多年未办理结婚登记，是否可以向法院起诉要求离婚")
    ).toDF("discovery_id","content")
    import org.apache.spark.ml.feature._
    import org.apache.spark.ml.linalg._
    import org.apache.spark.sql.types._
    val dfUsed = note.select($"discovery_id", $"content", jieba_udf($"content").as("words"))
    // Tokenize the wiki content
    val tokenizer = new Tokenizer().setInputCol("words").setOutputCol("word")
    val wordsDf = tokenizer.transform(dfUsed)

    // Word count to vector for each wiki content
    val vocabSize = 100
    val cvModel: CountVectorizerModel = new CountVectorizer().setInputCol("word").setOutputCol("features").setVocabSize(vocabSize).setMinDF(5).fit(wordsDf)
    val isNoneZeroVector = udf({v: Vector => v.numNonzeros > 0}, DataTypes.BooleanType)
    val vectorizedDf = cvModel.transform(wordsDf).filter(isNoneZeroVector(col("features")))

    val mh = new MinHashLSH().setNumHashTables(10).setInputCol("features").setOutputCol("hashValues")
    val model = mh.fit(vectorizedDf)
    val transform = model.transform(vectorizedDf)

    val threshold = 0.5
    val result = model.approxSimilarityJoin(vectorizedDf, vectorizedDf, threshold)
    result.show()

    val key = Vectors.sparse(vocabSize, Seq((cvModel.vocabulary.indexOf("结婚"), 1.0)))
    val k = 40
    model.approxNearestNeighbors(vectorizedDf, key, k).show()
    //Word representation learning//Word representation learning
    //val fastText = FastText.train(new File("train.data"), ModelName.sg)
    // Text classification
    //val fastText = FastText.train(new File("train.data"), ModelName.sup)

    //fastText.saveModel("path/data.model")
    //val fastText = FastText.loadModel("path/data.model", true)

//    val fastText = FastText.loadFasttextBinModel("path/wiki.bin")
//    val predict = fastText.predict(Arrays.asList("fastText在预测标签时使用了非线性激活函数".split(" ")), 5)
    //TestTextData.unittest(spark)
    //TfIdf.unittest(spark)
    // CharConvertor.unittest()


    sc.stop()
  }
}
