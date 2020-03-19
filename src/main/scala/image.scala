import org.apache.spark.sql._
import org.apache.spark.sql.types._
import com.intel.analytics.zoo.common.NNContext
import com.intel.analytics.zoo.pipeline.nnframes.NNImageReader

object Image {
  def unitTest(spark: SparkSession) = {
    val sc = NNContext.initNNContext("app")
    val imageDF = NNImageReader.readImages("*.jpg", sc)
    imageDF.show()
  }
}