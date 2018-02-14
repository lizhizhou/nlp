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
    val tg = new TripleGraphX(spark)
    tg.toTriple(tg.toGraphX(triple)).show(10)
    //triple.show(10)


//    graph.vertices.collect().take(10).foreach(println)
//    graph.edges.collect().take(10).foreach(println)




    sc.stop()
  }
}
