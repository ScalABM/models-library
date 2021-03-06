name := "example-model"

version := "0.1.0-SNAPSHOT"

organization := "com.github.ScalABM"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-agent" % "2.4.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
  "com.typesafe.play" %% "play-json" % "2.4.6",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.github.ScalABM" %% "markets-sandbox" % "0.1.0-alpha-SNAPSHOT"
)

fork in run := true

javaOptions ++= Seq(
  "-Xmn3G",
  "-Xmx6G",
  //"-XX:+UseG1GC",
  "-XX:+UseNUMA",
  "-XX:+UseCondCardMark",
  "-XX:-UseBiasedLocking",
  "-XX:+PrintCommandLineFlags"
  //"-XX:+PrintGCDetails",
  //"-XX:+PrintGCTimeStamps"
)

// Specify Docker options
enablePlugins(JavaAppPackaging, DockerPlugin)
maintainer in Docker := "davidrpugh <david.pugh@maths.ox.ac.uk>"
dockerRepository := Some("davidrpugh")
