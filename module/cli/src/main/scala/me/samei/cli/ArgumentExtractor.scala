package me.samei.cli

import me.samei.std._

trait ArgumentExtractor extends ValueExtractor {
    def firstOption[T](
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[Option[T]]
}

object ArgumentExtractor {

    trait Syntax extends ValueExtractor.Syntax {

        def firstOption[T](
            implicit
            convertor: Convertor[String, T],
            context: ArgumentExtractor
        ): ValueExtractor.Result[Option[T]] = context.firstOption[T]

    }

    object Syntax extends Syntax
}