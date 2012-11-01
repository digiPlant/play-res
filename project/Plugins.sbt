resolvers ++= Seq(
  Resolver.file("Local Play Repository", file(Path.userHome.absolutePath + "/Lib/play2/repository/local"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("releases")
)

addSbtPlugin("play" % "sbt-plugin" % "2.1-SNAPSHOT")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.7")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.0")
