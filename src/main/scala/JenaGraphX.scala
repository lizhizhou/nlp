
import java.io.InputStream
import scala.collection.JavaConverters._
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileManager

import org.apache.jena.query._
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.rdfconnection.RDFConnectionFactory
import org.apache.jena.system.Txn

import java.util.function.Consumer
import org.apache.jena.query.ResultSet
import org.apache.jena.atlas.iterator.Iter

class JenaGraphX()
{
  def toRDF(graph: Graph[String, String], index:String) = {

  }
  def toGraphX(file:String) = {

  }

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
    }

    val subjects = model.listSubjects()
    while (subjects.hasNext()) {
      val r = subjects.nextResource()
      println(r)
    }

    //implicit def funToRunnable(fun: () => Unit) = new Runnable() { def run() = fun() }

    val query = QueryFactory.create("SELECT * {}")
    val dataset = DatasetFactory.createTxnMem()
    val conn = RDFConnectionFactory.connect(dataset)

    Txn.executeWrite(conn, new Runnable() {
      override def run() = {
        println("Load a file")
        conn.load("vc-db-1.rdf")
        println("In write transaction")

        conn.queryResultSet(query, new Consumer[ResultSet] {
          override def accept(rs: ResultSet): Unit = {
            Iter.toList(rs).asScala.foreach(println)
          }
        })
      }
    })
  }
}