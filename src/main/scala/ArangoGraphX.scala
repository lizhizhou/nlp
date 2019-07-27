import org.apache.spark.SparkConf
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import com.arangodb.spark.{ArangoSpark, ReadOptions, WriteOptions}
import org.apache.spark.rdd.RDD
import com.arangodb.{ArangoDB, ArangoDBException}
import com.arangodb.entity.EdgeDefinition
import com.arangodb.entity.GraphEntity
import com.arangodb.model.GraphCreateOptions
import org.apache.spark.sql.SparkSession
import collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import com.arangodb.velocypack.module.jdk8.VPackJdk8Module
import com.arangodb.velocypack.module.scala.VPackScalaModule
import scala.util.parsing.json.JSON
import scala.reflect.ClassTag
import org.apache.log4j.Logger
import java.io._

// limit the partitions of data to the number of arango instances to avoid open too many tcp session and file lock

class ArangoGraphX(spark: SparkSession) extends Serializable {
  @transient lazy val log = Logger.getLogger(this.getClass)
  private def hosts(hosts: String): List[(String, Int)] =
    hosts.split(",").map({ x =>
      val s = x.split(":")
      if (s.length != 2 || !s(1).matches("[0-9]+"))
        throw new ArangoDBException(s"Could not load property-value arangodb.hosts=${s}. Expected format ip:port,ip:port,...");
      else
        (s(0), s(1).toInt)
    }).toList

  private def getdb(opt: ArangoGraphX.ArangoOption): ArangoDB =
    {
      val builder = new ArangoDB.Builder()
      builder.registerModules(new VPackJdk8Module, new VPackScalaModule)
      opt.hosts.foreach{ hosts(_).map { case (host, port) => builder.host(host, port) }}
      opt.user.foreach { builder.user(_) }
      opt.password.foreach { builder.password(_) }
      builder.build();
    }
  private def getconf(conf: SparkConf): ArangoGraphX.ArangoOption = {
    val propertyHosts = "spark.arangodb.hosts"
    val propertyUser = "spark.arangodb.user"
    val propertyPassword = "spark.arangodb.password"
    val hosts = Some(conf.get(propertyHosts, null))
    val user = Some(conf.get(propertyUser, null))
    val password = Some(conf.get(propertyPassword, null))
    ArangoGraphX.ArangoOption(hosts, user, password)
  }

  def toGraphX(database: String, vertex: String, edge: String) = {
    val sc = spark.sparkContext

    val edges = ArangoSpark.load[ArangoGraphX.link](sc, edge, ReadOptions(database))

    val vertexs = ArangoSpark.load[ArangoGraphX.point](sc, vertex, ReadOptions(database))

    // Create an RDD for vertex
    val concept: RDD[(VertexId, String)] = vertexs.map(x => (x.concept.hashCode(), x.concept))

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] = edges.map { x => Edge(x._from.split('/')(1).toLong, x._to.split('/')(1).toLong, x.relation) }

    // Define a default user in case there are relationship with missing user
    val defaultconcept = ""
    // Build the initial Graph
    val graph = Graph(concept, relationships, defaultconcept)
    graph
  }

  def toArrango(graph: Graph[String, String], database: String, graphdb: String, vertex: String, edge: String) = {
    val conf = graph.edges.sparkContext.getConf
    val arangoDB = getdb(getconf(conf))
    val db = arangoDB.db(database);

    db.graph(graphdb).drop();
    db.collection(vertex).drop();
    db.collection(edge).drop();

    // Create vertex collection
    db.createCollection(vertex, null);
    // Create edge collection
    val edgeDefinitions = new ArrayBuffer[EdgeDefinition]();
    val edgeDefinition = new EdgeDefinition();

    edgeDefinition.collection(edge);
    edgeDefinition.from(vertex, vertex)
    edgeDefinition.to(vertex, vertex)
    edgeDefinitions += edgeDefinition

    // Create graph
    val options = new GraphCreateOptions();
    db.createGraph(graphdb, edgeDefinitions.asJava, options);

    // Add vertex and edge element 
    val g = db.graph(graphdb)

    ArangoSpark.save[ArangoGraphX.point](graph.vertices.map(x => ArangoGraphX.point(x._2.hashCode.toString, x._2)),vertex, WriteOptions(database));
    ArangoSpark.save[ArangoGraphX.link](graph.triplets.map(x => ArangoGraphX.link(vertex + '/' + x.srcAttr.hashCode.toString, vertex + '/' + x.dstAttr.hashCode.toString, x.attr)),edge, WriteOptions(database));

//    graph.vertices.map(x => ArangoGraphX.point(x._2.hashCode.toString, x._2)).take(20).foreach { println }
//    graph.triplets.map(x => ArangoGraphX.link(x.srcAttr.hashCode().toString, x.dstAttr.hashCode.toString, x.attr)).take(20).foreach { println }
//
//        graph.vertices.foreachPartition(iter => {
//          val arangoDB = getdb(getconf(conf))
//          val db = arangoDB.db(database)
//          val g = db.graph(graphdb)
//          iter.foreach(x => g.vertexCollection(vertex).insertVertex(ArangoGraphX.point(x._2.hashCode.toString, x._2), null))
//        })
//        graph.triplets.foreachPartition(iter => {
//          val arangoDB = getdb(getconf(conf))
//          val db = arangoDB.db(database)
//          val g = db.graph(graphdb)
//          iter.foreach(x => g.edgeCollection(edge).insertEdge(ArangoGraphX.link(vertex + '/' + x.srcAttr.hashCode.toString, vertex + '/' + x.dstAttr.hashCode.toString, x.attr)))
//        })
  }

  // Use arangoimp --file "data.csv" --type csv --collection "users" to import data
  def toArangoImp(graph: Graph[String, String], vertexFile:String, vertexAttr:String, edgeFile: String, edgeAttr: String, vertexName: String): Unit = {
    val vertexOut = new PrintWriter(new BufferedWriter(new FileWriter(vertexFile)));
    val edgeOut = new PrintWriter(new BufferedWriter(new FileWriter(edgeFile)));
    vertexOut.println("_key,"+vertexAttr)
    graph.vertices.map(x => x.productIterator.mkString(",")).collect.foreach(vertexOut.println)
    edgeOut.println("_from,_to,"+ edgeAttr)
    graph.triplets.map(x => (vertexName + '/' + x.srcAttr.hashCode.toString, vertexName + '/' + x.dstAttr.hashCode.toString, x.attr))
      .map(x => x.productIterator.mkString(",")).collect.foreach(edgeOut.println)
  }
}

object ArangoGraphX {
  case class link(_from: String, _to: String, relation: String) {
    def this() = this("", "", "")
  }
  case class point(_key: String, concept: String) {
    def this() = this("", "")
  }
  case class ArangoOption(hosts: Option[String], user: Option[String], password: Option[String]) {
  }

  def apply(spark: SparkSession) = new ArangoGraphX(spark)

  def unitTest(spark: SparkSession)
  {
    val graph = TestKnowledgeGraph(spark)
    val arango = ArangoGraphX(spark)
    val ag = arango.toArrango(graph, "test", "graphdb", "vertex", "edge")
    arango.toGraphX("test", "vertex", "edge")
  }
  //    case class Concept(name: String)
  //    case class Triple(obj: String, rel: String, subject: String)
  //
  //    println("Write link data")

  //    ArangoSpark.save(graph.vertices.map { x => point( x._2)}, "vertex", WriteOptions("test"))
  //    ArangoSpark.save(graph.triplets.map { x => link("vertex/" + x.srcAttr, "vertex/"+x.dstAttr, x.attr) }, "link",WriteOptions("test"))

  //    ArangoSpark.save(sc.makeRDD(Seq(link("persons/alice", "persons/dave", "test"))), "knows", WriteOptions("test"))

  //    import com.arangodb.entity.DocumentField
  //    import com.arangodb.entity.DocumentField.Type

  //      val db = arangoDB.db(database)
  //
  //    println("idMap")
  //
  //    val data = "3"
  //    val id = db.collection("test").insertDocument(ArrangoGraphX.point(data.hashCode.toString,data)).getId
  //
  //    println("id = " + id)
  //
  //    println(JSON.parseFull(db.getDocument(id, classOf[java.lang.String])))
  //    //    match { case map: Map[String, Any] => map.get("Concept").asInstanceOf[String]}  )
  //
  //    println(db.getDocument(id, classOf[ArrangoGraphX.point]))
  //
  //    edges.flatMap { x => Array(x._from, x._to) }.distinct().collect()
  //      .map(x =>
  //        {
  //          println(x)
  //          (x, db.getDocument(x, classOf[ArrangoGraphX.point]).hashCode())
  //        }).foreach(println)
  //
  //    val idMap = edges.flatMap { x => Array(x._from, x._to) }.distinct().collect()
  //      .map(x => (x, db.getDocument(x, classOf[ArrangoGraphX.point]).hashCode())).toMap

}
