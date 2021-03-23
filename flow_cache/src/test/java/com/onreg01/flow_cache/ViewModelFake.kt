package com.onreg01.flow_cache

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow

const val DATA = "data"
const val PARAM_DATA = "paramData"
const val STATUS_DATA = "statusData"
const val PARAM_STATUS_DATA = "paramStatusData"

class ViewModelFake(
    initialParam: Map<String, String> = emptyMap(),
    autoStart: Map<String, Boolean> = emptyMap()
) : ViewModel() {

    private val bodyExecution = mutableMapOf<String, Int>()

    val results = mapOf(DATA to 1, PARAM_DATA to 2, STATUS_DATA to 3, PARAM_STATUS_DATA to 4)
    val awaitHandlers: MutableMap<String, CompletableDeferred<Unit>> = mutableMapOf()

    val data by cache<Int>(start = autoStart.getOrDefault(DATA, true)) {
        body(DATA)
    }

    val paramData by cache<String, Int>(initialParam[PARAM_DATA], start = autoStart.getOrDefault(PARAM_DATA, true)) {
        body(PARAM_DATA)
    }

    val statusData by statusCache<Int>(start = autoStart.getOrDefault(STATUS_DATA, true)) {
        body(STATUS_DATA)
    }

    val paramStatusData by statusCache<String, Int>(start = autoStart.getOrDefault(PARAM_STATUS_DATA, true)) {
        body(PARAM_STATUS_DATA)
    }

    private fun body(name: String) = flow {
        bodyExecution.inc(name)
        awaitHandlers[name]?.await()
        emit(results[name]!!)
    }

    private fun MutableMap<String, Int>.inc(name: String) {
        this[name] = this.getOrDefault(name, 0).inc()
    }

    suspend fun recordBodyExecutions(func: suspend () -> Unit): Map<String, Int> {
        bodyExecution.clear()
        func.invoke()
        return bodyExecution
    }
}