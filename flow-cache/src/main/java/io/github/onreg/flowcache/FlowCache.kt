package io.github.onreg.flowcache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.onreg.flowcache.cache.Cache
import io.github.onreg.flowcache.cache.CacheHolder
import io.github.onreg.flowcache.cache.ParamCache
import io.github.onreg.flowcache.cache.StatusCacheHolder
import io.github.onreg.flowcache.model.Status
import kotlinx.coroutines.flow.Flow

internal const val DEF_ACTION = "DEF_ACTION"

fun <R : Any?> ViewModel.cache(
    start: Boolean = true,
    function: () -> Flow<R>
): Lazy<Cache<R>> = lazy {
    CacheHolder(DEF_ACTION, start, viewModelScope, { function() })
}

fun <T : Any?, R : Any?> ViewModel.cache(
    initialParam: T? = null,
    start: Boolean = true,
    function: (T) -> Flow<R>
): Lazy<ParamCache<T, R>> = lazy {
    CacheHolder(initialParam, start, viewModelScope, function)
}

fun <R : Any?> ViewModel.statusCache(
    start: Boolean = true,
    function: () -> Flow<R>
): Lazy<Cache<Status<R>>> = lazy {
    StatusCacheHolder(DEF_ACTION, start, viewModelScope, { function() })
}

fun <T : Any, R : Any?> ViewModel.statusCache(
    initialParam: T? = null,
    start: Boolean = true,
    function: (T) -> Flow<R>
): Lazy<ParamCache<T, Status<R>>> = lazy {
    StatusCacheHolder(initialParam, start, viewModelScope, function)
}