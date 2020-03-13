// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

// voir http://www.wartremover.org/
lazy val warts = {
  import Wart._
  Warts.allBut(
    Nothing,
    //ImplicitConversion,
    Recursion,
    NonUnitStatements,
    //MutableDataStructures,
    StringPlusAny
  )
}

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
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    libraryDependencies in (Compile, compile) += splain,
    libraryDependencies in (Compile, console) += splain,
    addCompilerPlugin(kindProjector),
    scalafmtOnCompile := true
  )

/* Things maybe useful*/
lazy val toolbox =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("toolbox"))
    .settings(commonSettings: _*)
    .settings(name := "toolbox")

lazy val toolboxJS = toolbox.js
lazy val toolboxJVM = toolbox.jvm

/* Game Logic */
lazy val slimetrail =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("slimetrail"))
    .settings(commonSettings: _*)
    .settings(name := "slimetrail")
    .dependsOn(toolbox)

lazy val slimetrailJS = slimetrail.js
lazy val slimetrailJVM = slimetrail.jvm

// Text Interface
lazy val text =
  project
    .in(file("text"))
    .settings(commonSettings: _*)
    .settings(name := "slimetrail-text")
    .dependsOn(slimetrailJVM)

// Web Interface
lazy val web =
  project
    .in(file("web"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "slimetrail-web",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(slimetrailJS)