package me.samei.cli.launcher

import com.typesafe.scalalogging.Logger
import me.samei.std._
import Convertors._
import me.samei.cli.Task
import org.scalatest.{FlatSpec, Matchers}
import me.samei.cli.TestKit
import me.samei.cli.CliLauncher

import scala.concurrent.duration._


class Suite extends FlatSpec with Matchers with TestKit {

    val logget = Logger apply classOf[Suite]

    it must "???" in {

        val httpup = new Task {

            override type In = (String, Int, Boolean)

            override def props = Task.Props("httpup", "-host HOST [-port INT] [-debug BOOL]")

            override def extract = { implicit ctx =>
                for {
                    host <- required[String]("host")
                    port <- optional[Int]("port")
                    debug <- optional[Boolean]("debug")
                } yield (host, port getOrElse 80, debug getOrElse false)
            }

            override def run = func { implicit ctx => {case (host, port, debug) =>
                ctx.logger.info(s"New Server: ${host}:${port} ${if(debug) "debug-mode" else ""}")
                successful("Done")
            } }
        }

        val buildinfo = new Task {

            override type In = Boolean

            override def props = Task.Props("buildinfo", "[-debug BOOL]")

            override def extract = { implicit ctx =>
                for {
                    debug <- optional[Boolean]("debug")
                } yield debug getOrElse false
            }

            override def run = func { implicit ctx => { case debug =>
                val msg = "Version 1.0.0-SNAPSHOT!"
                ctx.logger.info(msg)
                println(msg)
                throw new RuntimeException("Hello !")
            }}

        }

        val launcher = CliLauncher("test")(httpup :: buildinfo :: Nil)

        launcher.resultBy(Nil)
            .sync(1 second) matchError {
                case Task.Result.Unsuccessful(code, msg, cause) =>
                    info(msg)
                    code shouldEqual Task.Result.Usage
            }

        launcher.resultBy("runit" :: "-host" :: "localhost" :: Nil)
            .sync(1 second) matchError {
                case Task.Result.Unsuccessful(code, msg, cause) =>
                info(msg)
                code shouldEqual Task.Result.Usage
            }

        launcher.resultBy("httpup" :: "-host" :: "localhost" :: Nil)
            .sync(1 second) matchValue {
            case Task.Result.Successful(msg) =>
                info(msg)
                succeed
        }

        launcher.resultBy("buildinfo" :: "-host" :: "localhost" :: Nil)
            .sync(1 second) onFailure { cause =>
                info(s"Cathed Exception: ${cause}")
                succeed
            }
    }

}
