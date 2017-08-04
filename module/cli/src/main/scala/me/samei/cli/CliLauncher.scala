package me.samei.cli

import me.samei.std._
import Convertors._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class CliLauncher(name: String, tasks: Seq[Task]) {

    def main(args: Array[String]): Unit = CliLauncher(name)(tasks).runBy(args)

}

object CliLauncher {

    def apply(
        name: String,
        executionContext: ExecutionContext = ExecutionContext.global
    )(
        tasks : => Seq[Task]
    ) = new ContextBuilder(name, executionContext, tasks)

    class ContextBuilder(
        val name: String,
        val executionContext: ExecutionContext,
        val tasks: Seq[Task]
    ) {
        def build(extractor: ArgumentExtractor) = new Context(name, extractor, executionContext, tasks)

        def resultBy(extractor: ArgumentExtractor) = new Context(
            name, extractor, executionContext, tasks
        ).result

        def resultBy(args: Iterable[String]) = new Context(
            name,
            new PosixArgExtractor(s"${name}.posix-args-extractor", args.toList),
            executionContext, tasks
        ).result

        def runBy(extractor: ArgumentExtractor) = new Context(
            name, extractor, executionContext, tasks
        ).run

        def runBy(args: Iterable[String]) = new Context(
            name,
            new PosixArgExtractor(s"${name}.posix-args-extractor", args.toList),
            executionContext,
            tasks
        ).run
    }

    class Context(
        override val name: String,
        override val extractor: ArgumentExtractor,
        implicit override val executionContext: ExecutionContext,
        val tasks: Seq[Task]
    ) extends LauncherContext with Module {

        override val logger = loadLogger

        logger.info("Init")

        private def indexTasks(
            tasks: Seq[Task]
        ): AsyncResult[Task.Result.Unsuccessful, Map[String, Task]] = AsyncResult lazyAsync {
            val buf = scala.collection.mutable.HashMap.empty[String, Task]
            val duplicated = scala.collection.mutable.ListBuffer.empty[(Task,Task)]
            val index = tasks.foldLeft(buf){ (buf, i) =>
                val props = i.props
                if (buf contains props.key) {
                    val first = buf(props.key)
                    duplicated append ((first, i))
                    logger.error(s"Init, Duplicated Task Key: ${props.key}, ${first}, ${i}")
                    buf
                } else {
                    logger.debug(s"Init, Add Task, ${props}, ${i}")
                    buf(props.key) = i
                    buf
                }
            }.toMap
            if (duplicated.nonEmpty) {
                throw new RuntimeException(s"Duplicated Task Keys:\n${duplicated.mkString("\n")}")
            } else Result value index
        }

        private def pickTask (
            args: ArgumentExtractor, tasks: Map[String, Task]
        ): AsyncResult[Task.Result.Unsuccessful, Task] = AsyncResult async {
            args.firstOption[String] errorTransform { err =>
                Result error Task.Result.usageError(s"Error in task name: ${err.desc}")
            } flatMap {
                case Some(name) if tasks contains name =>
                    Result value tasks(name)
                case Some(name) =>
                    Result error Task.Result.usageError(s"Undefined Task: ${name}")
                case None =>
                    Result error Task.Result.usageError("Not Specified Task!")
            }
        }


        private def extractTaskArgs (
            args: ArgumentExtractor,
            task: Task
        ): AsyncResult[Task.Result.Unsuccessful, Any] = AsyncResult async {
            (task.extract apply flat) errorTransform { err =>
                logger.error(s"Error in extraction, ${err}")
                Result error Task.Result.usageError(err.desc)
            }
        }

        private def runTask (
            ctx: LauncherContext.Flat,
            task: Task,
            args: Any
        ) =
            AsyncResult lazyApply {
                (task.run(ctx)(args.asInstanceOf[task.In])).map {
                    case done: Task.Result.Successful => Result value done
                    case error: Task.Result.Unsuccessful => Result error error
                }
            }

        private def exit (code: Int) = {
            sys.runtime.halt(code)
            ??? // Never
        }

        def result(): AsyncResult[Task.Result.Unsuccessful, Task.Result.Successful] = for {
            index <- indexTasks(tasks)
            task <- pickTask(extractor, index)
            args <- extractTaskArgs(extractor, task)
            rsl <- runTask(flat, task, args)
        } yield rsl

        def run(): Unit = result.sync(Duration.Inf) match {
            case Result.Value(Task.Result.Successful(msg)) =>
                logger.info(s"Successful: ${msg}")
                exit(Task.Result.Done)
            case Result.Error(Task.Result.Unsuccessful(code, message, cause)) =>
                logger.error(s"Error: ${message}", cause)
                println(message)
                exit(code)
            case Result.Failure(cause) =>
                val msg = s"Failure: ${cause.getMessage}"
                logger.error(msg, cause)
                println(msg)
                cause.printStackTrace()
                exit(Task.Result.Error)
        }
    }
}

