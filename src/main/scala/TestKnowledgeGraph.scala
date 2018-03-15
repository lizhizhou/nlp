import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.graphx.{ Edge, VertexId, Graph }
object TestKnowledgeGraph {
  def apply(spark: SparkSession) = {
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
    Graph(users, relationships)
  }
}