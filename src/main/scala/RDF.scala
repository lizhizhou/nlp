import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.graphx.{ Edge, VertexId, Graph }

class RDF (spark: SparkSession) {
  //  val factory = new RdfXmlReaderFactory()
  //HadoopRdfIORegistry.addReaderFactory(factory)
  //val conf = new Configuration()
  //conf.set("rdf.io.input.ignore-bad-tuples", "false")
  //val data = sc.newAPIHadoopFile(path,
  //    classOf[RdfXmlInputFormat],
  //    classOf[LongWritable], //position
  //    classOf[TripleWritable],   //value
  //    conf)
  //data.take(10).foreach(println)

  def toRDF(graph: Graph[String, String]) = {}
}
object RDF {
  def apply(spark: SparkSession) = new RDF(spark)
  def unitTest(spark: SparkSession)
  {
     val baseURI = "http://snee.com/xpropgraph#"
     val sc = spark.sparkContext

    // Create an RDD for the vertices
    val users: RDD[(VertexId, (String, String))] =
      sc.parallelize(Array(
        (3L, ("rxin", "student")),
        (7L, ("jgonzal", "postdoc")),
        (5L, ("franklin", "prof")),
        (2L, ("istoica", "prof")),
        // Following lines are new data
        (8L, ("bshears", "student")),
        (9L, ("nphelge", "student")),
        (10L, ("asmithee", "student")),
        (11L, ("rmutt", "student")),
        (12L, ("ntufnel", "student"))))
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

    // Output object property triples
    graph.triplets.foreach(t => println(
      s"<$baseURI${t.srcAttr._1}> <$baseURI${t.attr}> <$baseURI${t.dstAttr._1}> ."))

    // Output literal property triples
    users.foreach(t => println(
      s"""<$baseURI${t._2._1}> <${baseURI}role> \"${t._2._2}\" ."""))

    sc.stop

  } 
}
