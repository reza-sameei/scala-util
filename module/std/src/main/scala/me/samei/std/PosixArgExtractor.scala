package me.samei.std

import ValueExtractor.Error._
import pre._

/**
  * Created by reza on 8/3/17.
  */

class PosixArgExtractor(
    override val name: String,
    data: Iterable[String]
) extends ArgumentExtractor with PosixArgExtractor.Util with Module {

    override val logger = loadLogger

    logger info s"Init, data: ${data.toList}"

    override def required[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[T] = returnKey(key) flatMap { impureKey =>
        fold(data.iterator, FindKey((impureKey))) { (st, item) =>
            st match {
                case FindKey(impureKey) if item == impureKey =>
                    PeakValue(key)
                case PeakValue(impureKey) if isKey(item) =>
                    InternalError(MissedValue(key, s"Missed value for '${key}'"))
                case PeakValue(impureKey) =>
                    FinalValue(item)
                case _ => st
            }
        } match {
            case FinalValue(value) => convert[T](convertor, key, value)
            case PeakValue(_) => Result error MissedValue(key, s"Missed value for key: '${key}'")
            case FindKey(_) => Result error UndefinedKey(key, s"Undefined key: '${key}'")
            case InternalError(error) => Result error error
        }
    }

    override def required[T](
        namespace: String,
        fn: ValueExtractor => ValueExtractor.Result[T]
    ) = {
        val msg = s"Unsupported, Extract 'required' custom-value from namespace:'${namespace}'"
        logger warn msg
        Result failure new RuntimeException(msg)
    }

    override def optional[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[Option[T]] = {
        required(key).map { _.some }.errorTransform{
            case UndefinedKey(_, _) =>  Result value none[T]
            case err => Result error err
        }
    }

    override def optional[T](
        namespace: String,
        fn: ValueExtractor => ValueExtractor.Result[T]
    ) = {
        val msg = s"Unsupported, Extract 'optional' custom-value from namespace:'${namespace}'"
        logger warn msg
        Result failure new RuntimeException(msg)
    }

    override def nelist[T](key: String)(
        implicit convertor: Convertor[String, T]
    ) =
        returnKey(key) flatMap { impureKey =>
            fold(data.iterator, FindKey(impureKey)) { (st, item) =>
                st match {
                    case FindKey(impureKey) if item == impureKey =>
                        MultipleValue(key, Nil)
                    case MultipleValue(_, Nil) if isKey(item) =>
                        InternalError(MissedValue(key, s"Missed value for '${key}'"))
                    case MultipleValue(key, Nil) =>
                        MultipleValue(key, item :: Nil)
                    case MultipleValue(_, list) if isKey(item) =>
                        FinalValues(list)
                    case MultipleValue(key, list) =>
                        MultipleValue(key, item :: list)
                    case _ => st
                }
            } match {
                case MultipleValue(_, Nil) => Result error MissedValue(key, s"Missed value for key: '${key}'")
                case MultipleValue(_, list) => convertList(convertor, key, list.reverse)
                case FinalValues(list) => convertList(convertor, key, list.reverse)
                case FindKey(_) => Result error UndefinedKey(key, s"Undefined key: '${key}'")
                case InternalError(error) => Result error error
            }
        }

    override def nelist[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]) = {
        val msg = s"Unsupported, Extract 'nelist' custom-value from namespace:'${namespace}'"
        logger warn msg
        Result failure new RuntimeException(msg)
    }

    override def list[T] (key: String)(
        implicit convertor: Convertor[String, T]
    ) = {
        nelist(key).errorTransform{
            case UndefinedKey(_,_) => Result value Nil
            case err => Result error err
        }
    }

    override def list[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]) =
        nelist(namespace, fn)

    override def firstOption[T] (
        implicit convertor: Convertor[String, T]
    ) = {
        data.headOption match {
            case None => Result value none
            case Some(v) if isKey(v) => Result value none
            case Some(v) => convert[T](convertor, "Not a key; Just first value", v) map { i:T => Some(i) }
        }
    }

}

object PosixArgExtractor {

    trait Util { self: PosixArgExtractor =>

        val prefix = "-"

        protected def mkKey(s: String) = prefix + s
        protected def isKey(s: String) = s startsWith prefix

        protected sealed trait State { def break: Boolean }
        protected sealed trait Complete extends State { override val break = true }
        protected sealed trait InComplete extends State  { override val break = false }
        protected final case class InternalError(error: ValueExtractor.Error) extends State  { override val break = true }

        protected final case class FindKey(val key: String) extends InComplete
        protected final case class PeakValue(val key: String) extends InComplete
        protected final case class MultipleValue(val key: String, val value: List[String]) extends InComplete

        protected final case class FinalValue(val value: String) extends Complete
        protected final case class FinalValues(val list: List[String]) extends Complete
        protected final case object NoValue extends Complete

        protected def fold[T](itr: Iterator[String], init: State)(f: (State, String) => State) = {
            var rsl = init
            while(itr.hasNext && !rsl.break) rsl = f(rsl, itr.next)
            rsl
        }

        protected def returnKey(key: String): ValueExtractor.Result[String] =
            key.trim match {
                case "" => Result error InvalidKey(key, s"Invalid Key: '${key}'")
                case key => Result value mkKey(key)
            }

        protected def convert[T](
            convertor: Convertor[String, T],
            key: String,
            value: String
        ): ValueExtractor.Result[T] = {

            Result lazyValue {
                convertor unsafe value
            } failureTransform { cause =>

                logger.debug(s"Convert failure for single-value, Key:${key}, Value:${value}, Convertor:${convertor.title}", cause)

                Result error InvalidValue(
                    key = key, desc = s"Convert failure for '${key}' = '${value}' with '${convertor.title}'",
                    convertor = convertor, origin = value, cause = Some(cause)
                )
            }
        }

        protected def convertList[T](
            convertor: Convertor[String, T],
            key: String,
            values: List[String]
        ): ValueExtractor.Result[List[T]] = {

            var lastValue: String = ""

            Result lazyValue {
                values map {i =>
                    lastValue = i
                    convertor unsafe i
                }
            } failureTransform { cause =>

                logger.debug(s"Convert failure for list, Key:${key}, Value:${lastValue}, Convertor:${convertor.title}", cause)

                Result error InvalidValue(
                    key = key, desc = s"Convert failure for list '${key}' at specific vlaue: '${lastValue}' with '${convertor.title}'",
                    convertor = convertor, origin = lastValue, cause = Some(cause)
                )
            }
        }
    }
}
