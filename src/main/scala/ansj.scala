import org.ansj.splitWord.analysis.ToAnalysis
import org.apache.spark.sql.functions._

class ansj() {
  def ansj_udf = udf((s: String) => ToAnalysis.parse(s))
}

object ansj
{
  def unit_test() = {
    val str: String = "欢迎使用ansj_seg,(ansj中文分词)在这里如果你遇到什么问题都可以联系我.我一定尽我所能.帮助大家.ansj_seg更快,更准,更自由!"
    println(ToAnalysis.parse(str))
  }
}