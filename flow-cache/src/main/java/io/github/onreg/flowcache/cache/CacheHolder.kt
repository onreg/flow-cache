package io.github.onreg.flowcache.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class CacheHolder<T : Any?, R>(
    private var param: T?,
    start: Boolean = true,
    coroutineScope: CoroutineScope,
    function: (T) -> Flow<R>
) : Cache<R>, ParamCache<T, R> {

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
}