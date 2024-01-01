package com.funny.data_saver.core

/**
 * Some config that you can set:
 * 1. DEBUG: whether to output some debug info
 * 2. LIST_SEPARATOR: the separator used to convert a list into string, '#@#' by default (**don't use ',' which will occurs in json itself** )
 */
object DataSaverConfig {
    var DEBUG = true
    var LIST_SEPARATOR = "#@#"
}

