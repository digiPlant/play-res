name := "play-res"

version := "1.2.0"

scalaVersion := "2.12.2"

organization := "se.digiplant"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.5",
  "commons-codec" % "commons-codec" % "1.10",
  specs2 % Test,
  "com.typesafe.play" %% "play" % play.core.PlayVersion.current % "provided",
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
