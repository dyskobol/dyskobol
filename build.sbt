ivyLoggingLevel := UpdateLogging.Quiet

lazy val root = (project in file(".")).
  aggregate(core, native)

lazy val core = (project in file("core")).
  settings(
    organizationName := "pl.dyskobol",
    name := "dyskobol",

    version := "0.0.1-SNAPSHOT",
    mainClass in (Compile, run) := Some("pl.dyskobol.prototype.Dyskobol"),
    mainClass in (Compile, packageBin) := Some("pl.dyskobol.prototype.Dyskobol"),
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.5.11",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "org.xerial" % "sqlite-jdbc" % "3.7.2",
      "com.mchange" % "c3p0" % "0.9.2.1",
      "org.apache.tika" % "tika-core" % "1.17",
      "org.apache.tika" % "tika-parsers" % "1.17",
      "org.scalatest" %% "scalatest" % "3.0.2" % "test",
      "org.apache.poi" % "poi" % "3.17",
      "org.apache.poi" % "poi-ooxml" % "3.17",
      "cognitivej" % "cognitivej" % "0.6.2",
      "org.bytedeco" % "javacv" % "1.1",
      "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1",
      "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1",
      "org.apache.commons" % "commons-imaging" % "1.0-R1401825" from "https://repo.adobe.com/nexus/content/repositories/public/org/apache/commons/commons-imaging/1.0-R1401825/commons-imaging-1.0-R1401825.jar"
    )
  ).

  settings(target in javah := (sourceDirectory in nativeCompile in native).value / "include").
  dependsOn(native % Runtime)

lazy val native = (project in file("native")).
  settings(sourceDirectory in nativeCompile := sourceDirectory.value).
  enablePlugins(JniNative)
