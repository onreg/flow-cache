package com.github.onreg.flow_cache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.onreg.flow_cache.cache.Cache
import com.github.onreg.flow_cache.cache.CacheHolder
import com.github.onreg.flow_cache.cache.ParamCache
import com.github.onreg.flow_cache.cache.StatusCacheHolder
import com.github.onreg.flow_cache.model.Status
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