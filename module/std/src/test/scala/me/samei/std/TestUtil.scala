package me.samei.std

import org.scalactic.source
import org.scalatest.{Assertion, Assertions}
import scala.util._

trait TestUtil { self: Assertions =>

    implicit class EitherTestOperations[L,R](val self: Either[L,R]) extends {

        def onRight(fn : R => Assertion)(
            implicit pos: source.Position
        ): Assertion = {
            self match {
                case Right(value) => fn(value)
                case Left(value) => fail(s"Left Value: ${value}")
            }
        }

        def matchRight(fn: PartialFunction[R, Assertion])(
            implicit pos: source.Position
        ): Assertion = {
            self.onRight { value =>
                if (fn.isDefinedAt(value)) fn(value)
                else fail(s"Unmatched Right Value: ${value}")
            }
        }

        def onLeft(fn: L => Assertion)(
            implicit pos: source.Position
        ): Assertion = {
            self match {
                case Right(value) => fail(s"Right Value: ${value}")
                case Left(value) => fn(value)
            }
        }

        def matchLeft(fn: PartialFunction[L, Assertion])(
            implicit pos: source.Position
        ): Assertion = {
            self.onLeft { value =>
                if (fn.isDefinedAt(value)) fn(value)
                else fail(s"Unmatched Left Value: ${value}")
            }
        }
    }

    implicit class ResultTestOperations[E,V](val self: Result[E,V]) {

        def onValue(fn : V => Assertion)(
            implicit pos: source.Position
        ): Assertion = self match {
            case Result.Value(value) => fn(value)
            case Result.Error(value) => fail(s"Error: ${value}")
            case Result.Failure(cause) => fail(s"Failure: ${cause}", cause)
        }

        def matchValue(fn: PartialFunction[V, Assertion])(
            implicit pos: source.Position
        ): Assertion = self.onValue { value =>
            if (fn isDefinedAt value) fn(value)
            else fail(s"Unmatched Value: ${value}")
        }

        def onError(fn: E => Assertion)(
            implicit pos: source.Position
        ): Assertion = self match {
            case Result.Value(value) => fail(s"Value: ${value}")
            case Result.Error(error) => fn(error)
            case Result.Failure(cause) => fail(s"Failure: ${cause}", cause)
        }

        def matchError(fn: PartialFunction[E, Assertion])(
            implicit pos: source.Position
        ): Assertion = self.onError{ err =>
            if (fn isDefinedAt err) fn(err)
            else fail(s"Unmatched Error: ${err}")
        }

        def onFailure(fn: Throwable => Assertion)(
            implicit pos: source.Position
        ): Assertion = self match {
            case Result.Value(value) => fail(s"Value: ${value}")
            case Result.Error(err) => fail(s"Error: ${err}")
            case Result.Failure(cause) => fn(cause)
        }

        def matchFailure(fn: PartialFunction[Throwable, Assertion])(
            implicit pos: source.Position
        ): Assertion = self onFailure { cause =>
            if (fn isDefinedAt cause) fn(cause)
            else fail(s"Unmatched Failure: ${cause}", cause)
        }
    }

}
