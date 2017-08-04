package me.samei.std

import scala.util.{Try, Success}
import scala.util.control.NonFatal

sealed trait Result[+Err, +Val]

object Result {

    case class Value[+Err, +Val](value: Val) extends Result[Err, Val]

    case class Error[+Err, +Val](error: Err) extends Result[Err, Val]

    case class Failure[+Err, +Val](cause: Throwable) extends Result[Err, Val]

    def value[E,V](value: V): Result[E,V] = Value(value)

    def lazyValue[E,V](value: => V): Result[E,V] =
        try Value(value) catch {
            case NonFatal(cause) => Failure[E,V](cause)
        }

    def error[E,V](error: E): Result[E,V] = Error(error)

    def lazyError[E,V](error: => E): Result[E,V] =
        try Error(error) catch {
            case NonFatal(cause) => Failure[E,V](cause)
        }

    def failure[E,V](cause: Throwable): Result[E,V] = Failure(cause)

    def lazyFailure[E,V](cause: => Throwable): Result[E,V] =
        try Failure(cause) catch {
            case NonFatal(cause) => Failure(cause)
        }

    def fromEither[E,V](either: Either[E,V]) = either match {
        case Right(value) => Value(value)
        case Left(error) => Error(error)
    }

    def fromTry[E,V](t: Try[V]) = t match {
        case Success(value) => Value(value)
        case scala.util.Failure(cause) => Failure(cause)
    }

    implicit class Operations[E,V](val self: Result[E,V]) extends AnyVal {

        def transform[E2,V2](
            fn: Result[E,V] => Result[E2,V2]
        ): Result[E2,V2] = try fn(self) catch {
            case NonFatal(cause) => Failure[E2,V2](cause)
        }

        def valueTransform[V2](
            fn: V => Result[E,V2]
        ): Result[E, V2] = try self match {
            case Value(value) => fn(value)
            case Error(err) => Error[E,V2](err)
            case Failure(cause) => Failure[E,V2](cause)
        } catch {
            case NonFatal(cause) => Failure[E,V2](cause)
        }

        def valueMap[V2](fn: V => V2): Result[E,V2] = try self match {
            case Value(value) => Value(fn(value))
            case Error(err) => Error[E,V2](err)
            case Failure(cause) => Failure[E,V2](cause)
        } catch {
            case NonFatal(cause) => Failure[E,V2](cause)
        }

        def errorTransform[E2](
            fn: E => Result[E2,V]
        ): Result[E2,V] = try self match {
            case Value(value) => Value[E2,V](value)
            case Error(err) => fn(err)
            case Failure(cause) => Failure[E2,V](cause)
        } catch {
            case NonFatal(cause) => Failure[E2,V](cause)
        }

        def errorMap[E2](fn: E => E2): Result[E2,V] = try self match {
            case Value(value) => Value[E2,V](value)
            case Error(err) => Error(fn(err))
            case Failure(cause) => Failure[E2,V](cause)
        } catch {
            case NonFatal(cause) => Failure[E2,V](cause)
        }

        def failureTransform(fn: Throwable => Result[E,V]): Result[E,V] = try self match {
            case Failure(cause) => fn(cause)
            case valueOrError => valueOrError
        } catch {
            case NonFatal(cause) => Failure[E,V](cause)
        }

        @inline def flatMap[V2](fn: V => Result[E,V2]) = valueTransform(fn)

        @inline def map[V2](fn: V => V2) = valueMap(fn)

    }

}
