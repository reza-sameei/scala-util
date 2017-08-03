package me.samei.std.pre

/**
  * Created by reza on 8/3/17.
  */
trait Value {

    def unit(): Unit = ()

    def none[T]: Option[T] = None

}
