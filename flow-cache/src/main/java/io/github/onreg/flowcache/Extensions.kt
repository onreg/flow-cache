package io.github.onreg.flowcache

import io.github.onreg.flowcache.model.Status
import io.github.onreg.flowcache.model.StatusEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach

fun <T> Flow<Status<T>>.asDataEvent() =
    filterIsInstance<Status.Data<T>>()
        .filter { !it.consumed }
        .onEach { it.value }

fun <T> Flow<Status<T>>.asErrorEvent() =
    filterIsInstance<Status.Error>()
        .filter { !it.consumed }
        .onEach { it.value }


fun <T> Flow<Status<T>>.asEvent(): Flow<Status<T>> =
    filter {
        if (it is StatusEvent<*>) {
            return@filter !it.consumed.apply { it.value }
        }
        true
    }