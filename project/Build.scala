import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt._
import Keys._


object Build extends Build {

  val dependencies = Seq(
    "com.typesafe.akka" %% "akka-agent" % "2.4.1",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.github.ScalABM" %% "markets-sandbox" % "0.1.0-alpha-SNAPSHOT"
  )

  val options = Seq(
    "-Xmn3G",
    "-Xmx4G",
    //"-XX:+UseG1GC",
    "-XX:+UseNUMA",
    "-XX:+UseCondCardMark",
    "-XX:-UseBiasedLocking",
    "-XX:+PrintCommandLineFlags"
    //"-XX:+PrintGCDetails",
    //"-XX:+PrintGCTimeStamps"
  )

  lazy val exampleProject = Project("example-model", file("."))
    .enablePlugins(JavaAppPackaging, DockerPlugin)
    .settings(
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.11.7",
      libraryDependencies ++= dependencies,
      fork in run := true,
      javaOptions in run ++= options
    )

}
