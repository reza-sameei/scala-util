package me.samei.std

import pre.Logger

/**
  * Created by reza on 8/3/17.
  */

trait Module {

    def name: String

    protected def loadLogger:Logger = Logger(name)

    protected def logger: Logger

}
