import org.apache.spark.sql._
import org.apache.spark.sql.types._

object tsfile {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    import spark.implicits._
    val input = (1 to 3000).map(x => (x, Math.sin(x * 0.1)))
      .toDF("time", "value")
    input.show()
    input.write.format("org.apache.iotdb.spark.tsfile").save("output")
    spark.stop()
  }
}
