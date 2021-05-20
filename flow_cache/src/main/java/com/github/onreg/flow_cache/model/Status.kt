package com.github.onreg.flow_cache.model

sealed class Status<out R> {
    object Empty : Status<Nothing>()
    object Loading : Status<Nothing>()
    data class Data<T>(private val data: T) : Status<T>(), StatusEvent<T> by StatusEventImpl(data)
    data class Error(private val throwable: Throwable) : Status<Nothing>(), StatusEvent<Throwable> by StatusEventImpl(throwable)
}
