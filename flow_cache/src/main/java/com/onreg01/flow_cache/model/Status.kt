package com.onreg01.flow_cache.model

sealed class Status<out R> {
    object Empty : Status<Nothing>()
    object Loading : Status<Nothing>()
    data class Data<T>(val value: T) : Status<T>()
    data class Error(val throwable: Throwable) : Status<Nothing>()
}
