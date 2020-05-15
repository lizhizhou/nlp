import java.io.{FileOutputStream, OutputStream, InputStream}

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileManager
import org.apache.jena.query._
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.rdfconnection.RDFConnectionFactory
import org.apache.jena.system.Txn
import org.apache.jena.vocabulary.VCARD
import java.util.function.Consumer

import org.apache.jena.query.ResultSet
import org.apache.jena.atlas.iterator.Iter

class JenaGraphX(spark: SparkSession)
{
  private def readRDF(file:String) =
  {
    val model: Model = ModelFactory.createDefaultModel
    val in: InputStream = FileManager.get.open(file)
    if (in == null) throw new IllegalArgumentException("File: " + file + " not found")
    // read the RDF/XML file
    model.read(in, null)
    // write it to standard out
    model.write(System.out)
   // writeRDF(model, "test.rdf")
    model
  }

  def writeRDF(model:Model, file:String): Unit =
  {

    val model: Model = ModelFactory.createDefaultModel
    val out: OutputStream  =  new FileOutputStream(file)
    model.write(out)
    out.close()
  }

  def toRDF(graph: Graph[String, String], index:String) = {
    TripleGraphX[String,String](spark, "object", "subject", "relation").toTriple(graph)
  }

  def toGraphX(file:String) = {
    val sc = spark.sparkContext

    val model = readRDF(file)
    val subjects = model.listSubjects()
    while (subjects.hasNext()) {
      val r = subjects.nextResource()
      println(r)
    }

    var tripleRow = new ListBuffer[Row]()
    val statements = model.listStatements()
    while (statements.hasNext()) {
      val stmt = statements.nextStatement()
      val sub = stmt.getSubject()
      val pred = stmt.getPredicate()
      val obj = stmt.getObject()

      println(sub.toString + "->" + pred.toString + "->" + obj.toString)
      tripleRow += Row(sub.toString, pred.toString,obj.toString)
    }

    val tg = TripleGraphX[String,String](spark, "object", "subject", "relation")
    val tripleDF =  spark.createDataFrame(spark.sparkContext.parallelize(tripleRow), tg.getSchema())
    tg.toGraphX(tripleDF)
  }

}

object JenaGraphX {
  def apply(spark: SparkSession) = new JenaGraphX(spark)

  def sparQL(file:String, ql:String) = {
    val query = QueryFactory.create(ql)
    val dataSet = DatasetFactory.createTxnMem()
    val conn = RDFConnectionFactory.connect(dataSet)

    Txn.executeWrite(conn, new Runnable() {
      override def run() = {
        println("Load a file")
        conn.load(file)
        println("In write transaction")

        conn.queryResultSet(query, new Consumer[ResultSet] {
          override def accept(rs: ResultSet): Unit = {
            Iter.toList(rs).asScala.foreach(println)
          }
        })
      }
    })
  }

  def unitTest(spark: SparkSession) {
    // use the FileManager to find the input file
    val inputFileName = "vc-db-1.rdf"
    val jena = JenaGraphX(spark)
    val graph = jena.toGraphX(inputFileName)
    graph.triplets.map(
      triplet => triplet.srcAttr + " " + triplet.attr + " " + triplet.dstAttr
    ).collect.foreach(println(_))

    //implicit def funToRunnable(fun: () => Unit) = new Runnable() { def run() = fun() }
    sparQL("vc-db-1.rdf","SELECT * {}")

    val personURI = "http://somewhere/JohnSmith"
    val fullName = "John Smith"

    // create an empty Model
    val model = ModelFactory.createDefaultModel

    // create the resource
    val johnSmith = model.createResource(personURI)

    // add the property
    johnSmith.addProperty(VCARD.FN, fullName)
    jena.writeRDF(model,"test.rdf")

  }
}