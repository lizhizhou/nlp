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
//import org.apache.spark.ml.feature.Word2Phrase
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
    
    // import com.huaban.analysis.jieba.JiebaSegmenter
    // import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
    // import scala.collection.JavaConversions._
    // def jieba(s:String) =  {
    //     val segmenter = new JiebaSegmenter()
    //     segmenter.process(s, SegMode.SEARCH).toList.map(_.word + " ").foldLeft("")(_+_).toString
    // }

    // val sentences: Array[String] = Array[String]("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", "我不喜欢日本和服。", "雷猴回归人间。", "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", "结果婚的和尚未结过婚的")
    // for (sentence <- sentences) {
    //   println(
    //       jieba(sentence))
    // }

    //Word representation learning//Word representation learning
    //val fastText = FastText.train(new File("train.data"), ModelName.sg)
    // Text classification
    //val fastText = FastText.train(new File("train.data"), ModelName.sup)

    //fastText.saveModel("path/data.model")
    //val fastText = FastText.loadModel("path/data.model", true)

//    val fastText = FastText.loadFasttextBinModel("path/wiki.bin")
//    val predict = fastText.predict(Arrays.asList("fastText在预测标签时使用了非线性激活函数".split(" ")), 5)
//    val wordDataFrame = sqlContext.createDataFrame(Seq(
//      (0, "new york test drive cool york how always learn media new york ."),
//      (1, "online york new york learn to media cool time ."),
//      (2, "media play how cool times play ."),
//      (3, "code to to code york to loaded times media ."),
//      (4, "play awesome to york ."),
//      (1099, "work please ideone how awesome times ."),
//      (1100, "play how play awesome to new york york awesome use new york work please loaded always like ."),
//      (1101, "learn like I media online new york ."),
//      (1102, "media follow learn code code there to york times ."),
//      (1103, "cool use play work please york cool new york how follow ."),
//      (1104, "awesome how loaded media use us cool new york online code judge ideone like ."),
//      (1105, "judge media times time ideone new york new york time us fun ."),
//      (1106, "new york to time there media time fun there new like media time time ."),
//      (1107, "awesome to new times learn cool code play how to work please to learn to ."),
//      (1108, "there work please online new york how to play play judge how always work please ."),
//      (1109, "fun ideone to play loaded like how ."),
//      (1110, "fun york test drive awesome play times ideone new us media like follow .")
//    )).toDF("label", "inputWords")
//
//
//    val t = new Word2Phrase().setInputCol("inputWords").setOutputCol("out")
//
//    val model = t.fit(wordDataFrame)
//
//    val bi_gram_data = model.transform(wordDataFrame)
//
//    bi_gram_data.show()
    TfIdf.unittest(spark)
    CharConvertor.unittest()
    sc.stop()
  }
}
