import org.apache.spark._
import org.elasticsearch.spark._
import org.elasticsearch.spark.sql._
import org.elasticsearch.spark.rdd.Metadata._
import org.apache.spark.sql.DataFrame
import org.apache.spark.graphx.{ Edge, VertexId, Graph }

class ElasticsearchGraphX(val sc: SparkContext) {
  def save(graph: Graph[String, String], index:String) = {
    
  }
  
  
  def save(triple: DataFrame, index:String) = { 
    triple.saveToEs(index)//("spark/vertex")
  }
  //    
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