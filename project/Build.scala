import sbt._
import Keys._

object Plugin extends Build {

  lazy val buildVersion = "0.1-SNAPSHOT"
  lazy val playVersion = "2.1-SNAPSHOT"

  lazy val res = Project(
    id = "res",
    base = file("."),
    settings = Project.defaultSettings ++ Publish.settings ++ Ls.settings
  ).settings(
    organization := "se.digiplant",
    version := buildVersion,
    scalaVersion := "2.9.2",
    shellPrompt := ShellPrompt.buildShellPrompt,
    parallelExecution in Test := false,

    // Use when developing against play master
    // resolvers += Resolver.file("Local Play Repository", file(Path.userHome.absolutePath + "/Lib/play2/repository/local"))(Resolver.ivyStylePatterns),

    libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.4",
      "commons-codec" % "commons-codec" % "1.6",
      "play" %% "play" % playVersion,
      "play" %% "play-test" % playVersion % "test"
    )
  )
}

object Publish {
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/digiplant/play-res")),
    pomPostProcess := {
      (pomXML: scala.xml.Node) =>
        PomPostProcessor(pomXML)
    },
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
  )
}

object PomPostProcessor {
  import scala.xml._

  // see https://groups.google.com/d/topic/simple-build-tool/pox4BwWshtg/discussion
  // adding a post pom processor to make sure that pom for salat-core correctly specifies depdency type pom for casbah dependency

  def apply(pomXML: Node): Node = {
    def rewrite(pf: PartialFunction[Node, Node])(ns: Seq[Node]): Seq[Node] = for (subnode <- ns) yield subnode match {
      case e: Elem =>
        if (pf isDefinedAt e) pf(e)
        else Elem(e.prefix, e.label, e.attributes, e.scope, rewrite(pf)(e.child): _*)
      case other => other
    }

    val rule: PartialFunction[Node, Node] = {
      case e @ Elem(prefix, "dependency", attribs, scope, children @ _*) => {
        if (children.contains(<groupId>org.mongodb</groupId>)) {
          Elem(prefix, "dependency", attribs, scope, children ++ <type>pom</type>: _*)
        }
        else e
      }
    }

    rewrite(rule)(pomXML.theSeq)(0)
  }
}

// Shell prompt which show the current project, git branch and build version
object ShellPrompt {
  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }
  def currBranch = (
    ("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## "
    )

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (
        currProject, currBranch, ResBuild.buildVersion
      )
    }
  }
}

object Ls {

  import _root_.ls.Plugin.LsKeys._

  lazy val settings = _root_.ls.Plugin.lsSettings ++ Seq(
    (description in lsync) := "Resource management for Play Framework 2",
    licenses in lsync <<= licenses,
    (tags in lsync) := Seq("play", "resource"),
    (docsUrl in lsync) := Some(new URL("https://github.com/digiplant/play-res/wiki"))
  )
}