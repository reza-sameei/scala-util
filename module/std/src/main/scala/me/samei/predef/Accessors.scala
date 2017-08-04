package me.samei.predef

import scala.util._
import scala.concurrent.Future

trait Accessors {

    implicit def std$accessors$either[T](v: T) = new EitherAccessors(v)

    implicit def std$accessors$option[T](v: T) = new OptionAccessors(v)

    implicit def std$accessors$failure[T <: Throwable](v: T) = new FailureAccessors(v)

    implicit def std$accessors$success[T](v: T) = new StdSuccessAccessors(v)

    implicit def std$accessors$future[T](v: T) = new StdFutureAccessors(v)
}

object Accessors extends Accessors

final class EitherAccessors[T](val self: T) extends AnyVal {
    @inline def left[R](implicit ev: T<:!<Either[_,_]): Either[T,R] = Left(self)
    @inline def right[L](implicit ev: T<:!<Either[_,_]): Either[L,T] = Right(self)
}

final class OptionAccessors[T](val self: T) extends AnyVal {
    @inline def optional(implicit ev: T<:!<Option[_]): Option[T] = Option(self)
    @inline def some(implicit ev: T<:!<Option[_]): Option[T] = Some(self)
}

final class FailureAccessors[T<:Throwable](val self: T) extends AnyVal {
    @inline def tryFailure[R]: Try[R] = Failure(self)
}

final class StdSuccessAccessors[T](val self: T) extends AnyVal {
    @inline def trySuccess(implicit ev: T <:!< Throwable): Try[T] = Success[T](self)
}

final class StdFutureAccessors[T](val self: T) extends AnyVal {
    @inline def futureSuccessful(implicit ev: T <:!< Throwable): Future[T] = Future successful self
    @inline def futureFailed[R](implicit ev: T <:< Throwable): Future[R] = Future failed self
}
