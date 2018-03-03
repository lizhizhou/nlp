import org.neo4j.spark._
import org.neo4j.spark.Neo4j.{ Pattern, NameProp }
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib._
import org.apache.spark.sql.SparkSession

class Neo4jGraphX(spark: SparkSession) {
  val sc = spark.sparkContext
  val neo = Neo4j(sc)
  def toNeo4j(graph: Graph[String, String]) {
    neo.saveGraph(graph, "concept", Pattern(NameProp("Concept", "id"), Array(NameProp("Relation", "text")), NameProp("Concept", "id")), merge = true)
  }
  def toGraphX() = {
    val graphQuery = "MATCH (n:Concept)-[r:Relation]->(m:Concept) RETURN id(n) as source, id(m) as target, type(r) as value SKIP {_skip} LIMIT {_limit}"
    val graph: Graph[String, String] = neo.rels(graphQuery).partitions(7).batch(200).loadGraph
    graph
  }
}

object Neo4jGraphX {
  def apply(spark: SparkSession) = new Neo4jGraphX(spark)
  def unitTest()
  {
    
  } 
  //// load graph via Cypher query
  //val graphQuery = "MATCH (n:Person)-[r:KNOWS]->(m:Person) RETURN id(n) as source, id(m) as target, type(r) as value SKIP {_skip} LIMIT {_limit}"
  //val graph: Graph[Long, String] = neo.rels(graphQuery).partitions(7).batch(200).loadGraph
  //
  //graph.vertices.count
  ////    => 100
  //graph.edges.count
  ////    => 1000
  //
  //// load graph via pattern
  //val graph = neo.pattern(("Person","id"),("KNOWS","since"),("Person","id")).partitions(7).batch(200).loadGraph[Long,Long]
  //
  //val graph2 = PageRank.run(graph, 5)
  ////    => graph2: org.apache.spark.graphx.Graph[Double,Double] =    
  //
  //graph2.vertices.sort(_._2).take(3)
  ////    => res46: Array[(org.apache.spark.graphx.VertexId, Long)] 
  ////    => Array((236746,100), (236745,99), (236744,98))
  //
  //// uses pattern from above to save the data, merge parameter is false by default, only update existing nodes
  //neo.saveGraph(graph, "rank")
  //// uses pattern from parameter to save the data, merge = true also create new nodes and relationships
  //neo.saveGraph(graph, "rank",Pattern(("Person","id"),("FRIEND","years"),("Person","id")), merge = true)
}
