import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.log4j.Logger

class Office(spark: SparkSession) {
  @transient lazy val log = Logger.getLogger(this.getClass)
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
  def unitTest(spark: SparkSession)
  {
    val office = new Office(spark)
    office.openWord("test.docx").collect().foreach(println)
    val df = spark.read
        .format("com.crealytics.spark.excel")
        .option("sheetName", "Sheet1") // Required
        .option("useHeader", "true") // Required
        .option("treatEmptyValuesAsNulls", "false") // Optional, default: true
        .option("inferSchema", "false") // Optional, default: false
        .option("addColorColumns", "true") // Optional, default: false
        .option("startColumn", 0) // Optional, default: 0
        .option("endColumn", 99) // Optional, default: Int.MaxValue
        .option("timestampFormat", "MM-dd-yyyy HH:mm:ss") // Optional, default: yyyy-mm-dd hh:mm:ss[.fffffffff]
        .option("maxRowsInMemory", 20) // Optional, default None. If set, uses a streaming reader which can help with big files
        .option("excerptSize", 10) // Optional, default: 10. If set and if schema inferred, number of rows to infer schema from
        .load("test.xlsx")
    df.show()
  }
}
