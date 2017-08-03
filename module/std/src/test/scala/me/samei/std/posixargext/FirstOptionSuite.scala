package me.samei.std.posixargext

import me.samei.std.PosixArgExtractor
import org.scalatest.{FlatSpec, Matchers}
import me.samei.std.Convertors._
import me.samei.std.ValueExtractor.Syntax._
import me.samei.std.Result

class FirstOptionSuite extends FlatSpec with Matchers {

    it must "return error for firstOption in an empty data-list" in {
        implicit val extractor = new PosixArgExtractor("1st", Nil)
        firstOption[String] shouldEqual Result.Value(None)
    }

    it must "return first value for firstOption in a single-value data-list" in {
        implicit val extractor = new PosixArgExtractor("2nd", List("hello"))
        firstOption[String] shouldEqual Result.Value(Some("hello"))
    }

    it must "return first value for firstOption in a multi-value data-list" in {
        implicit val extractor = new PosixArgExtractor("3rd", List("1", "2", "3"))
        firstOption[String] shouldEqual Result.Value(Some("1"))
    }
}
