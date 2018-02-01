
import com.arangodb.spark.{ArangoSpark, ReadOptions, WriteOptions}
import com.arangodb.ArangoDB

class ArrangoGraphX {
  
}

object ArrangoGraphX{ 
    def apply() = {
      
    case class Concept(name: String)
    case class Triple(obj: String, rel: String, subject: String)
    case class link(var _from: String, var _to:String, var relation:String) {
      def this() = this("","","")
    }

    case class point(var Concept: String) {
      def this() = this("")
    }
    println("Write link data")
//    ArangoSpark.save(vertex.rdd.map { x => point( x.getAs("Concept"))}, "vertex", WriteOptions("test"))
//    ArangoSpark.save(triple.rdd.map { x => link("vertex/" + x.getAs("object"), "vertex/"+x.getAs("subject"), x.getAs("relation")) }, "link",WriteOptions("test"))
//    val rdd = ArangoSpark.load[link](sc, "link", ReadOptions("test"))
//    println("Read link data")
//    rdd.collect().foreach(println)
    
    //ArangoSpark.saveDF(triple, "edge")
//    ArangoSpark.save(sc.makeRDD(Seq(link("persons/alice", "persons/dave", "test"))), "knows", WriteOptions("test"))

//    import com.arangodb.entity.DocumentField
//    import com.arangodb.entity.DocumentField.Type
    
    
    
//    val arangoDB: ArangoDB = new ArangoDB.Builder().user("root").password("lab123").build();
    //arangoDB.createDatabase("test");
//    val db = arangoDB.db("test");
//    val g = db.graph("myGraph")
//    val vid = graph.vertices.collect().map(x => (x._2,g.vertexCollection("concept").insertVertex(point(x._2), null).getId)).toMap;
//    triple.collect().map(x =>  g.edgeCollection("link").insertEdge(link(vid(x.getAs("object")),vid(x.getAs("subject")),x.getAs("relation"))))
    //graph.edges.collect().map(x =>  g.edgeCollection("link").insertEdge(link(vid(x.srcId.),vid(x.getAs("subject")),x.getAs("relation"))))
    }
  
}