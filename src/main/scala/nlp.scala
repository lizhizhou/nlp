import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.graphx.{ Edge, VertexId, Graph }
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import com.databricks.spark.corenlp.functions._

import com.arangodb.spark.{ ArangoSpark, ReadOptions, WriteOptions }
import com.arangodb.ArangoDB
import scala.beans.BeanProperty

import edu.stanford.nlp.util._
import scala.collection.mutable.WrappedArray
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import com.intel.analytics.bigdl.utils.{Engine, LoggerFilter, T}
import com.softwaremill.debug.DebugMacros._


object NLP {

  def main(args: Array[String]) {
    
    val conf = Engine.createSparkConf()
      .set("spark.arangodb.host", "127.0.0.1")
      .set("spark.arangodb.port", "8529")
      .set("spark.arangodb.user", "root")
      .set("spark.arangodb.password", "lab123")
      .set("es.index.auto.create", "true")
      .set("spark.neo4j.bolt.user", "neo4j")
      .set("spark.neo4j.bolt.password", "lab123")
    val spark = SparkSession.builder.appName("Simple Application").config(conf).getOrCreate()
    val sc = spark.sparkContext
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    
    val userComment = spark.read.parquet("/home/bigdata/temp_spam_user_topic_comment/1",
        "/home/bigdata/temp_spam_user_topic_comment/2")
    val triplet = userComment.select($"nickname", $"content", $"target_user_nick_name")
    triplet.show
 
//    debug()
//    import com.github.johnreedlol.Pos
//    Pos.err("Standard error") 
//    Pos.out("Hello World")
//    return

    //RDF.unitTest(spark); return
    //ElasticsearchGraphX.unitTest(spark)
    //GrokPattern.unitTest()
    
//    val office = Office(spark)
//    val textrdd = office.openWord(Seq("/home/bigdata/test.docx"):_ *)
//    val input = textrdd.map { x => (x.hashCode(),x) }.toDF("id", "text")
//    println(textrdd.foreach { println })
//    val text = "<xml>Stanford University is located in California. It is a great university.</xml>"
//    val input = Seq(
//      (text.hashCode(), text)).toDF("id", "text")
//        input.show()

//    val output = input
//      .select(cleanxml('text).as('doc))
//      .select(explode(ssplit('doc)).as('sen))
//      .select('sen, tokenize('sen).as('words), ner('sen).as('nerTags), coref('sen).as('coref), openie('sen).as('openie), sentiment('sen).as('sentiment))
//    output.show(truncate = false)

//    val triplet =  input.select(cleanxml('text).as('doc))
//      .select(explode(ssplit('doc)).as('sen))
//       .select(openie('sen).as('openie))
//    val tripleRow = triplet.rdd.map(x => (x.getAs[WrappedArray[GenericRowWithSchema]]("openie")))
//    .flatMap { iter => 
//      for (x <- iter) yield  Row(x(0), x(1), x(2))
//    }
    val tripleRow = triplet.sample(false, 0.001).rdd
    println(tripleRow.count)
    //tripleRow.foreach { println }
    val tg = TripleGraphX[String,String](spark, "object", "subject", "relation")
    val triple = spark.createDataFrame(tripleRow.distinct(), tg.getSchema())
  
    //val triple = sqlContext.read.json("/home/bigdata/microeco.json")
    //val triple = sqlContext.read.json("/home/bigdata/chinese.json")
    

    val tf = tg.toTriple(tg.toGraphX(triple))
    tf.show(10)
    //triple.show(10)
    val ag = ArrangoGraphX(spark)
    ag.toArrango(tg.toGraphX(triple), "test", "myGraph", "concept", "link")
    tg.toTriple(ag.toGraphX("test", "concept", "link")).show(10)

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

    sc.stop()
  }
}
