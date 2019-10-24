resolvers += "Spark Packages repo" at "https://dl.bintray.com/spark-packages/maven/"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
addSbtPlugin("org.spark-packages" %% "sbt-spark-package" % "0.2.5")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")