package com.onreg01.flowcache.utils

import android.app.Activity
import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.sample

private const val DEF_THROTTLING = 400L

fun <T> Flow<T>.throttleFirst(periodMillis: Long = DEF_THROTTLING): Flow<T> {
    require(periodMillis > 0) { "period should be positive" }
    return flow {
        var lastTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }
}

@FlowPreview
fun <T> Flow<T>.sample(): Flow<T> {
    return this.sample(DEF_THROTTLING)
}

fun CircularProgressIndicator.changeVisibility(progress: Boolean) {
    if (progress) show() else hide()
}

fun Activity.handleException(view: View, throwable: Throwable) {
    val message = if (throwable is MessageException) {
        throwable.message
    } else {
        "Something went wrong!"
    }
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}