import org.apache.spark._
import org.elasticsearch.spark._
import org.elasticsearch.spark.sql._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.elasticsearch.spark.rdd.Metadata._
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.apache.spark.graphx.{ Edge, VertexId, Graph }

class ElasticsearchGraphX(spark: SparkSession) {
  val sc = spark.sparkContext
  def toES(graph: Graph[String, String], index:String) {
    this.toES(TripleGraphX(spark).toTriple(graph),index)
  }
  
  def toES(triple: DataFrame, index:String) { 
    triple.saveToEs(index)//("spark/vertex")
  }
  def toGraphX(index:String)
  {
    val triple = sc.esRDD(index)
    triple.take(5).foreach(println)
    //.map(triplet =>
//      Row(triplet.srcAttr, triplet.attr, triplet.dstAttr))
//    spark.createDataFrame(triple.distinct(), TripleGraphX.schema)
    
  } 

}

object ElasticsearchGraphX {
  def apply(spark: SparkSession) = new ElasticsearchGraphX(spark)
  def unitTest(spark: SparkSession)
  {
    val graph = TestKnowledgeGraph(spark)
    
//    val es = sc.esRDD("spark/vertex")
//    es.take(10).foreach(println)
//
//    val otp = Map("iata" -> "OTP", "name" -> "Otopeni")
//    val muc = Map("iata" -> "MUC", "name" -> "Munich")
//    val sfo = Map("iata" -> "SFO", "name" -> "San Fran")
//
//    // metadata for each document
//    // note it's not required for them to have the same structure
//    val otpMeta = Map(ID -> 1)
//    val mucMeta = Map(ID -> 2)//, VERSION -> "23")
//    val sfoMeta = Map(ID -> 3)
//
//    val airportsRDD = sc.makeRDD(Seq((otpMeta, otp), (mucMeta, muc), (sfoMeta, sfo)))
//    airportsRDD.saveToEsWithMeta("airports/2015")
  }
}