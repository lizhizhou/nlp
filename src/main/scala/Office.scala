import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

class Office(spark: SparkSession) {
  val sc = spark.sparkContext
  def openWord(path: String*) = {
    val pathRDD = sc.parallelize(path)
    pathRDD.map { x =>
      val opcPackage = POIXMLDocument.openPackage(x);
      val extractor = new XWPFWordExtractor(opcPackage);
      extractor.getText();
    }
  }
}

object Office {
  def apply(spark: SparkSession) = new Office(spark)
  def unitTest()
  {
    
  }
}
