name := "play-res"

version := "1.2.0"

scalaVersion := "2.11.7"

organization := "se.digiplant"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.4",
  "commons-codec" % "commons-codec" % "1.6",
  specs2 % Test,
  "com.typesafe.play" %% "play" % play.core.PlayVersion.current % "provided",
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
