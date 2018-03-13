lazy val root = (project in file(".")).
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
      "org.sleuthkit" % "datamodel" % "4.6.0" from "file:////lib/sleuthkit-4.6.0.jar"
    )
  )
