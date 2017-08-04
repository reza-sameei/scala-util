package me.samei.std

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NonFatal

case class AsyncResult[+Err, +Val](
    underlay: Future[Result[Err, Val]]
)

object AsyncResult {

    def async[E,V](rsl: Result[E,V]): AsyncResult[E,V] = AsyncResult(scala.concurrent.Future.successful(rsl))
    def value[E,V](value: V): AsyncResult[E,V] = async(Result.Value(value))
    def error[E,V](error: E): AsyncResult[E,V] = async(Result.Error(error))
    def failure[E,V](cause: Throwable): AsyncResult[E,V] = async(Result.Failure(cause))

    def lazyAsync[E,V](rsl: => Result[E,V]): AsyncResult[E,V] = AsyncResult(scala.concurrent.Future.successful(rsl))
    def lazyValue[E,V](value: => V): AsyncResult[E,V] = lazyAsync(Result.Value(value))
    def lazyError[E,V](error: => E): AsyncResult[E,V] = lazyAsync(Result.Error(error))
    def lazyFailure[E,V](cause: => Throwable): AsyncResult[E,V] = lazyAsync(Result.Failure(cause))

    def lazyApply[E,V](fn: => Future[Result[E,V]]): AsyncResult[E,V] =
        try apply(fn) catch {
            case cause: Throwable => failure(cause)
        }

    implicit class Operations[E,V](val self: AsyncResult[E,V]) extends AnyVal {

        def transform[E2,V2](
            fn: Result[E,V] => AsyncResult[E2,V2]
        )(
            implicit ec: ExecutionContext
        ): AsyncResult[E2,V2] = AsyncResult {
            val promise = scala.concurrent.Promise[Result[E2,V2]]

            self.underlay.onComplete {

                case scala.util.Success(rsl) =>
                    try {
                        promise completeWith fn(rsl).underlay
                    } catch {
                        case NonFatal(cause) => promise success Result.Failure(cause)
                    }

                case scala.util.Failure(cause) =>
                    try {
                        val rsl = Result.Failure(cause)
                        promise completeWith fn(rsl).underlay
                    } catch {
                        case NonFatal(cause) => promise success Result.Failure(cause)
                    }
            }

            promise.future
        }

        def flatMap[V2](
            fn: V => AsyncResult[E,V2]
        )(
            implicit ec: ExecutionContext
        ): AsyncResult[E,V2] = AsyncResult {
            val promise = scala.concurrent.Promise[Result[E,V2]]

            self.underlay.onComplete {

                case scala.util.Success(v) => v match {
                    case Result.Value(value) =>
                        try {
                            promise completeWith fn(value).underlay
                        } catch {
                            case NonFatal(cause) => promise success Result.Failure(cause)
                        }
                    case Result.Error(error) => promise success Result.Error(error)
                    case Result.Failure(cause) => promise success Result.Failure(cause)
                }

                case scala.util.Failure(cause) => promise success Result.Failure(cause)
            }

            promise.future
        }


        def map[V2](
            fn: V => V2
        )(
            implicit ec: ExecutionContext
        ): AsyncResult[E,V2] = AsyncResult {
            val promise = scala.concurrent.Promise[Result[E,V2]]

            self.underlay.onComplete {

                case scala.util.Success(v) => v match {
                    case Result.Value(value) =>
                        try {
                            promise success Result.Value(fn(value))
                        } catch {
                            case NonFatal(cause) => promise success Result.Failure(cause)
                        }
                    case Result.Error(error) => promise success Result.Error(error)
                    case Result.Failure(cause) => promise success Result.Failure(cause)
                }

                case scala.util.Failure(cause) => promise success Result.Failure(cause)
            }

            promise.future
        }

        def sync(timeout: Duration): Result[E,V] = try {
            Await.result(self.underlay, timeout)
        } catch {
            case cause: Throwable => Result failure cause
        }
    }

}
