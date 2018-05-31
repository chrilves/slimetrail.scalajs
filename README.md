# PSUG #87 - Workshop ScalaJS

Bienvenue à la 87ième session du [Paris Scala User Group](https://www.meetup.com/fr-FR/Paris-Scala-User-Group-PSUG/events/251045516/). Le thème de la soirée est le développement d'application web en Scala avec [ScalaJS](https://www.scala-js.org/).

Je vous propose de réaliser le jeu [Slimetrail](http://www.di.fc.ul.pt/~jpn/gv/slimetrail.htm). Une démo du rendu final est disponible ici: https://chrilves.github.io/slimetrail/index.html . Les règles du jeu sont très simples:

* Il se joue à deux joueurs.euses, que nous appellerons **joueur.euse vert.e et jaune**.
* Le plateau de jeu est une grille hexagonale
* Sur une de ces cases est déposé un **pion**, représenté par la couleur **rouge**.
* Le but du/de la joueur.euse **vert.e** est d'amener le pion sur la **case verte** (la plus **en haut** de la grille).
* Le but du/de la joueur.euse **jaune** est d'amener le pion sur la **case jaune** (la plus **en bas** de la grille).
* Pour ce faire, chaque joueur.euse joue tour à tour, en commençant par le.la joueur.euse vert.e.
* A son tour un.e joueur.euse peut déplacer le pion vers une case adjacente à condition que:
  * le pion n'ai jamais été sur cette case.
  * il est toujours possible pour au moins un.e des joueurs.esues d'atteindre son objectif. Autrement dit il   n'y a pas de match nul.
* Si le pion atteind la case verte (la plus en haut de la grille) alors le.la joueur.euse vert.e est déclaré.e vainqueur.
* Si le pion atteind la case jaune (la plus en bas de la grille) alors le.la joueur.euse jaune est déclaré.e vainqueur.

Pour aider les joeurs.euses, à chaque tour, les coups autorisés sont indiqué par des cases de la couleur du/de la joueur.euse à qui c'est le tour. Je vous invite à vous mettre en binôme et faire quelques parties pour comprendre le jeu.

Si vous aimez ce genre de jeux, je vous recommande le livre [Mathematical Games, Abstract Games](http://store.doverpublications.com/0486499901.html) par *Joao Pedro Neto* et *Jorge Nuno Silva*.

## Mise en Place

La branche git `PSUG` contient une implémentation du jeu avec une interface texte. Cette branche est notre point de départ. Je vous invite à cloner le projet dans cette branche et lancer l'interface texte avec la commande sbt `texte/run` :

```sh
git clone https://github.com/chrilves/slimetrail.scalajs.git -b PSUG
cd slimetrail.scalajs
sbt texte/run
```

Cette branche implémente déjà toute la logique du jeu. Le code est séparé en 3 sous-projets:
* Dans **outils** vous trouverez diverses choses qui peuvent vous être utiles comme une implémentation de points en 2 dimensions ou un algorithme de différenciation de séquences.
* **slimetrail** contient toute la logique du jeu: ce qu'est une partie, un coup, etc. Tout ce que vous avez besoin pour gérer le déroulement d'une partie se trouve là.
* **texte** est l'interface texte.

**L'un des objecitfs de cette session est de vous montrer à quel point il est aisé de développer en ScalaJS avec du code Scala standard préexistant. A cette fin je vous demande de ne pas modifier le code contenu dans ces 3 sous-projets. Considérez ces modules comme des bibliothèque que vous êtes invités a utiliser et lire mais pas à modifier.**

Je vous invite également à génerer la documentation des sous-projets **outils** et **slimetrail** avec les commandes sbt `outils/doc` `slimetrail/doc`. Je vous recommande aussi fortement d'ouvrir la documentation des [API standard de ScalaJS](https://www.scala-js.org/api/scalajs-library/latest/#scala.scalajs.js.package) et les [API de manipulation du DOM de ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/#org.scalajs.dom.package). La documentation [MDN de Mozilla](https://developer.mozilla.org/fr/) est également une grande source d'informations que je vous invite à ouvrir également.

### 1. Amener ScalaJS dans le projet

A ce stade vous devez avoir le projet sur la branche `PSUG` et 3 onglets de vôtre navigateur ouverts sur les 3 pages recommandées plus haut. Je recommande également vivement utiliser [Firefox](https://www.mozilla.org/fr/firefox/) ou [Chromium/Chrome](https://www.google.fr/chrome/index.html). Cette session a été testé avec des versions à jour de ces deux navigateurs. Je ne garanti pas le fonctionnement sur autre chose.

Pour amener *ScalaJS* dans un projet, la première chose à faire est d'ajouter les plugins *sbt* [sbt-crossproject](https://github.com/portable-scala/sbt-crossproject) et [sbt-scalajs](http://www.scala-js.org/doc/project/). Le premier permet de compiler un projet pour différentes cibles (ici *JVM* et *ScalaJS*), le second fournit la cible *ScalaJS*.

Ajoutez ces deux lignes au fichier `project/plugins.sbt`:

```scala
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.4.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.23")
```

Puis rechargez la configuration sbt.

### 2. Cross compiler un projet

*sbt-crossproject* offre différentes options pour la cross compilation comme indiqué [ici](https://github.com/portable-scala/sbt-crossproject#configuration). Cross compiler pour différentes
plateformes est très simple il suffit de remplacer dans la définition d'un project `project` par
`crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)`. Par example, le projet

```scala
lazy val outils =
  project
    .in(file("outils"))
```
devient
```scala
lazy val outils =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("outils"))

lazy val outilsJS = outils.js
lazy val outilsJVM = outils.jvm
```

Ici `CrossType.Pure` indique que les sources sont les mêmes sur les deux plateforme. `outils` devient
un projet "générique" alors que `outilsJS` et `outilsJVM` sont spécialisés à leur plateforme respective.

Il est aussi nécessaire de rajouter dans `build.sbt` la ligne suivante:

```scala
// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}
```

**Modifiez `build.sbt` pour cross compiler les projets `outils` et `slimetrail`.**

### 3. Nouveau projet pour l'interface web

Créer un projet pour *ScalaJS* est aisé. Il suffit d'ajouter au projet le plugin sbt **ScalaJSPlugin**:

```scala
// Interface web
lazy val web =
  project
    .in(file("web"))
    .enablePlugins(ScalaJSPlugin)
    .settings(settingsGlobaux: _*)
    .settings(
      name := "slimetrail-web",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(slimetrailJS)
```
et de créer le fichier `web/src/main/scala/Main.scala`:
```scala
package slimetrail.web

object Main {
  def main(args: Array[String]): Unit =
    println("Hello world!")
}
```

Lancez la commande `web/fastOptJS` pour créer le fichier compilé `web/target/scala-2.12/slimetrail-web-fastopt.js` que vous pouvez soit exécuter avec node
```sh
node web/target/scala-2.12/slimetrail-web-fastopt.js
```
ou executer dans le navigateur
```sh
firefox fast.html
```

### 4. Jouer avec le DOM

A partir de maintenant il est indispensable que vous ayez au moins ces 5 onglets ouverts:
* [fast.html](./fast.html)
* [Démo Slimetrail](https://chrilves.github.io/slimetrail/index.html)
* [API standard ScalaJS](https://www.scala-js.org/api/scalajs-library/latest/#scala.scalajs.js.package)
* [API de manipulation du DOM de ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/#org.scalajs.dom.package)
* [Documentation MDN de Mozilla](https://developer.mozilla.org/fr/)

Pour les deux premières, activez les outis de développement (voir https://developer.mozilla.org/fr/docs/Outils). L'inspecteur vous permet d'inspecter (et oui!) le code HTML, SVG et CSS de la page en temps réel (aussi appelé le DOM). La console Web est un [REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop) javascript. Ces deux outils sont précieux, utilisez les.

Nous utiliserons essentiellement les fonctionnalitées suivantes:

* [document](https://developer.mozilla.org/fr/docs/Web/API/Document): représente une page [org.scalajs.dom.document](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.package@document:org.scalajs.dom.html.Document).
* [Element](https://developer.mozilla.org/fr/docs/Web/API/Element): un type de noeud HTML/SVG [org.scalajs.dom.raw.Elelement](https://www.scala-js.org/api/scalajs-dom/0.9.5/#org.scalajs.dom.raw.Element).
* [Node](https://developer.mozilla.org/fr/docs/Web/API/Node): un noeud HTML/SVG au sens large [org.scalajs.dom.raw.Node](https://www.scala-js.org/api/scalajs-dom/0.9.5/#org.scalajs.dom.raw.Node).
* [document.getElementById](https://developer.mozilla.org/fr/docs/Web/API/Document/getElementById): récupère (**s'il existe!**) le noeud du DOM ayant cet *id* (attribut de nom `id`) [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.Document@getElementById(elementId:String):org.scalajs.dom.raw.Element).
* [document.createTextNode](https://developer.mozilla.org/fr/docs/Web/API/Document/createTextNode): crée un noeud texte [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.HTMLDocument@createTextNode(data:String):org.scalajs.dom.raw.Text).
* [document.createElementNS](https://developer.mozilla.org/fr/docs/Web/API/Document/createElementNS): crée un noeud de type `Element` [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.HTMLDocument@createElementNS(namespaceURI:String,qualifiedName:String):org.scalajs.dom.raw.Element). Le namespace est important! Il indique au navigateur la sémantique à donner au noeud:
  * `"http://www.w3.org/1999/xhtml"` pour un noeud **HTML**.
  * `"http://www.w3.org/2000/svg"` pour un noeud **SVG**.
* [_.setAttributeNS](https://developer.mozilla.org/fr/docs/Web/API/Element/setAttributeNS): définit un attribut sur un noeud `Element` [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.Element@setAttributeNS(namespaceURI:String,qualifiedName:String,value:String):Unit). Encore une fois le namespace est important, sa valeur dépend de l'attribut. Il est souvent `undefined` (`null` en *ScalaJS*).
* [_.parentNode](https://developer.mozilla.org/fr/docs/Web/API/Node/parentNode): renvoie le noeud parent (**si il existe!**) [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.Node@parentNode:org.scalajs.dom.raw.Node).
* [node.replaceChild](https://developer.mozilla.org/fr/docs/Web/API/Node/replaceChild): replace un des enfants du noeud [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.Node@replaceChild(newChild:org.scalajs.dom.raw.Node,oldChild:org.scalajs.dom.raw.Node):org.scalajs.dom.raw.Node).
* [_.addEventListener](https://developer.mozilla.org/fr/docs/Web/API/EventTarget/addEventListener): ajoute une fonction de réponse à un évènement [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.Node@addEventListener[T%3C:org.scalajs.dom.raw.Event](type:String,listener:scala.scalajs.js.Function1[T,_],useCapture:Boolean):Unit).
* [_.stopPropagation()](https://developer.mozilla.org/fr/docs/Web/API/Event/stopPropagation): évite que cet évènement de remonte dans la [phase de bulle](https://developer.mozilla.org/fr/Apprendre/JavaScript/Building_blocks/Ev%C3%A8nements) "Event bubbling and capture" [ScalaJS](https://www.scala-js.org/api/scalajs-dom/0.9.5/index.html#org.scalajs.dom.raw.Event@stopPropagation():Unit).

## DOM Virtuel

Une page HTML n'est rien d'autre qu'un arbre dont les feuilles sont les "noeuds texte" du DOM crées avec [document.createTextNode](https://developer.mozilla.org/fr/docs/Web/API/Document/createTextNode) et les noeuds de l'arbre sont les "noeuds `Element`" comme `<body ...>`, `<h1 ...>`, `<svg ...>`, ... crées avec [document.createElementNS](https://developer.mozilla.org/fr/docs/Web/API/Document/createElementNS) et [_.setAttributeNS](https://developer.mozilla.org/fr/docs/Web/API/Element/setAttributeNS).

1. **Définissez un type, que vous nommerez `Html`, pour représenter de manière abstraite (sans faire référence à un type de `org.scalajs`!) un arbre HTML. Inventez quelques exemples de tels arbres `Html`.**
2. **Implémentez une méthode `dessiner` qui crée un noeud du DOM (de type `Node`) à partir d'un tel arbre. Servez vous en pour afficher dans le navigateurs les exemples ci-dessus.**

Utiliser le DOM pour afficher une page statique c'est bien, mais quelques peu dépassé. Chaque noeud du DOM à la possibilité de réagir à des [évènements](https://developer.mozilla.org/fr/docs/Web/Events) comme un clic, une touche pressée au clavier, etc. Cela se fait en utilisant la méthode [_.addEventListener](https://developer.mozilla.org/fr/docs/Web/API/EventTarget/addEventListener). Par exemple pour exécuter une action une fois que la page est chargée par le navigateur:

```scala
  def auChargement(a: => Unit): Unit =
    dom.document.addEventListener("DOMContentLoaded", (_: Event) => a)
```

On appellera les arguments passées a `addEventListener` la **réaction**, c'est à dire la paire formée du nom de l'évènement et de la fonction. Le type de la réaction est le type de retour de la fonction.

3. **Modifier le type `Html` en `Html[+A]` afin de pouvoir définir pour chaque noeud d'élément (non texte) des réactions de type `A`.**
4. **Modifiez la méthode `dessiner` afin d'ajouter les réaction aux noeuds du DOM.**

## L'Architecture [Elm](http://elm-lang.org/)

L'architecture d'[Elm](http://elm-lang.org/) est un modèle de conception d'interface reposant sur 5 choses:

* Le **Modèle** est un type de donnée servant à représenter, à chaque instant, l'état *complet* de l'application. Pour nôtre application le modèle est le type `Partie` qui représente l'état courant d'une partie.
* Le **modèle initial** est l'état initial de l'application.
* Les  **Messages** sont les évènements qui peuvent se produire. Ici un message est soit un coup joué, soit une demande de nouvelle partie. Chaque message entraîne une mise à jour éventuelle du modèle. Par exemple, jouer un coup fera avancer la partie, et démarrer une nouvelle partie la remettra à zéro.
* La fonction **miseAJour** calcule, en fonction du modèle courant et d'un message, le nouvel état de l'application (c'est à dire le nouveau modèle).
* Finalement la **vue** calcule l'affichage de l'application en fonction du modèle courant. Chaque message entraîne un nouveau modèle courant et donc un calcul de la vue associée a ce nouveau modèle.

**Les types `Model` et `Msg` ainsi que le modèle initial et la fonction `miseAJour` sont déjà définis dans la classe `slimetrail.AppSlimetrail`. L'objectif ce cette section est de réaliser le pendant web de la classe `slimetrail.texte.AppliTexteSlimetrail` que l'on appellera `slimetrail.web.AppliWebSlimetrail`.**

*Notez que la classe `slimetrail.texte.AppliTexteSlimetrail` se contente de fournir la vue textuelle.*

Une application web doit implémenter le trait suivant:

```scala
package slimetrail.web

import slimetrail._

trait ApplicationWebElm extends ApplicationElm { self =>
  /** La vue renvoie un arbre Html*/
  def vue(model: Model): slimetrail.web.html.Html

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final def executer(noeudInitial: Node): Unit = ???
}
```

3. **Implémentez la méthode `executer`**.

L'application web Slimetrail peut alors se définir comme la classe suivante:

```scala
package slimetrail.web

import slimetrail._
import outils._

final class AppliWebSlimetrail(taille: Int)
    extends AppSlimetrail(taille)
    with ApplicationWebElm {

  def vue(m: Partie): Html[Msg] = ???
}
```

4. **Implémentez la méthode `vue`. Vous pouvez vous référer au fichier [plateau.svg](plateau.svg).**.

## Conclusion

J'espère que vous vous êtes bien amusé.