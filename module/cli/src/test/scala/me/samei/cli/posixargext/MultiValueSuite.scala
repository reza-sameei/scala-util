package me.samei.std.posixargext

import me.samei.cli.PosixArgExtractor
import me.samei.std.Result
import me.samei.predef.Logger
import org.scalatest.{FlatSpec, Matchers}
import me.samei.std.Convertors._
import me.samei.std.ValueExtractor
import me.samei.cli.ArgumentExtractor.Syntax._
import me.samei.cli.TestKit

class MultiValueSuite extends FlatSpec with Matchers with TestKit {

    val logger = Logger apply classOf[MultiValueSuite]

    it must "return undefined-key for nelist 'nums' in empty-list" in {
        implicit val ctx = new PosixArgExtractor("1st", Nil)
        nelist[Int]("nums") matchError {
            case ValueExtractor.Error.UndefinedKey(key, _) => key shouldEqual "nums"
        }
    }

    it must "return empty-list for list 'nums' in multi-value-list" in {
        implicit val ctx = new PosixArgExtractor("2nd", List("-model", "-name"))
        list[Int]("nums") shouldEqual Result.Value(Nil)
    }

    it must "return missed-value for nelist 'nums' in mutli-value list" in {
        implicit val ctx = new PosixArgExtractor("3rd", List("-nums","-name", "reza"))
        nelist[Int]("nums") matchError {
            case ValueExtractor.Error.MissedValue(key, _) => key shouldEqual "nums"
        }
    }

    it must "return missed-value for list 'nums' in mutli-value list" in {
        implicit val ctx = new PosixArgExtractor("4th", List("-nums","-name", "reza"))
        list[Int]("nums") matchError {
            case ValueExtractor.Error.MissedValue(key, _) => key shouldEqual "nums"
        }
    }

    it must "return numbers of 'nums' in nelist & list" in {
        implicit val ctx = new PosixArgExtractor("5th", List("persist", "-nums", "21", "52", "43", "-name", "reza"))
        nelist[Int]("nums") shouldEqual Result.Value(List(21,52,43))
        list[Int]("nums") shouldEqual Result.Value(List(21,52,43))
    }

    it must "return invalid-value of 'nums' in nelist" in {
        implicit val ctx = new PosixArgExtractor("5th", List("persist", "-nums", "21", "x52", "43", "-name", "reza"))
        nelist[Int]("nums") matchError {
            case ValueExtractor.Error.InvalidValue(key, _, _, origin, Some(cause)) =>
                cause.printStackTrace // java.lang.NumberFormatException
                key shouldEqual "nums"
                origin shouldEqual "x52"
        }
    }

}
