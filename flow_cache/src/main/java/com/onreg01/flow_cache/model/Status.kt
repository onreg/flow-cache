package com.onreg01.flow_cache.model

sealed class Status {
    object Empty : Status()
    object Loading : Status()
    sealed class Result<R> : Status() {
        data class Data<R>(private val value: R) : Result<R>()
        data class Error(private val throwable: Throwable) : Result<Nothing>()
    }
}
