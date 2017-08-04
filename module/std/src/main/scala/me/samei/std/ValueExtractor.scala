package me.samei.std

trait ValueExtractor {

    def required[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[T]

    def required[T](
        namespace: String,
        fn: ValueExtractor => ValueExtractor.Result[T]
    ): ValueExtractor.Result[T]

    def optional[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[Option[T]]

    def optional[T](
        namespace: String,
        fn: ValueExtractor => ValueExtractor.Result[T]
    ): ValueExtractor.Result[Option[T]]

    def nelist[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[List[T]]

    def nelist[T](
        namespace: String,
        fn: ValueExtractor => ValueExtractor.Result[T]
    ): ValueExtractor.Result[List[T]]

    def list[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[List[T]]

    def list[T](
        namespace: String,
        fn: ValueExtractor => ValueExtractor.Result[T]
    ): ValueExtractor.Result[List[T]]

}

object ValueExtractor {

    type Result[T] = me.samei.std.Result[Error, T]

    type Extractor[T] = ValueExtractor => Result[T]

    sealed trait Error {
        def key: String
        def desc: String
    }

    object Error {

        // Failure vs. Unexpected!
        case class Unexpected(
            override val key: String,
            override val desc: String,
            cause: Option[Throwable]
        ) extends Error

        case class InvalidKey(override val key: String, override val desc: String) extends Error

        case class UndefinedKey(override val key: String, override val desc: String) extends Error

        // Missed single value + empty list for nelist
        case class MissedValue(override val key: String, override val desc: String) extends Error

        case class InvalidValue[T](
            override val key: String, override val desc: String,
            convertor: Convertor[String, T], origin: String,
            cause: Option[Throwable]
        ) extends Error

        // Custom Value Errors
        case class IncompleteValue(
            override val key: String,
            override val desc: String,
            origin: Error
        ) extends Error

    }

    trait Syntax {

        def required[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            context: ValueExtractor
        ): Result[T] = context required key

        def required[T](
            ns: String,
            fn: Extractor[T]
        )(
            implicit context: ValueExtractor
        ): Result[T] = context required (ns, fn)

        def optional[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            context: ValueExtractor
        ): Result[Option[T]] = context optional key

        def optional[T](
            ns: String,
            fn: Extractor[T]
        )(
            implicit context: ValueExtractor
        ): Result[Option[T]] = context optional (ns, fn)

        def nelist[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            context: ValueExtractor
        ): Result[List[T]] = context nelist key

        def nelist[T](
            ns: String,
            fn: Extractor[T]
        )(
            implicit context: ValueExtractor
        ): Result[List[T]] = context nelist (ns, fn)

        def list[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            context: ValueExtractor
        ): Result[List[T]] = context list key

        def list[T](
            ns: String,
            fn: Extractor[T]
        )(
            implicit context: ValueExtractor
        ): Result[List[T]] = context list (ns, fn)

    }

    object Syntax extends Syntax
}