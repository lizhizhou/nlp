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
import com.arangodb.spark.ArangoSpark

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
    graph.vertices.collect().take(10).foreach(println)
    graph.edges.collect().take(10).foreach(println)
    
    // val rdd = ArangoSpark.load[MyBean](sc, "myCollection")
    // ArangoSpark.saveDF(triple, "edge")
    // ArangoSpark.saveDF(vertex, "vertex")

    triple.saveToEs("spark/vertex")
    val es = sc.esRDD("spark/vertex")
    es.take(10).foreach(println)
    
    
    sc.stop()
  }
}
