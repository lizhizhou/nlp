name := "NLP Project"

version := "1.0"

scalaVersion := "2.11.8"

sparkVersion := "2.2.1"

sparkComponents ++= Seq("streaming", "sql", "graphx", "mllib")

spAppendScalaVersion := true

libraryDependencies ++= Seq(
   "databricks" %% "spark-corenlp" % "0.3.0-SNAPSHOT",
   "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "6.1.2",
   "com.arangodb" % "arangodb-spark-connector" % "1.0.0"
)