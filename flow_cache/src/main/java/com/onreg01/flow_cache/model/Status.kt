package com.onreg01.flow_cache.model

sealed class Status<out R> {
    object Empty: Status<Nothing>()
    object Loading : Status<Nothing>()
    data class Data<R>(private val value: R) : Status<R>(), Event<R> by EventImpl(value)
    data class Error(private val throwable: Throwable) : Status<Nothing>(), Event<Throwable> by EventImpl(throwable)
}
