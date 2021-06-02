package io.github.onreg.flowcache.cache

import io.github.onreg.flowcache.model.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class StatusCacheHolder<T : Any, R>(
    private var param: T?,
    start: Boolean = true,
    coroutineScope: CoroutineScope,
    function: (T) -> Flow<R>
) : Cache<Status<R>>, ParamCache<T, Status<R>> {

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
}