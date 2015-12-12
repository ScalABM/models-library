name := """example-model"""

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-agent" % "2.4.1",
  "com.github.ScalABM" %% "markets-sandbox" % "0.1.0-alpha-SNAPSHOT"
)

