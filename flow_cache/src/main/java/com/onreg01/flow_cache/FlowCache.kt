package com.onreg01.flow_cache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onreg01.flow_cache.cache.Cache
import com.onreg01.flow_cache.cache.CacheHolder
import com.onreg01.flow_cache.cache.ParamCache
import com.onreg01.flow_cache.cache.StatusCacheHolder
import com.onreg01.flow_cache.model.Status
import kotlinx.coroutines.flow.Flow
import kotlin.properties.ReadOnlyProperty

internal const val DEF_ACTION = "DEF_ACTION"

fun <R : Any?> ViewModel.cache(
    start: Boolean = true,
    function: () -> Flow<R>
): ReadOnlyProperty<ViewModel, Cache<R>> {
    return CacheHolder(DEF_ACTION, start, viewModelScope, { function() })
}

fun <T : Any?, R : Any?> ViewModel.paramCache(
    initialParam: T? = null,
    start: Boolean = true,
    function: (T) -> Flow<R>
): ReadOnlyProperty<ViewModel, ParamCache<T, R>> {
    return CacheHolder(initialParam, start, viewModelScope, function)
}

fun <R : Any?> ViewModel.statusCache(
    start: Boolean = true,
    function: () -> Flow<R>
): ReadOnlyProperty<ViewModel, Cache<Status>> {
    return StatusCacheHolder(DEF_ACTION, start, viewModelScope, { function() })
}

fun <T : Any, R : Any?> ViewModel.paramStatusCache(
    initialParam: T? = null,
    start: Boolean = true,
    function: (T) -> Flow<R>
): ReadOnlyProperty<ViewModel, ParamCache<T, Status>> {
    return StatusCacheHolder(initialParam, start, viewModelScope, function)
}