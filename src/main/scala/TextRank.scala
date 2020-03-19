import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import org.apache.spark.rdd.RDD

import scala.util.control.Breaks._
import com.huaban.analysis.jieba.JiebaSegmenter
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import Jieba.jieba

object TextRankWordSet extends Serializable{

  def transform(document: Iterable[_]): mutable.HashMap[String, mutable.HashSet[String]] ={
    val keyword = mutable.HashMap.empty[String, mutable.HashSet[String]]
    val que = mutable.Queue.empty[String]

    document.foreach { term =>
      val word = term.toString
      if (!keyword.contains(word)) {
        /* 初始化，对每个分词分配一个 HashSet 空间*/
        keyword.put(word, mutable.HashSet.empty[String])
      }
      que.enqueue(word)
      if (que.size > 5) {
        que.dequeue()
      }

      for (w1 <- que) {
        for (w2 <- que) {
          if (!w1.equals(w2)) {
            keyword.apply(w1).add(w2)
            keyword.apply(w2).add(w1)
          }
        }
      }
    }
    keyword
  }

  def transform[D <: Iterable[_]] (dataset: RDD[D]): RDD[mutable.HashMap[String, mutable.HashSet[String]]] = {
    dataset.map(this.transform)
  }
}

class TextRankKeyword extends Serializable{
  var numKeyword: Int = 10 /* 关键词个数 */
  var d: Double = 0.85f /* 阻尼系数 */
  var max_iter: Int = 200 /* 最大迭代次数 */
  var min_diff: Double = 0.001f /* 最小变化区间 */
  private final var index:Int = 0
  /* 排序，根据分词对应的权重，由高到底排序 */
  def sortByValue(dataset: RDD[mutable.HashMap[String, Double]]): RDD[Seq[(String, Double)]] = {
    dataset.map(doc => {
      val mapDoc = doc.toSeq
      println("mapDoc before Sort: ")
      mapDoc.foreach(println)
      mapDoc.sortWith(_._2 > _._2)
    })
  }

  def rank(document: mutable.HashMap[String, mutable.HashSet[String]]): mutable.HashMap[String, Double] = {
    var score = mutable.HashMap.empty[String, Double]
    breakable {
      for (iter <- 1 to max_iter) {
        val tmpScore = mutable.HashMap.empty[String, Double]
        var max_diff: Double = 0f
        for (word <- document) {
          tmpScore.put(word._1, 1 - d)
          for (element <- word._2) {
            val size = document.apply(element).size
            if(0 == size) println("document.apply(element).size == 0 :element: " + element + "keyword: " + word._1)
            if(word._1.equals(element)) println("word._1.equals(element): " + element + "keyword: " + word._1)
            if ((!word._1.equals(element)) && (0 != size)) {
              /* 计算，这里计算方式可以和TextRank的公式对应起来 */
              tmpScore.put(word._1, tmpScore.apply(word._1) + ((d / size) * score.getOrElse(word._1, 0.0d)))
            }
          }
          /* 取出每次计算中变化最大的值，用于下面得比较，如果max_diff的变化低于min_diff，则停止迭代 */
          max_diff = Math.max(max_diff, Math.abs(tmpScore.apply(word._1) - score.getOrElse(word._1, 0.0d)))
        }
        score = tmpScore
        if(max_diff <= min_diff) break()
      }
    }
    score
  }

  def rank(dataset: RDD[mutable.HashMap[String, mutable.HashSet[String]]]): RDD[mutable.HashMap[String, Double]] = {
    dataset.map(this.rank)
  }
}

object TextRank {
  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._
  //使用pageRand算法计算句子排名----计算单个句子的得分
  def pageRank(board:Array[Array[Double]],ranks:Array[Double],num:Int): Double ={
    val len=board.length
    val d = 0.85
    var added_score = 0.0
    for(j<-0 until len){
      var fraction = 0.0
      var denominator:Double = 0.0
      // 先计算分子
      fraction= board(j)(num) * ranks(j)
      // 计算分母
      for(k<-0 until len){
        denominator = denominator + board(j)(k)
      }
      added_score += fraction / denominator
    }
    val weighted_score = (1 - d) + d * added_score
    return weighted_score
  }

  //句子间的相似度计算：共同词个数/log（len1）+log(len2）
  def similar_cal(sent1:List[String],sent2:List[String]): ((String, String), Double) ={
    val same_word=sent1.intersect(sent2)
    val sim=same_word.size/(math.log(sent1.size)+math.log(sent2.size))
    (sent1.toString(),sent2.toString())->sim
  }

  //构建句子间的相邻矩阵
  def createMat(document:Map[Int, List[String]]): Array[Array[Double]] ={
    val num = document.size
    // 初始化表
    val board=Array.ofDim[Double](num,num)
    for(i<-0 until  num){
      for(j<-0 until num){
        if(i!=j){
          board(i)(j) = similar_cal(document.get(i).get, document.get(j).get)._2
        }
      }
    }
    return board
  }

  //中文分词
  def cutWord(sentense:String): List[String] ={
    val jieba = new JiebaSegmenter
    val sent_split:List[String]= jieba.sentenceProcess(sentense).asScala.toList.filterNot(str=>str.matches("，|“|”|：|、"))
    return sent_split
  }
  //拆分句子
  def  splitSentence(document:String ): Map[Int, List[String]] ={
    val pattern="(。|！|？|\\.|\\!|\\?)".r
    val res=pattern.split(document).toList.zipWithIndex.map(str=>str._2->cutWord(str._1)).toMap
    return res
  }

  def unit_test(spark: SparkSession): Unit = {
    //训练词向量

    val text="朝鲜外相今抵新加坡，穿黑西装打紫色领带，将与多国外长会谈。朝鲜外相李勇浩今抵新加坡。朝中社英文版2日的报道称，李勇浩率领朝鲜代表团于当天启程，除了新加坡，他还将访问伊朗。"
    //分句拆词
    val sent:Map[Int, List[String]]=splitSentence(text)
    val metric=createMat(sent)
    for(i<-metric)println(i.mkString(" "))
    val ranks=Array.ofDim[Double](metric.length).map(str=>1.0)
    //循环迭代结算得分
    for(i<-0 until 5){
      val res:Map[Int, Double]=sent.keys.map{ line=>
        val score=pageRank(metric,ranks,line)
        line->score
      }.toMap
      res.foreach(str=>println("词"+str._1+"=="+str._2))
      //更新第一次循环后的得分
      res.map(str=>ranks.update(str._1,str._2))
    }

    val tr = new TextRankKeyword()
    println(tr.rank(TextRankWordSet.transform(cutWord(text))).toList.sortBy (-_._2))
  }
}
