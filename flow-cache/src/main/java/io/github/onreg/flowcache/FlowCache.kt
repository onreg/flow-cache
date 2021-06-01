package io.github.onreg.flowcache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.onreg.flowcache.cache.Cache
import io.github.onreg.flowcache.cache.CacheHolder
import io.github.onreg.flowcache.cache.ParamCache
import io.github.onreg.flowcache.cache.StatusCacheHolder
import io.github.onreg.flowcache.model.Status
import kotlinx.coroutines.flow.Flow
import kotlin.properties.ReadOnlyProperty

internal const val DEF_ACTION = "DEF_ACTION"

fun <R : Any?> ViewModel.cache(
    start: Boolean = true,
    function: () -> Flow<R>
): ReadOnlyProperty<ViewModel, Cache<R>> {
    return CacheHolder(DEF_ACTION, start, viewModelScope, { function() })
}

fun <T : Any?, R : Any?> ViewModel.cache(
    initialParam: T? = null,
    start: Boolean = true,
    function: (T) -> Flow<R>
): ReadOnlyProperty<ViewModel, ParamCache<T, R>> {
    return CacheHolder(initialParam, start, viewModelScope, function)
}

fun <R : Any?> ViewModel.statusCache(
    start: Boolean = true,
    function: () -> Flow<R>
): ReadOnlyProperty<ViewModel, Cache<Status<R>>> {
    return StatusCacheHolder(DEF_ACTION, start, viewModelScope, { function() })
}

fun <T : Any, R : Any?> ViewModel.statusCache(
    initialParam: T? = null,
    start: Boolean = true,
    function: (T) -> Flow<R>
): ReadOnlyProperty<ViewModel, ParamCache<T, Status<R>>> {
    return StatusCacheHolder(initialParam, start, viewModelScope, function)
}