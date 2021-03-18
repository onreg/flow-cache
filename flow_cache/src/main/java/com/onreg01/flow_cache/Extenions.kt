package com.onreg01.flow_cache

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.whenStarted
import com.onreg01.flow_cache.model.Event
import com.onreg01.flow_cache.model.Status
import kotlinx.coroutines.flow.*

fun <T> Flow<T>.oneShotError(lifecycleOwner: LifecycleOwner? = null): Flow<Throwable> {
    return filterIsInstance<Status.Error>()
        .filter { !it.hasBeenHandled() }
        .onEach { it.consumeContent() }
        .map { it.peekContent() }
        .awaitStarted(lifecycleOwner)
}

fun <T> Flow<Status<T>>.oneShotData(lifecycleOwner: LifecycleOwner? = null): Flow<T> {
    return filterIsInstance<Status.Data<T>>()
        .filter { !it.hasBeenHandled() }
        .onEach { it.consumeContent() }
        .map { it.peekContent() }
        .awaitStarted(lifecycleOwner)
}

fun <T> Flow<Status<T>>.oneShot(lifecycleOwner: LifecycleOwner? = null): Flow<Status<T>> {
    return filter {
        if (it is Event<*>) {
            return@filter !it.hasBeenHandled().apply { it.consumeContent() }
        }
        true
    }
        .awaitStarted(lifecycleOwner)
}

fun <T> Flow<T>.awaitStarted(lifecycleOwner: LifecycleOwner? = null): Flow<T> {
    return mapLatest { lifecycleOwner?.whenStarted { it } ?: it }
}

fun <T> Flow<Status<T>>.filterStatusData(): Flow<T> {
    return filterIsInstance<Status.Data<T>>()
        .map { it.peekContent() }
}
