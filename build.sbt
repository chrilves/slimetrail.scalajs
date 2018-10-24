// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

// voir http://www.wartremover.org/
lazy val warts =
  Warts.allBut(
    Wart.Nothing,
    Wart.ImplicitConversion,
    Wart.Recursion,
    Wart.NonUnitStatements,
    Wart.MutableDataStructures
  )

lazy val settingsGlobaux: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "chrilves",
        scalaVersion := "2.12.7",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
    scalacOptions in (Compile, console) -= "-Xfatal-warnings",
    scalacOptions -= "-Ywarn-unused:params",
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    addCompilerPlugin("io.tryp" % "splain" % "0.3.4" cross CrossVersion.patch),
    addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.8" cross CrossVersion.binary),
    scalafmtOnCompile := true
  )

/* Diverses choses qui peuvent être utiles
 * comme des algorithmes de diff
 */
lazy val toolbox =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("toolbox"))
    .settings(settingsGlobaux: _*)
    .settings(name := "toolbox")

lazy val toolboxJS = toolbox.js
lazy val toolboxJVM = toolbox.jvm

/* Implémentation de la logique du jeu
 */
lazy val slimetrail =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("slimetrail"))
    .settings(settingsGlobaux: _*)
    .settings(name := "slimetrail")
    .dependsOn(toolbox)

lazy val slimetrailJS = slimetrail.js
lazy val slimetrailJVM = slimetrail.jvm

// Interface web
lazy val web =
  project
    .in(file("web"))
    .enablePlugins(ScalaJSPlugin)
    .settings(settingsGlobaux: _*)
    .settings(
      name := "slimetrail-web",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(slimetrailJS)

// Interface texte
lazy val text =
  project
    .in(file("text"))
    .settings(settingsGlobaux: _*)
    .settings(name := "slimetrail-text")
    .dependsOn(slimetrailJVM)