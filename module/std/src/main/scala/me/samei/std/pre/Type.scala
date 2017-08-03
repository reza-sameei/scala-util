package me.samei.std.pre

import scala.annotation.implicitNotFound

/**
  * Created by reza on 8/3/17.
  */
trait Type {

    /** A way to prevent subtype params base on: https://gist.github.com/milessabin/c9f8befa932d98dcc7a4 */

    // Encoding for "A is not a subtype of B"
    // @implicitNotFound("WHAT")
    // could not find implicit value for parameter ev: com.bisphone.stdv1.predef.<:!<[scala.collection.mutable.Buffer[R],com.bisphone.stdv1.predef.StdEither[_, _]]
    @implicitNotFound("Need evidence to prove that ${A} is not a subtype of ${B} (${A} <:!< ${B})")
    trait <:!<[A, B]

    // Uses ambiguity to rule out the cases we're trying to exclude
    implicit def notSubType[A, B] : A <:!< B = null
    implicit def notSubtype1[A, B >: A] : A <:!< B = null
    implicit def notSubType2[A, B >: A] : A <:!< B = null

    // Type alias for context bound
    type |¬|[T] = {
        type λ[U] = U <:!< T
    }

    type Logger = com.typesafe.scalalogging.Logger
    val Logger = com.typesafe.scalalogging.Logger
}
