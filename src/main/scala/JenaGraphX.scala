
import java.io.InputStream

import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileManager

class JenaGraphX()
{


}

object JenaGraphX {
  def unitTest(spark: SparkSession) {
    val model: Model = ModelFactory.createDefaultModel
    // use the FileManager to find the input file
    val inputFileName = "vc-db-1.rdf"
    val in: InputStream = FileManager.get.open(inputFileName)
    if (in == null) throw new IllegalArgumentException("File: " + inputFileName + " not found")

    // read the RDF/XML file
    model.read(in, null)
    // write it to standard out
    model.write(System.out)

    val statements = model.listStatements()
    while (statements.hasNext()) {
      val stmt = statements.nextStatement()
      println(stmt.getString)
//      val obj = stmt.getObject()
//      val sub = stmt.getSubject()
//      if (obj != null && sub != null)
//        println(sub + "->" + stmt.getString + "->" + obj)

      //      import java.sql.SQLException
      //      try { // Make a query
      //        val rset = stmt.executeQuery("SELECT DISTINCT ?type WHERE { ?s a ?type } LIMIT 100")
      //        // Iterate over results
      //        while ( {
      //          rset.next
      //        }) { // Print out type as a string
      //          println(rset.getString("type"))
      //        }
      //        // Clean up
      //        rset.close
      //      } catch {
      //        case e: SQLException =>
      //          System.err.println("SQL Error - " + e.getMessage)
      //      } finally stmt.close

    }

    val subjects = model.listSubjects()
    while (subjects.hasNext()) {
      val r = subjects.nextResource()
      println(r)
    }

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
        (12L, ("ntufnel", "student"))
      ))
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
        Edge(2L, 11L, "advisor")
      ))
    // Build the initial Graph
    val graph = Graph(users, relationships)

    // Output object property triples
    graph.triplets.foreach( t => println(
      s"<$baseURI${t.srcAttr._1}> <$baseURI${t.attr}> <$baseURI${t.dstAttr._1}> ."
    ))

    // Output literal property triples
    users.foreach(t => println(
      s"""<$baseURI${t._2._1}> <${baseURI}role> \"${t._2._2}\" ."""
    ))

  }
}