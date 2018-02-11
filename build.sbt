name := "NLP Project"

version := "1.0"

scalaVersion := "2.11.8"

sparkVersion := "2.2.1"

sparkComponents ++= Seq("streaming", "sql", "graphx", "mllib")

spAppendScalaVersion := true

libraryDependencies ++= Seq(
   "databricks" %% "spark-corenlp" % "0.3.0-SNAPSHOT",
   "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "6.1.2",
   "neo4j-contrib" % "neo4j-spark-connector" % "2.1.0-M4",
   "com.arangodb" % "arangodb-spark-connector" % "1.0.2",
   "com.arangodb" % "arangodb-java-driver" % "4.3.0",
   "org.apache.jena" % "jena-elephas-io" % "0.9.0"
)

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "axiom.xml" => MergeStrategy.filterDistinctLines
    case PathList(ps @ _*) if ps.last endsWith "Log$Logger.class" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "ILoggerFactory.class" => MergeStrategy.first
    case x => old(x)
  }
}

// sbt-assembly 0.14.0 adds shading support.
//assemblyShadeRules in assembly := Seq(
//  ShadeRule.rename("org.apache.commons.io.**" -> "shadeio.@1").inAll
//)