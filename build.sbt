// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

// voir http://www.wartremover.org/
lazy val warts = {
  import Wart._
  Warts.allBut(
    Nothing,
    ImplicitConversion,
    Recursion,
    NonUnitStatements,
    MutableDataStructures,
    StringPlusAny
  )
}
  

lazy val settingsGlobaux: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "chrilves",
        scalaVersion := "2.13.0",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.8" % Test,
    scalacOptions -= "-Xfatal-warnings",
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    addCompilerPlugin("io.tryp" % "splain" % "0.4.1" cross CrossVersion.patch),
    scalafmtOnCompile := true
  )

/* Diverses choses qui peuvent être utiles
 * comme des algorithmes de diff
 */
lazy val outils =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("outils"))
    .settings(settingsGlobaux: _*)
    .settings(name := "outils")

lazy val outilsJS = outils.js
lazy val outilsJVM = outils.jvm

/* Implémentation de la logique du jeu
 */
lazy val slimetrail =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("slimetrail"))
    .settings(settingsGlobaux: _*)
    .settings(name := "slimetrail")
    .dependsOn(outils)

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
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.7",
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(slimetrailJS)

// Interface texte
lazy val texte =
  project
    .in(file("texte"))
    .settings(settingsGlobaux: _*)
    .settings(name := "slimetrail-texte")
    .dependsOn(slimetrailJVM)