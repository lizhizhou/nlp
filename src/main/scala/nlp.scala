import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import com.databricks.spark.corenlp.functions._
import org.elasticsearch.spark._
import org.elasticsearch.spark.sql._
import org.elasticsearch.spark.rdd.Metadata._
import com.arangodb.spark.{ArangoSpark, ReadOptions, WriteOptions}
import com.arangodb.ArangoDB
import scala.beans.BeanProperty

case class Concept(name: String)
case class Triple(obj: String, rel: String, subject: String)

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
    triple.show(10)

    // Generate the schema based on the string of schema
    val schema = StructType(Array(StructField("Concept", StringType, nullable = true)))
    val vertex = spark.createDataFrame(triple.select("object").rdd.union(triple.select("subject").rdd).distinct(), schema)

    vertex.show(10)

    // Create an RDD for vertex
    val concept: RDD[(VertexId, String)] = vertex.rdd.map(x => (x.hashCode(), x.getAs("Concept")))

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] = triple.rdd.map { x => Edge(x.getAs("object").hashCode(), x.getAs("subject").hashCode(), x.getAs("relation")) }

    // Define a default user in case there are relationship with missing user
    val defaultconcept = ""
    // Build the initial Graph
    val graph = Graph(concept, relationships, defaultconcept)
//    graph.vertices.collect().take(10).foreach(println)
//    graph.edges.collect().take(10).foreach(println)

    case class link(var _from: String, var _to:String, var relation:String) {
      def this() = this("","","")
    }

    case class point(var Concept: String) {
      def this() = this("")
    }
    println("Write link data")
    ArangoSpark.save(vertex.rdd.map { x => point( x.getAs("Concept"))}, "vertex", WriteOptions("test"))
//    ArangoSpark.save(triple.rdd.map { x => link("vertex/" + x.getAs("object"), "vertex/"+x.getAs("subject"), x.getAs("relation")) }, "link",WriteOptions("test"))
//    val rdd = ArangoSpark.load[link](sc, "link", ReadOptions("test"))
//    println("Read link data")
//    rdd.collect().foreach(println)
    
    //ArangoSpark.saveDF(triple, "edge")
    //ArangoSpark.save(sc.makeRDD(Seq(link("persons/alice", "persons/dave", "test"))), "knows", WriteOptions("test"))

//    import com.arangodb.entity.DocumentField
//    import com.arangodb.entity.DocumentField.Type
    
    
    
    val arangoDB: ArangoDB = new ArangoDB.Builder().user("root").password("lab123").build();
    //arangoDB.createDatabase("test");
    val db = arangoDB.db("test");
    val g = db.graph("myGraph")
    val vid = graph.vertices.collect().map(x => (x._2,g.vertexCollection("concept").insertVertex(point(x._2), null).getId)).toMap;
    triple.collect().map(x =>  g.edgeCollection("link").insertEdge(link(vid(x.getAs("object")),vid(x.getAs("subject")),x.getAs("relation"))))
   
//    triple.saveToEs("spark/vertex")
//    val es = sc.esRDD("spark/vertex")
//    es.take(10).foreach(println)
//
//    val otp = Map("iata" -> "OTP", "name" -> "Otopeni")
//    val muc = Map("iata" -> "MUC", "name" -> "Munich")
//    val sfo = Map("iata" -> "SFO", "name" -> "San Fran")
//
//    // metadata for each document
//    // note it's not required for them to have the same structure
//    val otpMeta = Map(ID -> 1)
//    val mucMeta = Map(ID -> 2)//, VERSION -> "23")
//    val sfoMeta = Map(ID -> 3)
//
//    val airportsRDD = sc.makeRDD(Seq((otpMeta, otp), (mucMeta, muc), (sfoMeta, sfo)))
//    airportsRDD.saveToEsWithMeta("airports/2015")

    sc.stop()
  }
}
