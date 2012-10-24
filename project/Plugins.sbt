resolvers ++= Seq(
  Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com"
)

addSbtPlugin("play" % "sbt-plugin" % "2.0.4")

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6.1")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")
