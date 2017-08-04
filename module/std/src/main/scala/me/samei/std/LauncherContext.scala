package me.samei.std

import scala.concurrent.ExecutionContext
import com.typesafe.scalalogging.Logger

trait LauncherContext extends Module { self =>

    override def logger: Logger

    def extractor: ValueExtractor

    def executionContext: ExecutionContext

    final protected def flat() = new LauncherContext.Flat(self)
}

object LauncherContext {

    class Flat(ctx: LauncherContext)
        extends LauncherContext
            with ValueExtractor
            with ExecutionContext {

        override def execute (runnable: Runnable): Unit = ctx.executionContext.execute(runnable)

        override def reportFailure (cause: Throwable): Unit = ctx.executionContext.reportFailure(cause)

        override def required[T] (key: String)(implicit convertor: Convertor[String, T]): ValueExtractor.Result[T] = ctx.extractor.required(key)

        override def required[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[T] = ctx.extractor.required(namespace, fn)

        override def optional[T] (key: String)(implicit convertor: Convertor[String, T]): ValueExtractor.Result[Option[T]] = ctx.extractor.optional(key)

        override def optional[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[Option[T]] = ctx.extractor.optional(namespace, fn)

        override def nelist[T] (key: String)(implicit convertor: Convertor[String, T]): ValueExtractor.Result[List[T]] = ctx.extractor.nelist(key)

        override def nelist[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[List[T]] = ctx.extractor.nelist(namespace, fn)

        override def list[T] (key: String)(implicit convertor: Convertor[String, T]): ValueExtractor.Result[List[T]] = ctx.extractor.list(key)

        override def list[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[List[T]] = ctx.extractor.list(namespace, fn)

        override def extractor: ValueExtractor = ctx.extractor

        override def executionContext = ctx.executionContext

        override def logger = ctx.logger

        override def name = ctx.name

        override protected def loadLogger = ctx.logger
    }
}
