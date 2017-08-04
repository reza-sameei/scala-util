package me.samei.std

import com.typesafe.scalalogging.Logger

trait Module {

    def name: String

    protected def loadLogger:Logger = Logger(name)

    protected def logger: Logger

}
