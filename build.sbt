import Dependencies._

val dottyVersion     = "0.24.0-RC1"
val scala213Version     = "2.13.2"

ThisBuild / scalaVersion          := scala213Version
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  scalaVersion := dottyVersion,
  scalacOptions ++= "-deprecation" :: "-feature" :: Nil,
  scalafmtOnCompile := true
)

lazy val testSettings = Seq(libraryDependencies += scalaTest)

lazy val root = (project in file("."))
  .settings(
    name := "shogi-api-server",
    commonSettings,
    testSettings,
    libraryDependencies ++= Seq(
      circe,
      akka
    ).flatten.map(_.withDottyCompat(scalaVersion.value))
  ) 

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
