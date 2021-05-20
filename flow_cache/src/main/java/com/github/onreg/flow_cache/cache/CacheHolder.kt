package com.github.onreg.flow_cache.cache

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CacheHolder<T : Any?, R>(
    private var param: T?,
    start: Boolean = true,
    coroutineScope: CoroutineScope,
    function: (T) -> Flow<R>
) : Cache<R>,
    ParamCache<T, R>, ReadOnlyProperty<ViewModel, CacheHolder<T, R>> {

    private val flow = MutableStateFlow<T?>(null)

    @ExperimentalCoroutinesApi
    override val cache = flow.filterNotNull()
        .onStart {
            param?.let {
                if (start) {
                    flow.value = it
                }
            }
        }
        .flatMapLatest { function(it) }
        .onEach { flow.value = null }
        .distinctUntilChanged()
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)

    override fun run(value: T) {
        flow.value = value
        param = value
    }

    override fun run() {
        flow.value = param
    }

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): CacheHolder<T, R> = this
}
