package com.homefirstindia.hfo.utils

import java.util.logging.Logger

object LoggerUtils {

    private val logger: Logger = Logger.getLogger(LoggerUtils::class.java.simpleName)

    fun log(value: String) {
        logger.info("\n\nHFOS - Value --> $value\n\n")
    }

    fun logBody(body: String) {
        logger.info("\n\nHFOS - Body --> $body\n\n")
    }

    fun logMethodCall(value: String) {
        logger.info("\nHFOS -\n----------------------\n  Method --> $value  \n----------------------\n\n")
    }

    fun printLog(value: String) {
        println("\n\nHFOS - Value --> $value\n\n")
    }

}