package com.example

import me.samei.std.Convertors._
import me.samei.cli.{Task, CliLauncher}

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


object Main extends CliLauncher(
    "main",
    new Hello("say-hello") :: new Hello("say-hello") :: Nil
)
