import org.apache.spark.graphx._
import org.apache.spark.graphx.lib._
import org.apache.spark.sql.SparkSession

object LPA {
  def unitTest(spark: SparkSession): Unit = {
    val sc = spark.sparkContext
    val n = 5
    val clique1 = for (u <- 0L until n; v <- 0L until n) yield Edge(u, v, 1)
    val clique2 = for (u <- 0L to n; v <- 0L to n) yield Edge(u + n, v + n, 1)
    val twoCliques = sc.parallelize(clique1 ++ clique2 :+ Edge(0L, n, 1))
    val graph = Graph.fromEdges(twoCliques, 1)
    // Run label propagation
    val labels = LabelPropagation.run(graph, n * 4).cache()
    val clique1Labels = labels.vertices.filter(_._1 < n).map(_._2).collect.toArray
    clique1Labels.foreach(println)
    val clique2Labels = labels.vertices.filter(_._1 >= n).map(_._2).collect.toArray
    clique2Labels.foreach(println)
  }
}
