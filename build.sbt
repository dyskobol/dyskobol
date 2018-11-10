ivyLoggingLevel := UpdateLogging.Quiet

lazy val root = (project in file(".")).
  aggregate(core, native).settings(
  mainClass in (Compile, run) := Some("pl.dyskobol.Main")
)


lazy val core = (project in file("core")).
  settings(
    organizationName := "pl.dyskobol",
    name := "dyskobol",

    version := "0.0.1-SNAPSHOT",
    mainClass in (Compile, run) := Some("pl.dyskobol.Main"),
    mainClass in (Compile, packageBin) := Some("pl.dyskobol.Main"),
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(


      "org.scala-lang" % "scala-library" % "2.12.7",
      "org.scala-lang" % "scala-reflect" % "2.12.7",
      "org.scala-lang" % "scala-compiler" % "2.12.7",
      "com.typesafe.akka" %% "akka-actor" % "2.5.17",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.17" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.17",
      "org.xerial" % "sqlite-jdbc" % "3.7.2",
      "com.mchange" % "c3p0" % "0.9.2.1",
      "org.apache.tika" % "tika-core" % "1.18",
      "org.apache.tika" % "tika-parsers" % "1.18",
      "org.scalatest" %% "scalatest" % "3.0.2" % "test",
      "org.apache.poi" % "poi" % "3.17",
      "org.apache.poi" % "poi-ooxml" % "3.17",
      "cognitivej" % "cognitivej" % "0.6.2",
      "org.bytedeco" % "javacv" % "1.1",
      "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1",
      "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1",
      "org.apache.commons" % "commons-imaging" % "1.0-R1401825" from "https://repo.adobe.com/nexus/content/repositories/public/org/apache/commons/commons-imaging/1.0-R1401825/commons-imaging-1.0-R1401825.jar",
      "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
      "com.typesafe.slick" %% "slick" % "3.2.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.akka" %% "akka-persistence" % "2.5.17",
      "net.codingwell" %% "scala-guice" % "4.2.1",
      "me.tongfei" % "progressbar" % "0.7.2",
      "org.apache.pdfbox" % "jbig2-imageio" % "3.0.1",
      "com.github.jai-imageio" % "jai-imageio-jpeg2000" % "1.3.0",
    )
  ).

  settings(target in javah := (sourceDirectory in nativeCompile in native).value / "include").
  dependsOn(native % Runtime)

lazy val native = (project in file("native")).
  settings(sourceDirectory in nativeCompile := sourceDirectory.value).
  enablePlugins(JniNative)

