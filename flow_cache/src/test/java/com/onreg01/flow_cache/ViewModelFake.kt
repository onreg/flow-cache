package com.onreg01.flow_cache

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.flowOf

const val DATA = "data"
const val PARAM_DATA = "paramData"
const val STATUS_DATA = "statusData"
const val PARAM_STATUS_DATA = "paramStatusData"

class ViewModelFake(
    results: Map<String, Int> = mapOf(DATA to 1, PARAM_DATA to 2, STATUS_DATA to 3, PARAM_STATUS_DATA to 4),
    autoStart: Map<String, Boolean> = emptyMap()
) : ViewModel() {

    private val bodyExecution = mutableMapOf<String, Int>()

    val data by cache<Int>(start = autoStart.getOrDefault(DATA, true)) {
        bodyExecution.inc(DATA)
        flowOf(results.getOrDefault(DATA, 1))
    }

    val paramData by cache<String, Int>(start = autoStart.getOrDefault(PARAM_DATA, true)) {
        bodyExecution.inc(PARAM_DATA)
        flowOf(results.getOrDefault(PARAM_DATA, 2))
    }

    val statusData by statusCache<Int>(start = autoStart.getOrDefault(STATUS_DATA, true)) {
        bodyExecution.inc(STATUS_DATA)
        flowOf(results.getOrDefault(STATUS_DATA, 3))
    }

    val paramStatusData by statusCache<String, Int>(start = autoStart.getOrDefault(PARAM_STATUS_DATA, true)) {
        bodyExecution.inc(PARAM_STATUS_DATA)
        flowOf(results.getOrDefault(PARAM_STATUS_DATA, 4))
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