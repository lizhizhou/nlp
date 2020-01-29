package com.navercorp

import org.apache.spark.sql.DataFrame

import scala.util.Try

object util {
  def hasColumn(df: DataFrame, path: String) = Try(df(path)).isSuccess
}
