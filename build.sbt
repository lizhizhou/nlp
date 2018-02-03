name := "NLP Project"

version := "1.0"

scalaVersion := "2.11.8"

sparkVersion := "2.2.1"

sparkComponents ++= Seq("streaming", "sql", "graphx", "mllib")

spAppendScalaVersion := true

libraryDependencies ++= Seq(
   "databricks" %% "spark-corenlp" % "0.3.0-SNAPSHOT",
   "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "6.1.2",
   "com.arangodb" % "arangodb-spark-connector" % "1.0.2",
   "com.arangodb" % "arangodb-java-driver" % "4.3.0",
   "org.apache.jena" % "jena-elephas-io" % "0.9.0"
)

// sbt-assembly 0.14.0 adds shading support.
//assemblyShadeRules in assembly := Seq(
//  ShadeRule.rename("org.apache.commons.io.**" -> "shadeio.@1").inAll
//)