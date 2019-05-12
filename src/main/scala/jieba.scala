import com.huaban.analysis.jieba.JiebaSegmenter
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import org.apache.spark.sql.functions._
import scala.collection.JavaConversions._

object jieba
{
  def jieba(s:String) =  {
    val segmenter = new JiebaSegmenter()
    segmenter.process(s, SegMode.SEARCH).toList.map(_.word + " ").foldLeft("")(_+_).toString
  }

  def jieba_udf = udf((s: String) => jieba(s))

  def unit_test(): Unit =
  {
    val sentences: Array[String] = Array[String]("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。", "我不喜欢日本和服。", "雷猴回归人间。", "工信处女干事每月经过下属科室都要亲口交代24口交换机等技术性器件的安装工作", "结果婚的和尚未结过婚的")
    for (sentence <- sentences) {
      println(
        jieba(sentence))
    }
  }

}