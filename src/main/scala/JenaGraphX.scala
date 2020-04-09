
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
      val sub = stmt.getSubject()
      val pred = stmt.getPredicate()
      val obj = stmt.getObject()

      println(sub.toString + "->" + pred.toString + "->" + obj.toString)

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
  }
}