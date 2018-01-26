name := "NLP Project"

version := "1.0"

crossScalaVersions := Seq("2.11.8", "2.10.6")

sparkVersion := "2.2.0"

sparkComponents ++= Seq("streaming", "sql")

spAppendScalaVersion := true

libraryDependencies ++= Seq(
   "databricks" %% "spark-corenlp" % "0.3.0-SNAPSHOT",
   "org.elasticsearch" % "elasticsearch-spark-20_2.10" % "6.1.2",
   "com.arangodb" % "arangodb-spark-connector" % "1.0.0"
)