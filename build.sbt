import Dependencies._

ThisBuild / scalaVersion := Versions.scalaVersion
ThisBuild / organization := "com.marimon"
ThisBuild / organizationName := "marimon"

semanticdbEnabled := true
semanticdbVersion := "4.4.31"

// TODO : split main and experiments
// TODO : split main and samples 

lazy val root = (project in file("."))
  .settings(
    name := "semanticdb-index",
    libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.13.8" , // only used in experiments for reflection
    libraryDependencies += playJson,
    libraryDependencies += scalameta,
    libraryDependencies += scalaTest ,

  )
