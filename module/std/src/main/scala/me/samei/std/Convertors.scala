package me.samei.std

import scala.reflect.ClassTag
import scala.concurrent.duration._

trait Convertors {

    def default[T:ClassTag] = {
        val c = implicitly[ClassTag[T]]
        Convertor[T,T](s"${c.runtimeClass.getName} => ${c.runtimeClass.getName}")(any => any)
    }

    implicit val std$convertor$string2string = default[String]

    implicit val std$convertor$string2int = Convertor[String,Int]("String => Int")(i => i.toInt)

    implicit val std$convertor$string2long = Convertor[String,Long]("String => Long")(i => i.toLong)

    implicit val std$convertor$string2float = Convertor[String,Float]("String => Float")(i => i.toFloat)

    implicit val std$convertor$string2double = Convertor[String,Double]("String => Float")(i => i.toDouble)

    implicit val std$convertor$string2bool = Convertor[String, Boolean]("String => Boolean")( i => i.toLowerCase match {
        case "true" | "on" => true
        case "false" | "off" => false
    })

    implicit val std$convertor$string2finiteduration = Convertor[String,FiniteDuration]("String => FiniteDuration")(i => Duration(i).asInstanceOf[FiniteDuration])

}

object Convertors extends Convertors

