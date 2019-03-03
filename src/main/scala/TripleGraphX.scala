import scala.reflect.ClassTag
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.graphx.{ Edge, VertexId, Graph }
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

class TripleGraphX[V:ClassTag,E:ClassTag](spark: SparkSession, obj:String, sub:String, rel:String) extends Serializable {

  def toGraphX(triple: DataFrame) = {
    val edges = triple.rdd.map { x => (x.getAs[V](obj), x.getAs[V](sub), x.getAs[E](rel)) }

    val vertex = edges.map(x => x._1).union(edges.map(x => x._2)).distinct()

    // Create an RDD for vertex
    val concept: RDD[(VertexId, V)] = vertex.map(x => (x.hashCode(), x))

    // Create an RDD for edges
    val relationships: RDD[Edge[E]] = edges.map { x => Edge(x._1.hashCode(), x._2.hashCode(), x._3) }

    // Define a default user in case there are relationship with missing user
    val defaultconcept = implicitly[ClassTag[V]].runtimeClass.newInstance.asInstanceOf[V]
    // Build the initial Graph
    val graph = Graph(concept, relationships, defaultconcept)
    graph
  }
  def toTriple(graph: Graph[V, E]) = {

    val triple = graph.triplets.map(triplet =>
      Row(triplet.srcAttr, triplet.attr, triplet.dstAttr))
    spark.createDataFrame(triple.distinct(), getSchema())
  }
  def getSchema() = StructType(Array(StructField(obj, StringType, nullable = true),
    StructField(rel, StringType, nullable = true),
    StructField(sub, StringType, nullable = true)))
}

object TripleGraphX {
  def apply[V:ClassTag,E:ClassTag](spark: SparkSession, obj:String, sub:String, rel:String) =
     new TripleGraphX[V,E](spark, obj, sub, rel)

  def unitTest(spark: SparkSession)
  {
    val graph = TestKnowledgeGraph(spark)
    val tg = TripleGraphX[String,String](spark, "object", "subject", "relation")
    val tf = tg.toTriple(graph)
    val gf = tg.toGraphX(tf)
  }
}
