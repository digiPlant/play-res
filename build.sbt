import play.PlayImport.PlayKeys._

name := "play-res"

version := "1.1.0"

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

credentials := Seq(Credentials(Path.userHome / ".ivy2" / ".digiplantcredentials"))

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/digiplant/play-res"))

pomExtra := (
  <scm>
    <url>git://github.com/digiplant/play-res.git</url>
    <connection>scm:git://github.com/digiplant/play-res.git</connection>
  </scm>
    <developers>
      <developer>
        <id>leon</id>
        <name>Leon Radley</name>
        <url>http://github.com/leon</url>
      </developer>
    </developers>)