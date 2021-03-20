package com.onreg01.flow_cache.cache

import androidx.lifecycle.ViewModel
import com.onreg01.flow_cache.model.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class StatusCacheHolder<T : Any, R>(
    private var param: T?,
    start: Boolean = true,
    coroutineScope: CoroutineScope,
    function: (T) -> Flow<R>
) : Cache<Status>, ParamCache<T, Status>, ReadOnlyProperty<ViewModel, StatusCacheHolder<T, R>> {

    private val flow = MutableStateFlow<T?>(null)

    override val cache = flow.filterNotNull()
        .onStart {
            param?.let {
                if (start) {
                    flow.value = it
                }
            }
        }.flatMapLatest {
            flow {
                emit(Status.Loading)
                emitAll(function(it)
                    .map { Status.Result.Data(it) }
                    .onEmpty { emit(Status.Empty) }
                    .catch { emit(Status.Result.Error(it)) })
            }
        }
        .onEach { flow.value = null }
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)
        .onStart { emit(Status.Empty) }

    override fun run(value: T) {
        flow.value = value
        param = value
    }

    override fun run() {
        flow.value = param
    }

    override fun getValue(thisRef: ViewModel, property: KProperty<*>): StatusCacheHolder<T, R> = this
}
