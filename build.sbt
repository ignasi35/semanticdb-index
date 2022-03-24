import Dependencies._

ThisBuild / scalaVersion := Versions.scalaVersion
ThisBuild / organization := "com.marimon"
ThisBuild / organizationName := "marimon"

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))


inThisBuild(List(
    organization := "com.marimon-clos",
    homepage := Some(url("https://github.com/ignasi35/semanticdb-index")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
        Developer(
            "ignasi35",
            "Ignasi Marimon-Clos",
            "ignasi35@gmail.com",
            url("https://www.marimon-clos.com")
        )
    )
))
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"



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
