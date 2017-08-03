package me.samei.example

import me.samei.std._
import Convertors._

object BuildInfo extends Task {
	override type In = Boolean
	override def props = Task.Props("buildinfo", "[-debug BOOL]")
	override def extract = { implicit ctx => for {
		debug <- optional[Boolean]("debug")
	} yield debug getOrElse false }
	override def run = func { implicit ctx => { boolean => 
		println("Version 1.0.0")
		successful("BuildInfo ... Done!")
	}}
}

object Example extends CliLauncher("Hello", BuildInfo :: Nil)
