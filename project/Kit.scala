package me.samei.sbt

import sbt.Keys.{fork, _}
import sbt._

object key {
    val jvmTarget = SettingKey[String]("jvmTarget")
}

case class DefContext(
    org: String,
    compiler: String = "2.12.3",
    jvm: String = "1.8",
    singleVersion: Option[String] = None,
    debug: Boolean = false
) {
    protected def compilerOptions() = {
        var settings = Seq(
            "-feature",
            "-deprecation",
            "-language:postfixOps",
            "-language:implicitConversions",
            s"-target:jvm-${jvm}",
            // https://tpolecat.github.io/2014/04/11/scalac-flags.html
            "-encoding", "UTF-8",
            // "-Xfatal-warnings",
            "-Xlint",
            "-Yno-adapted-args",
            "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
            "-Ywarn-numeric-widen",
            "-Ywarn-value-discard",
            "-Xfuture",
            "-Ywarn-unused-import"
        )
        settings = if (debug) settings ++ Seq("-Ylog-classpath") else settings
        settings // return
    }

    def toSettings(): Def.SettingsDefinition = {
        var settings = Seq(
            organization := org,
            scalaVersion := "2.12.3",
            crossScalaVersions := Set("2.11.11", "2.12.3", compiler).toSeq,
            fork := true,
            scalacOptions ++= compilerOptions(),
            javacOptions ++= Seq("-source", jvm, "-target", jvm),
            key.jvmTarget := jvm
        )

        settings = singleVersion match {
            case None => settings
            case Some(value) => settings ++ Seq(sbt.Keys.version := value)
        }

        settings // return
    }
}

object Prj {

    def apply(
        name: String,
        context: DefContext,
        dir: Option[String] = None
    ): Project = {
        Project(
            name,
            file(s"module/${dir getOrElse name}")
        ).settings(
            context.toSettings:_*
        )
    }

    def root(
        name: String,
        context: DefContext
    ) = {
        Project(name, file("."))
            .settings(context.toSettings:_*)
    }
}