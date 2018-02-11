import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.graphx.{ Edge, VertexId, Graph }
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

class TripleGraphX(spark: SparkSession) {
  def toGraphX(triple: DataFrame) = {
    // Generate the schema based on the string of schema
    val schema = StructType(Array(StructField("Concept", StringType, nullable = true)))
    val vertex = spark.createDataFrame(triple.select("object").rdd.union(triple.select("subject").rdd).distinct(), schema)

    // Create an RDD for vertex
    val concept: RDD[(VertexId, String)] = vertex.rdd.map(x => (x.hashCode(), x.getAs("Concept")))

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] = triple.rdd.map { x => Edge(x.getAs("object").hashCode(), x.getAs("subject").hashCode(), x.getAs("relation")) }

    // Define a default user in case there are relationship with missing user
    val defaultconcept = ""
    // Build the initial Graph
    val graph = Graph(concept, relationships, defaultconcept)
    graph
  }
  def toTriple(graph: Graph[String, String]) =  {
    graph.edges
  }
}