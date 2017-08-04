package me.samei.predef

import scala.util._

trait Operations {

    implicit def std$operations$either[L,R](v: Either[L,R]) = new EitherOperations(v)

    implicit def std$operations$try[T](v: Failure[T]) = new TryOps(v)

}

object Operations extends Operations

final class EitherOperations[L,R] (val self: Either[L,R]) extends AnyVal {

    @inline def flatMap[R2](f: R => Either[L,R2]): Either[L,R2] = self match {
        case Left(left) => Left(left)
        case Right(right) => f(right)
    }

    @inline def map[R2](f: R => R2): Either[L,R2] = flatMap( r => Right(f(r)) )
    
    @inline def leftFlatMap[L2](f: L => Either[L2,R]): Either[L2,R] = self match {
        case Left(left) => f(left)
        case Right(right) => Right(right)
    }

    @inline def leftMap[L2](f: L => L2): Either[L2,R] = leftFlatMap( l => Left(f(l)) )
}

final class TryOps[T](val self: Try[T]) extends AnyVal {

    @inline def failMap( f: Throwable => Throwable ): Try[T] = self match {
        case Failure(cause) => Failure(f(cause))
        case success => success
    }

    @inline def failMap(msg: String): Try[T] = failMap(e => new scala.RuntimeException(msg,e))
}

