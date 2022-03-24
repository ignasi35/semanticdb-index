import Dependencies._

ThisBuild / scalaVersion := Versions.scalaVersion
ThisBuild / organization := "com.marimon"
ThisBuild / organizationName := "marimon"

//  - - - Setup sbt-github-actions" % "0.14.2" - - -
// publish on tags of type vx.y.z
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
// publish on tags and on `main` Snapshots
ThisBuild / githubWorkflowPublishTargetBranches +=
  RefPredicate.StartsWith(Ref.Tag("v"))
// publish using `sbt ci-release`
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
ThisBuild / githubWorkflowPublish := Seq(
    WorkflowStep.Sbt(
        List("ci-release"),
        env = Map(
            "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
            "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
            "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
            "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
        )
    )
)

sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"


// - - - Project setup - - -

lazy val root = (project in file("."))
  .settings(
    publish / skip := true
  ).aggregate(
    `semanticdb-index`,
    experiments
  )


lazy val experiments = (project in file("experiments"))
  .settings(
    name := "experiments",
    libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.13.8" ,
    publish / skip := true
  )

lazy val `semanticdb-index` = (project in file("semanticdb-index"))
  .settings(
    semanticdbEnabled := true,
    semanticdbVersion := "4.4.31"
  )
  .settings(
    name := "semanticdb-index",
    libraryDependencies += playJson,
    libraryDependencies += scalameta,
    libraryDependencies += scalaTest,
  )
