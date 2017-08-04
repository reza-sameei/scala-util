package me.samei.predef

trait Value {

    def unit(): Unit = ()

    def none[T]: Option[T] = None

}
