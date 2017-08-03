package me.samei.std

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import me.samei.std.Convertors._
import pre._

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
            new PosixArgExtractor(s"${name}.posix-argument-extractor", args.toList),
            executionContext, tasks
        ).result

        def runBy(extractor: ArgumentExtractor) = new Context(
            name, extractor, executionContext, tasks
        ).run

        def runBy(args: Iterable[String]) = new Context(
            name,
            new PosixArgExtractor(s"${name}.posix-argument-extractor", args.toList),
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

        override def logger = loadLogger

        val index = tasks.map(i => i.props.key -> i).toMap

        private[CliLauncher] def pickTask (
            args: ArgumentExtractor, tasks: Seq[Task]
        ): AsyncResult[Task.Result.Unsuccessful, Task] = AsyncResult async {
            args.firstOption[String] errorTransform { err =>
                Result error Task.Result.usageError(s"Error in task name: ${err.desc}")
            } flatMap {
                case Some(name) if index contains name =>
                    Result value index(name)
                case Some(name) =>
                    Result error Task.Result.usageError(s"Undefined Task: ${name}")
                case None =>
                    Result error Task.Result.usageError("Not Specified Task!")
            }
        }


        private[CliLauncher] def extractTaskArgs (
            args: ArgumentExtractor,
            task: Task
        ): AsyncResult[Task.Result.Unsuccessful, Any] = AsyncResult async {
            (task.extract apply flat) errorTransform { err =>
                logger.error(s"Error in extraction: ${err}")
                Result error Task.Result.usageError(s"Error in argument extraction: ${err}")
            }
        }

        private[CliLauncher] def runTask (
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

        private[CliLauncher] def exit (code: Int) = {
            sys.runtime.halt(code)
            ??? // Never
        }

        private def info(str: String): Unit = {
            logger.info(str)
            println(str)
        }

        private def err(str: String, cause: Option[Throwable] = None): Unit = {
            logger.error(str, cause.orNull)
            println(str)
            cause.foreach(_.printStackTrace())
        }

        def result(): AsyncResult[Task.Result.Unsuccessful, Task.Result.Successful] = for {
            task <- pickTask(extractor, tasks)
            args <- extractTaskArgs(extractor, task)
            rsl <- runTask(flat, task, args)
        } yield rsl

        def run(): Unit = result.sync(Duration.Inf) match {
            case Result.Value(Task.Result.Successful(msg)) =>
                info(msg)
                exit(Task.Result.Done)
            case Result.Error(Task.Result.Unsuccessful(code, message, cause)) =>
                err(message, cause)
                exit(code)
            case Result.Failure(cause) =>
                err("Failure", cause.some)
                exit(Task.Result.Error)
        }
    }
}

