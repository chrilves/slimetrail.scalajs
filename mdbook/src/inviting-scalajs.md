# Inviting ScalaJS to the Party

For now *Slimetrail* is a plain old *JVM-only* project. We need to make it able to produce both *JVM* and *JavaScript* outputs: it will become a **cross-project**.

## Cross-projectization

**Add the following lines to** `project/plugins.sbt` **.**

```scala
// ScalaJS
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.25")
```

A large amount of *Scala* libraries are available both as *JVM* and *ScalaJS* artifacts. So will we! The *Text UI* needs the `toolbox` and `slimetrail` to be compiled for the *JVM* while the *Web UI* needs them to be compiled in `JavaScript`. *Cross-projects* can define `libraryDependencies` using `%%%` instead of `%%` to ensure the correct artifacts are fetched.

**In** `build.sbt`**, replace any** `%%` **in** `commonSettings` **by** `%%%` **.**

We now need to adapt the `toolbox` and `slimetrail` project definitions to define these as *cross-projects*. To do so:

- **In the** `toolbox`**, project definition, replace** `project` **by:**

  ```scala
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
  ```

- **After the** `toolbox`**, project definition, add:**

  ```scala
  lazy val toolboxJS = toolbox.js
  lazy val toolboxJVM = toolbox.jvm
  ```

- **In the** `slimetrail`**, project definition, replace** `project` **by:**

  ```scala
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
  ```

- **After the** `slimetrail`**, project definition, add:**

  ```scala
  lazy val slimetrailJS = slimetrail.js
  lazy val slimetrailJVM = slimetrail.jvm
  ```

- **In the** `text`**, project definition, replace** `slimetrailJVM` **by** `slimetrailJVM` **.**

`toolbox` and `slimetrail` projects now exists in two flavors each:

- `toolboxJVM` and `slimetrailJVM` are *JVM* projects.
- `toolboxJS` and `slimetrailJS` are *ScalaJS* projects.

**Run** `sbt text/run` **to ensure everything is still working fine.**

## Setting up the Web UI

**Add the new project definition for the Web UI:**

```scala
// Web Interface
lazy val web =
  project
    .in(file("web"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      name := "slimetrail-web",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(slimetrailJS)
```

**Create the file** `web/src/main/scala/Main.scala` **whose content is:**

```scala
package slimetrail.web

import slimetrail._
import org.scalajs._

/** Tree strucure to represent the DOM */
sealed trait Html[+A] {
  final def render: Node =
    ??? // Replace by actual code

  final def map[B](f: A => B): Html[B] =
    ??? // Replace by actual code
}
final case class ATextNode(value: String) extends Html[Nothing]
final case class AnElement[+A](
    namespace: String,
    tag: String,
    attributes: Map[(Option[String], String), String],
    eventListeners: List[(String, js.Function1[_ <: Event, A])],
    children: List[Html[A]]
  ) extends Html[A]

/** Generic Web Application */
trait WebApplication extends Application {

  def view(model: Model): Html[Msg]

  final def run(initialNode: Node): Unit =
    ??? // Replace by actual code
}

/** The Slimetrail Web Application */
final class SlimetrailWebApp(size: Int)
    extends SlimetrailApp(size)
    with WebApplication {

  def view(m: GameState): Html[Msg] =
    ??? // Replace by actual code
}

object Main {
  def onLoading(a: => Unit): Unit =
    dom.document.addEventListener("DOMContentLoaded", (_: Event) => a)

  def main(args: Array[String]): Unit =
    onLoading {
      new SlimetrailWebApp(10)
        .run(document.getElementById("scalajs-controlled-node"))
    }
}
```

The `WebApplication` trait is the exact *Web* counterpart of the `TextApplication` trait. The `view` method of `WebApplication`, just like in `TextApplication`, takes the current state of the application (`GameState` for *Slimetrail*) as input and produce events (`Action` for *Slimetrail*) that will be sent to `update` in order to compute the new state of the application and so on.

The two differences with `TextApplication` are:

- the *Web UI* needs to render some *HTML/SVG/CSS* in a browser page via the **DOM** instead of lines of text in a terminal console.
- the *Web UI* needs to produce user `Action` from [DOM Events](https://developer.mozilla.org/en-US/docs/Web/Events) instead of reading lines from the keyboard.

To produce the *JavaScript* you have two options:

- `sbt web/fastOptJS` will compile the `web` project into a `JavaScript` file at `web/target/scala-2.12/slimetrail-web-fastopt.js` fast but without much optimizations.
- `sbt web/fullOptJS` will compile the `web` project into an optimized `JavaScript` file at `web/target/scala-2.12/slimetrail-web-opt.js`.

Run `bin/genHtml.sh` to produce both `fast.html` and `full.html` which run the application using the corresponding *JavaScript* file.

## Implementing the Web UI

- **(Optional) Implements** `Html.map` **.**

  It applies the function `f` on any value of type `A`.

- **Implements** `Html.render` **.**

  It creates a new [Node](https://www.scala-js.org/api/scalajs-dom/0.9.5/#org.scalajs.dom.raw.Node) corresponding to this *HTML/SVG* tree using the *DOM*.

- **Implements** `WebApplication.run` **.**

  It runs a *Web Application* by replacing the `initialNode` by the rendering of the current state of the application.

- **Implements** `SlimetrailWebApp.view` **.**

  It renders the current state of the *Slimetrail* application as an `Html[Action]` tree.