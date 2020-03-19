import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DataTypes

object LSH
{
  def unittest(spark: SparkSession): Unit = {
    import spark.implicits._
    val note = Seq(
      (0, "未办登记只举办结婚仪式可起诉离婚吗"),
      (1, "未办登记只举办结婚仪式可起诉离婚吗"),
      (2, "无偿居间介绍买卖毒品的行为应如何定性"),
      (3, "吸毒男动态持有大量毒品的行为该如何认定"),
      (4, "如何区分是非法种植毒品原植物罪还是非法制造毒品罪"),
      (5, "为毒贩贩卖毒品提供帮助构成贩卖毒品罪"),
      (6, "将自己吸食的毒品原价转让给朋友吸食的行为该如何认定"),
      (7, "为获报酬帮人购买毒品的行为该如何认定"),
      (8, "毒贩出狱后再次够买毒品途中被抓的行为认定"),
      (9, "虚夸毒品功效劝人吸食毒品的行为该如何认定"),
      (10, "妻子下落不明丈夫又与他人登记结婚是否为无效婚姻"),
      (11, "一方未签字办理的结婚登记是否有效"),
      (12, "夫妻双方1990年按农村习俗举办婚礼没有结婚证 一方可否起诉离婚"),
      (13, "结婚前对方父母出资购买的住房写我们二人的名字有效吗"),
      (14, "身份证被别人冒用无法登记结婚怎么办？"),
      (15, "同居后又与他人登记结婚是否构成重婚罪"),
      (16, "同居多年未办理结婚登记，是否可以向法院起诉要求离婚")
    ).toDF("discovery_id", "content")

    val dfUsed = note.select($"discovery_id", $"content", Jieba.jieba_udf($"content").as("words"))
    // Tokenize the wiki content
    val tokenizer = new Tokenizer().setInputCol("words").setOutputCol("word")
    val wordsDf = tokenizer.transform(dfUsed)

    // Word count to vector for each wiki content
    val vocabSize = 1000
    val cvModel: CountVectorizerModel = new CountVectorizer().setInputCol("word").setOutputCol("features")
      .setVocabSize(vocabSize).setMinDF(1).fit(wordsDf)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val vectorizedDf = cvModel.transform(wordsDf).filter(isNoneZeroVector(col("features")))

    val mh = new MinHashLSH().setNumHashTables(10).setInputCol("features").setOutputCol("hashValues")
    val model = mh.fit(vectorizedDf)
    val transform = model.transform(vectorizedDf)

    transform.groupBy("hashValues").agg(collect_list($"discovery_id")
      .alias("ids")).filter(size($"ids") > 1).select("ids")
      .withColumn("group",monotonically_increasing_id()).show()

    val threshold = 0.5
    val result = model.approxSimilarityJoin(vectorizedDf, vectorizedDf, threshold)
    result.show()

    val key = Vectors.sparse(vocabSize, Seq((cvModel.vocabulary.indexOf("结婚"), 1.0)))
    val k = 40
    model.approxNearestNeighbors(vectorizedDf, key, k).show()


    val brp = new BucketedRandomProjectionLSH()
      .setBucketLength(2.0)
      .setNumHashTables(3)
      .setInputCol("features")
      .setOutputCol("hashes")

    val brpmodel = brp.fit(vectorizedDf)

    // Feature Transformation
    println("The hashed dataset where hashed values are stored in the column 'hashes':")
    brpmodel.transform(vectorizedDf).show()
    brpmodel.approxSimilarityJoin(vectorizedDf, vectorizedDf, threshold).show()
    //brpmodel.approxNearestNeighbors(vectorizedDf, key, k).show()

  }
}
