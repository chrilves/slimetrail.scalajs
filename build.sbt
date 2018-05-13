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


val optionsScalacDePrudence =
  Seq(
    "-deprecation",
    "-encoding",
    "UTF8",
    "-explaintypes",
    "-feature",
    "-language:-dynamics",
    "-language:postfixOps",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:experimental.macros",
    "-unchecked",
    "-Xlint:_",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-macros:both",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-self-implicit",
    "-Ywarn-unused:_",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  )


val settingsGlobaux: Seq[sbt.Def.SettingsDefinition] =
  Seq(
    inThisBuild(
      List(
        organization := "chrilves",
        scalaVersion := "2.12.6",
        version := "0.1.0-SNAPSHOT"
      )),
    updateOptions := updateOptions.value.withCachedResolution(true),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
    scalacOptions ++= optionsScalacDePrudence,
    wartremoverErrors in (Compile, compile) := warts,
    wartremoverWarnings in (Compile, console) := warts,
    addCompilerPlugin("io.tryp" % "splain" % "0.3.1" cross CrossVersion.patch),
    scalafmtOnCompile := true,    
  )


/* Diverses choses qui peuvent être utiles
 * comme des algorithmes de diff
 */
lazy val outils =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("outils"))
    .settings(settingsGlobaux :_*)
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
lazy val root =
  project
    .in(file("web"))
    .settings(settingsGlobaux:_*)
    .settings(
      name := "web",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      scalaJSUseMainModuleInitializer := true
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(slimetrailJS)