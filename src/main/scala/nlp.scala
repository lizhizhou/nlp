import org.apache.spark.sql.SparkSession

import org.apache.spark.sql.functions._
import com.databricks.spark.corenlp.functions._

object NLP {
  def main(args: Array[String]) {
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val sc = spark.sparkContext
    val data = Array(1, 2, 3, 4, 5)
    val distData = sc.parallelize(data)
    distData.collect.foreach(println)

    
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)  
    import sqlContext.implicits._
    
    val input = Seq(
      (1, "<xml>Stanford University is located in California. It is a great university.</xml>")).toDF("id", "text")

    val output = input
      .select(cleanxml('text).as('doc))
      .select(explode(ssplit('doc)).as('sen))
      .select('sen, tokenize('sen).as('words), ner('sen).as('nerTags), sentiment('sen).as('sentiment))

    output.show(truncate = false)

    sc.stop()
    
    
//val conf = new SparkConf()
//    .set("arangodb.host", "127.0.0.1")
//    .set("arangodb.port", "8529")
//    .set("arangodb.user", "root")
//    .set("arangodb.password", "lab123")
//    ...
//val sc = new SparkContext(conf)
    
//val rdd = ArangoSpark.load[MyBean](sc, "myCollection")
//ArangoSpark.save(rdd, "myCollection")

  }
}
