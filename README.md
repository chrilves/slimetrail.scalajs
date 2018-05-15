# slimetrail.scalajs
Slimetrail game in ScalaJS (http://www.di.fc.ul.pt/~jpn/gv/slimetrail.htm)

## Application Web

[Demo](https://chrilves.github.io/slimetrail/index.html)

* Optimisations légères:
```
 ./bin/genHtml.sh && sbt web/fullOptJS && firefox full.html
```
* Optimisations fortes:
```
./bin/genHtml.sh && sbt web/fastOptJS && firefox fast.html
```
## Application texte (mode console)

```
sbt texte/run
```
