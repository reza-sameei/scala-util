# std
Utilities for Scala

### Import
Currently you need to clone the project and publish it locally on your target:

```
sbt "+ publish-local"
```

Then:

```
libraryDependencies += "me.samei" %% "scala-util" % "1.0.0-SNAPSHOT"
```

### CliLauncher & Task
```scala
package com.example

import me.samei.std._
import Convertors._
import me.samei.cli._

class Hello(name: String) extends Task {

    def props = Task.Props(name, "-name STRING [-debug true]")

    type In = (String, Option[Boolean])

    def extract = { implicit ctx => for {
        name <- required[String]("name")
        debug <- optional[Boolean]("debug")
    } yield (name, debug) }

    def run = func { implicit ctx => {
        case (name, _) =>
            println(s"Hello ${name}!")
            successful("Done")
    }}
}


object Main extends CliLauncher("main", new Hello("say-hello") :: Nil)

```
You can call your tasks:
```
sbt> run say-hello
[info] Undefined key: 'name'

sbt> run say-hello -name
[info] Missed value for key: 'name'

sbt> run say-hello -name Reza
[info] Hello Reza!
```


### Result & AsyncResult

### ValueExtractor

### Convertor
