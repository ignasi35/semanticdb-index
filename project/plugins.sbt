// dynver is already brought in by ci-release
// addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")


// bring in dynver, pgp, sonatype and sbt-git
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")

addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.2")
