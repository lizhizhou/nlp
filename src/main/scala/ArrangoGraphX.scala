import org.apache.spark.SparkConf
import org.apache.spark.graphx.{ Edge, VertexId, Graph }
import com.arangodb.spark.{ ArangoSpark, ReadOptions, WriteOptions }
import org.apache.spark.rdd.RDD
import com.arangodb.ArangoDB
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

class ArrangoGraphX(spark: SparkSession) extends Serializable {

  private def getdb(opt: ArrangoGraphX.ArrangoOption): ArangoDB =
    {
      val builder = new ArangoDB.Builder()
      builder.registerModules(new VPackJdk8Module, new VPackScalaModule)
      opt.host.foreach { builder.host(_, opt.port.getOrElse("8529").toInt) }
      opt.user.foreach { builder.user(_) }
      opt.password.foreach { builder.password(_) }
      builder.build();
    }
  private def getconf(conf: SparkConf): ArrangoGraphX.ArrangoOption = {
    val propertyHost = "arangodb.host"
    val propertyPort = "arangodb.port"
    val propertyUser = "arangodb.user"
    val propertyPassword = "arangodb.password"
    val host = Some(conf.get(propertyHost, null))
    val port = Some(conf.get(propertyPort, null))
    val user = Some(conf.get(propertyUser, null))
    val password = Some(conf.get(propertyPassword, null))
    ArrangoGraphX.ArrangoOption(host, port, user, password)
  }

  def toGraphX(database: String, vertex: String, edge: String) = {
    val sc = spark.sparkContext

    val edges = ArangoSpark.load[ArrangoGraphX.link](sc, edge, ReadOptions(database))

    val vertexs = ArangoSpark.load[ArrangoGraphX.point](sc, vertex, ReadOptions(database))

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

    //graph.vertices.map(x => ArrangoGraphX.point(x._2.hashCode.toString, x._2)).take(20).foreach { println }
    //graph.triplets.map(x => ArrangoGraphX.link(x.srcAttr.hashCode().toString, x.dstAttr.hashCode.toString, x.attr)).take(20).foreach { println }

    graph.vertices.foreachPartition(iter => {
      val arangoDB = getdb(getconf(conf))
      val db = arangoDB.db(database)
      val g = db.graph(graphdb)
      iter.foreach(x => g.vertexCollection(vertex).insertVertex(ArrangoGraphX.point(x._2.hashCode.toString, x._2), null))
    })
    graph.triplets.foreachPartition(iter => {
      val arangoDB = getdb(getconf(conf))
      val db = arangoDB.db(database)
      val g = db.graph(graphdb)
      iter.foreach(x => g.edgeCollection(edge).insertEdge(ArrangoGraphX.link(vertex + '/' + x.srcAttr.hashCode.toString, vertex + '/' + x.dstAttr.hashCode.toString, x.attr)))
    })
  }

}

object ArrangoGraphX {
  case class link(_from: String, _to: String, relation: String) {
    def this() = this("", "", "")
  }
  case class point(_key: String, concept: String) {
    def this() = this("", "")
  }
  case class ArrangoOption(host: Option[String], port: Option[String], user: Option[String], password: Option[String]) {
  }

  def apply(spark: SparkSession) = new ArrangoGraphX(spark)

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
