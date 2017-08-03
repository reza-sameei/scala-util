package me.samei.std

/**
  * Created by reza on 8/3/17.
  */

object Path {
    type Type = java.nio.file.Path

    final case class Absolute(path: Type)
    final case class ReadableFile(path: Type)
}
