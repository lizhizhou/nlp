import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.graphx.{Edge,VertexId,Graph}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import com.databricks.spark.corenlp.functions._

import com.arangodb.spark.{ArangoSpark, ReadOptions, WriteOptions}
import com.arangodb.ArangoDB
import scala.beans.BeanProperty

object NLP {
 
  
  def main(args: Array[String]) {

    val conf = new SparkConf()
      .set("arangodb.host", "127.0.0.1")
      .set("arangodb.port", "8529")
      .set("arangodb.user", "root")
      .set("arangodb.password", "lab123")
      .set("es.index.auto.create", "true")
    val spark = SparkSession.builder.appName("Simple Application").config(conf).getOrCreate()
    val sc = spark.sparkContext
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val input = Seq(
      (1, "<xml>Stanford University is located in California. It is a great university.</xml>")).toDF("id", "text")

    //    val output = input
    //      .select(cleanxml('text).as('doc))
    //      .select(explode(ssplit('doc)).as('sen))
    //      .select('sen, tokenize('sen).as('words), ner('sen).as('nerTags), coref('sen).as('coref), openie('sen).as('openie), sentiment('sen).as('sentiment))
    //    output.show(truncate = false)

    val triple = sqlContext.read.json("/home/bigdata/microeco.json")
    val tg = TripleGraphX(spark)
    tg.toTriple(tg.toGraphX(triple)).show(10)
    //triple.show(10)
    val ag = ArrangoGraphX(spark)
    ag.toArrango(tg.toGraphX(triple), "test", "myGraph", "concept", "link")
    tg.toTriple(ag.toGraphX("test", "concept", "link")).show(10)
//    graph.vertices.collect().take(10).foreach(println)
//    graph.edges.collect().take(10).foreach(println)

//import org.apache.spark.sql.SQLContext

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
//df.write
//  .format("com.crealytics.spark.excel")
//  .option("sheetName", "Daily")
//  .option("useHeader", "true")
//  .option("dateFormat", "yy-mmm-d") // Optional, default: yy-m-d h:mm
//  .option("timestampFormat", "mm-dd-yyyy hh:mm:ss") // Optional, default: yyyy-mm-dd hh:mm:ss.000
//  .mode("overwrite")
//  .save("Worktime2.xlsx")
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
