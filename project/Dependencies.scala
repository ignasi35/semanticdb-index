import sbt._

object Dependencies {
  object Versions {
    lazy val scalaVersion = "2.13.7"
  }
  lazy val playJson = "com.typesafe.play" %% "play-json" % "2.9.2"
  lazy val scalameta = "org.scalameta" % s"semanticdb-scalac_${Versions.scalaVersion}" % "4.4.31"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
}
