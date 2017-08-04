package me.samei.std.posixargext

import com.typesafe.scalalogging.Logger
import me.samei.cli.PosixArgExtractor
import me.samei.std.Result
import org.scalatest.{FlatSpec, Matchers}
import me.samei.std.Convertors._
import me.samei.cli.ArgumentExtractor.Syntax._
import me.samei.cli.TestKit
import me.samei.std.ValueExtractor

class SingleValueSuite extends FlatSpec with Matchers with TestKit {

    val logger = Logger apply classOf[SingleValueSuite]

    it must "return undefined-key for reqiured 'debug' in a multi-value data-list" in {
        implicit val extractor = new PosixArgExtractor("1st", List("1", "2", "3"))
        required[String]("debug") matchError {
            case ValueExtractor.Error.UndefinedKey(key, _) => key shouldEqual "debug"
        }
    }

    it must "return undefined-key for optional 'debug' in a multi-value data-list" in {
        implicit val extractor = new PosixArgExtractor("2nd", List("1", "2", "3"))
        optional[String]("debug") onValue { _ shouldBe None }
    }

    it must "return missed-value for required 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "-another-key")
        implicit val extractor = new PosixArgExtractor("3rd", data)
        required[String]("debug") matchError {
            case ValueExtractor.Error.MissedValue(key, desc) => key shouldBe "debug"
        }
    }

    it must "return missed-value for optional 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "-another-key")
        implicit val extractor = new PosixArgExtractor("4th", data)
        optional[String]("debug") matchError {
            case ValueExtractor.Error.MissedValue(key, desc) => key shouldBe "debug"
        }
    }

    it must "return string value for 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "true", "-another-key")
        implicit val extractor = new PosixArgExtractor("5th", data)
        required[String]("debug") shouldBe Result.Value("true")
    }

    it must "return boolean value for 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "true", "-another-key")
        implicit val extractor = new PosixArgExtractor("5th", data)
        required[Boolean]("debug") shouldBe Result.Value(true)
    }

    it must "return invalid-value error for 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "truex", "-another-key")
        implicit val extractor = new PosixArgExtractor("6th", data)
        required[Boolean]("debug") matchError {
            case ValueExtractor.Error.InvalidValue(key, _, _, origin, Some(cause)) =>
                key shouldBe "debug"
                origin shouldBe "truex"
        }
    }

}
