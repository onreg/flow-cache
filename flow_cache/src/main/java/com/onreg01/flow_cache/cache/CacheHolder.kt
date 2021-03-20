package com.onreg01.flow_cache.cache

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CacheHolder<T : Any?, R>(
    private var action: T?,
    start: Boolean = true,
    coroutineScope: CoroutineScope,
    private val function: (T) -> Flow<R>
) : Cache<R>,
    ParamCache<T, R>, ReadOnlyProperty<ViewModel, CacheHolder<T, R>> {

    private val flow = MutableStateFlow<T?>(null)

    override val cache = flow.filterNotNull()
        .onStart {
            action?.let {
                if (start) {
                    emit(it)
                }
            }
        }
        .flatMapLatest { function(it) }
        .onEach { flow.value = null }
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)

    override fun run(value: T) {
        flow.value = value
        action = value
    }

    override fun run() {
        action?.let {
            flow.value = it
        }
    }

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): CacheHolder<T, R> = this
}
