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

lazy val commonSettings: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "chrilves",
        scalaVersion := "3.1.3",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    scalacOptions /*in (Compile, console)*/ -= "-Xfatal-warnings",
    Compile/compile/wartremoverErrors := warts,
    Compile/console/wartremoverErrors := warts,
    scalafmtOnCompile := true
  )

/* Things maybe useful*/
lazy val toolbox =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("toolbox"))
    .settings(commonSettings: _*)
    .settings(name := "toolbox")
    .jsSettings(scalacOptions += "-scalajs")

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
    .jsSettings(scalacOptions += "-scalajs")

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
      scalacOptions += "-scalajs",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(slimetrailJS)