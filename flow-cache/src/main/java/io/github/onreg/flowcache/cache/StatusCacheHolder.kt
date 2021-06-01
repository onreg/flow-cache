package io.github.onreg.flowcache.cache

import androidx.lifecycle.ViewModel
import io.github.onreg.flowcache.model.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class StatusCacheHolder<T : Any, R>(
    private var param: T?,
    start: Boolean = true,
    coroutineScope: CoroutineScope,
    function: (T) -> Flow<R>
) : Cache<Status<R>>, ParamCache<T, Status<R>>, ReadOnlyProperty<ViewModel, StatusCacheHolder<T, R>> {

    private val flow = MutableStateFlow<T?>(null)

    @ExperimentalCoroutinesApi
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
                    .map { Status.Data(it) }
                    .onEmpty { emit(Status.Empty) }
                    .catch { emit(Status.Error(it)) })
            }
        }
        .onEach {
            if (it != Status.Loading) {
                flow.value = null
            }
        }
        .distinctUntilChanged()
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
