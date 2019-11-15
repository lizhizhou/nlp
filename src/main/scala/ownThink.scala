import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
class ownThink(spark: SparkSession, path:String) {
  val ownThinkCsv = spark.read.option("header", "true").csv(path)
  def toTriple() = {
    ownThinkCsv.toDF("object", "relation", "subject")
  }
}

object ownThink {
  def apply(spark: SparkSession, path:String) = new ownThink(spark, path)
  def unitTest(spark: SparkSession): Unit =
  {
    val csv = ownThink(spark, "ownthink_v2.csv")
    csv.toTriple.show()
  }
}
