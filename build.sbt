import Dependencies._

ThisBuild / scalaVersion := Versions.scalaVersion
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.marimon"
ThisBuild / organizationName := "marimon"

semanticdbEnabled := true
semanticdbVersion := "4.4.31"

// TODO : split main and experiments

lazy val root = (project in file("."))
  .settings(
    name := "semanticdb-index",
    libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.13.8" , // only used in experiments for reflection
    libraryDependencies += playJson,
    libraryDependencies += scalameta,
    libraryDependencies += scalaTest ,

  )
