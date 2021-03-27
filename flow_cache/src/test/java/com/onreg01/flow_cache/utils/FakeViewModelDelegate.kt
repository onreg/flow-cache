package com.onreg01.flow_cache.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeViewModelDelegate<T>(
    private val result: T
) : BodyExecutionSpy, BodyExecutionHandler<T> {

    private var bodyExecution = 0
    override var awaitHandler: CompletableDeferred<Unit>? = null
    override var throwable: Throwable? = null

    override suspend fun recordBodyExecutions(func: suspend () -> Unit): Int {
        bodyExecution = 0
        func.invoke()
        return bodyExecution
    }

    override fun run(): Flow<T> = flow {
        bodyExecution = bodyExecution.inc()
        throwable?.let { throw it }
        awaitHandler?.await()
        emit(result)
    }
}

interface BodyExecutionSpy {
    suspend fun recordBodyExecutions(func: suspend () -> Unit): Int
}

interface BodyExecutionHandler<T> {
    var awaitHandler: CompletableDeferred<Unit>?
    var throwable: Throwable?
    fun run(): Flow<T>
}