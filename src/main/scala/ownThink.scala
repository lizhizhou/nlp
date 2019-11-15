import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
class ownThink(spark: SparkSession, path:String) {
  val ownThinkCsv = spark.read.csv(path)
  def toTriple() = {
    ownThinkCsv
  }
//  def getSchema() = StructType(Array(StructField(obj, StringType, nullable = true),
//    StructField(rel, StringType, nullable = true),
//    StructField(sub, StringType, nullable = true)))
}

object ownThink {
  def apply(spark: SparkSession, path:String) = new ownThink(spark, path)
  def unitTest(spark: SparkSession): Unit =
  {
    val csv = ownThink(spark, "ownthink_v2.csv")
    csv.toTriple.show()
  }
}
