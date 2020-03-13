// see http://www.wartremover.org/
lazy val warts =
  Warts.allBut(
    Wart.Nothing,
    Wart.Recursion,
    Wart.NonUnitStatements,
    Wart.ToString
  )

lazy val splain: ModuleID = "io.tryp" % "splain" % "0.5.1" cross CrossVersion.patch
lazy val kindProjector: ModuleID = "org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary

lazy val commonSettings: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "chrilves",
        scalaVersion := "2.13.1",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % Test,
    scalacOptions /*in (Compile, console)*/ -= "-Xfatal-warnings",
    scalacOptions -= "-Ywarn-unused:params",
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    libraryDependencies in (Compile, compile) += splain,
    libraryDependencies in (Compile, console) += splain,
    addCompilerPlugin(kindProjector),
    scalafmtOnCompile := true
  )

/* Things maybe useful*/
lazy val toolbox =
    project
    .in(file("toolbox"))
    .settings(commonSettings: _*)
    .settings(name := "toolbox")

/* Game Logic */
lazy val slimetrail =
  project
    .in(file("slimetrail"))
    .settings(commonSettings: _*)
    .settings(name := "slimetrail")
    .dependsOn(toolbox)

// Text Interface
lazy val text =
  project
    .in(file("text"))
    .settings(commonSettings: _*)
    .settings(name := "slimetrail-text")
    .dependsOn(slimetrail)
