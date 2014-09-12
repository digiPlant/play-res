import play.PlayImport.PlayKeys._

name := "play-res"

version := "1.1.1"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.10.4", "2.11.1")

organization := "se.digiplant"

playPlugin := true

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4",
  "commons-codec" % "commons-codec" % "1.6",
  "com.typesafe.play" %% "play" % play.core.PlayVersion.current % "provided",
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
