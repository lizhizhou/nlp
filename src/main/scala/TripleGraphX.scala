import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.graphx.{ Edge, VertexId, Graph }
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

class TripleGraphX(spark: SparkSession) {

  def toGraphX(triple: DataFrame) = {
    val edges = triple.rdd.map { x => (x.getAs[String]("object"), x.getAs[String]("subject"), x.getAs[String]("relation")) }

    val vertex = edges.map(x => x._1).union(edges.map(x => x._2)).distinct()

    // Create an RDD for vertex
    val concept: RDD[(VertexId, String)] = vertex.map(x => (x.hashCode(), x))

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] = edges.map { x => Edge(x._1.hashCode(), x._2.hashCode(), x._3) }

    // Define a default user in case there are relationship with missing user
    val defaultconcept = ""
    // Build the initial Graph
    val graph = Graph(concept, relationships, defaultconcept)
    graph
  }
  def toTriple(graph: Graph[String, String]) = {

    val triple = graph.triplets.map(triplet =>
      Row(triplet.srcAttr, triplet.attr, triplet.dstAttr))
    spark.createDataFrame(triple.distinct(), TripleGraphX.schema)
  }
}

object TripleGraphX {
  val schema = StructType(Array(StructField("object", StringType, nullable = true),
    StructField("relation", StringType, nullable = true),
    StructField("subject", StringType, nullable = true)))
  def apply(spark: SparkSession) = new TripleGraphX(spark)
  def unitTest(spark: SparkSession)
  {
    val sc = spark.sparkContext
    // Create an RDD for the vertices
    val users: RDD[(VertexId, String)] =
      sc.parallelize(Array(
        (3L, "rxin"),
        (7L, "jgonzal"),
        (5L, "franklin"),
        (2L, "istoica"),
        // Following lines are new data
        (8L, "bshears"),
        (9L, "nphelge"),
        (10L, "asmithee"),
        (11L, "rmutt"),
        (12L, "ntufnel")))
    // Create an RDD for edges
    val relationships: RDD[Edge[String]] =
      sc.parallelize(Array(
        Edge(3L, 7L, "collab"),
        Edge(5L, 3L, "advisor"),
        Edge(2L, 5L, "colleague"),
        Edge(5L, 7L, "pi"),
        // Following lines are new data
        Edge(5L, 8L, "advisor"),
        Edge(2L, 9L, "advisor"),
        Edge(5L, 10L, "advisor"),
        Edge(2L, 11L, "advisor")))
    // Build the initial Graph
    val graph = Graph(users, relationships)

    val tg = TripleGraphX(spark)
    val tf = tg.toTriple(graph)
    val gf = tg.toGraphX(tf)

  }
}
