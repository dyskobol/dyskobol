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
      "org.xerial" % "sqlite-jdbc" % "3.7.2",
      "com.mchange" % "c3p0" % "0.9.2.1",

      "org.scalatest" %% "scalatest" % "3.0.2" % "test"
    )
  ).

  settings(target in javah := (sourceDirectory in nativeCompile in native).value / "include").
  dependsOn(native % Runtime)

lazy val native = (project in file("native")).
  settings(sourceDirectory in nativeCompile := sourceDirectory.value).
  enablePlugins(JniNative)
