import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SparkSession

class Metrics {

}

object Metrics {

  def unitTest(spark: SparkSession): Unit = {
    val predictionAndLabels = spark.sparkContext.parallelize(Array((1.0,1.0),(1.0,0.0),(0.0,1.0),(0.0,0.0)))

    // Instantiate metrics object
    val metrics = new MulticlassMetrics(predictionAndLabels)

    // Confusion matrix
    println("Confusion matrix:")
    println(metrics.confusionMatrix)

  }
}