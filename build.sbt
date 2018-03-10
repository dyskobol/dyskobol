lazy val root = (project in file(".")).
  settings(
    organizationName := "pl.dyskobol",
    name := "dyskobol",

    version := "0.0.1-SNAPSHOT",
    mainClass in (Compile, run) := Some("pl.dyskobol.prototype.Dyskobol"),
    mainClass in (Compile, packageBin) := Some("pl.dyskobol.prototype.Dyskobol"),
    scalaVersion := "2.12.4"
  )
