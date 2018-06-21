resolvers += Resolver.url(
  "bintray-jodersky-sbt-plugins",
  url("http://dl.bintray.com/jodersky/sbt-plugins"))(
  Resolver.ivyStylePatterns)
ivyLoggingLevel := UpdateLogging.Quiet

addSbtPlugin("ch.jodersky" % "sbt-jni" % "1.3.1")
