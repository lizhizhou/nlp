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

class ArrangoGraphX(spark: SparkSession) {
  case class link(_from: String, _to: String, relation: String) {
    def this() = this("", "", "")
  }
  case class point(Concept: String) {
    def this() = this("")
  }

  val sc = spark.sparkContext
  val conf = sc.getConf
  val propertyHost = "arangodb.host"
  val propertyPort = "arangodb.port"
  val propertyUser = "arangodb.user"
  val propertyPassword = "arangodb.password"
  val host = Some(conf.get(propertyHost, null))
  val port = Some(conf.get(propertyPort, null))
  val user = Some(conf.get(propertyUser, null))
  val password = Some(conf.get(propertyPassword, null))

  val builder = new ArangoDB.Builder()
  builder.registerModules(new VPackJdk8Module, new VPackScalaModule)
  host.foreach { builder.host(_, port.getOrElse("8529").toInt) }
  user.foreach { builder.user(_) }
  password.foreach { builder.password(_) }
  val arangoDB: ArangoDB = builder.build();

  def toGraphX(database: String, vertex: String, edge: String) = {
    val edges = ArangoSpark.load[link](sc, edge, ReadOptions(database))

    val vertexs = ArangoSpark.load[point](sc, vertex, ReadOptions(database))

    // Create an RDD for vertex
    val concept: RDD[(VertexId, String)] = vertexs.map(x => (x.Concept.hashCode(), x.Concept))

    val db = arangoDB.db(database)

    println("idMap")
    
    val id = db.collection("test").insertDocument(point("3")).getId 
    
    println("id = " + id)
         
    println(JSON.parseFull(db.getDocument(id, classOf[java.lang.String]))) 
    //    match { case map: Map[String, Any] => map.get("Concept").asInstanceOf[String]}  )
        
    val classtag = ClassTag[point](point.getClass)
    println(db.getDocument(id, classtag.runtimeClass))
    
    
    edges.flatMap { x => Array(x._from, x._to) }.distinct().collect()
      .map(x => 
        {println(x)
        (x, x)//db.getDocument(x, point.getClass).hashCode())
        }
        ).foreach(println) 
    
    val idMap = edges.flatMap { x => Array(x._from, x._to) }.distinct().collect()
      .map(x => (x, db.getDocument(x, point.getClass.asInstanceOf[Class[point]]).hashCode())).toMap

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] = edges.map { x => (x._from, x._to, x.relation) }
      .mapPartitions { iter =>
        {
          for (e <- iter) yield Edge(idMap(e._1), idMap(e._2), e._3)
        }
      }

    // Define a default user in case there are relationship with missing user
    val defaultconcept = ""
    // Build the initial Graph
    val graph = Graph(concept, relationships, defaultconcept)
    graph
  }
  
  def toArrango(graph: Graph[String, String], database: String, graphdb: String, vertex: String, edge: String) = {
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
    val vid = graph.vertices.collect().map(x => (x._2, g.vertexCollection(vertex).insertVertex(point(x._2), null).getId)).toMap;
    graph.triplets.collect().map(x => g.edgeCollection(edge).insertEdge(link(vid(x.srcAttr), vid(x.dstAttr), x.attr)))

  }

}

object ArrangoGraphX {
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

}
