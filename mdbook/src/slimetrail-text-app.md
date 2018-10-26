# The Slimetrail Text App

A text interface similar to the web one you used to far has been realized. To get it clone the following [git](https://git-scm.com/) repository:

```sh
git clone https://github.com/chrilves/slimetrail.scalajs.git -b ScalaIO.2018
cd slimetrail.scalajs
sbt text/run
```

You should see something like this:

![Text User Interface](./images/tui.png "Text User Interface")

The project is divided into three *sbt modules*:

- `text` implements the text user interface.
- `slimetrail` implements the game's logic.
- `toolbox` defines various useful things.

**Do not modify any file under the `toolbox`, `slimetrail` and `text` directories!!!**

Note that `text` does not include any game logic and `slimetail` does not include any interface code. This strict isolation is the key enabling sharing `slimetail` between the *Text UI* and *Web UI*. 
