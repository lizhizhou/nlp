import org.apache.spark.sql._
import org.apache.spark.sql.types._

object minio {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val sc = spark.sparkContext
    //Endpoint:  http://192.168.13.56:9000  http://127.0.0.1:9000
    //AccessKey: GMXNG65FSOIPNQOK62G2
    //SecretKey: YG7WRhAlcrH5NjYsIV6xb0mzZoBr5I6HqbDuLFbQ
    sc.hadoopConfiguration.set("fs.s3a.endpoint", "http://127.0.0.1:9000")
    sc.hadoopConfiguration.set("fs.s3a.access.key", "GMXNG65FSOIPNQOK62G2")
    sc.hadoopConfiguration.set("fs.s3a.secret.key", "YG7WRhAlcrH5NjYsIV6xb0mzZoBr5I6HqbDuLFbQ")
    val schema = StructType(
      List(
        StructField("name", StringType, true),
        StructField("age", IntegerType, false)
      )
    )

    val df = spark.read.format("io.minio.spark.select.SelectJSONSource").schema(schema).load("s3://test/people.json")

    println(df.show())
  }
}
