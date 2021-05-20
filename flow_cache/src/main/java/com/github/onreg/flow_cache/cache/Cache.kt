package com.github.onreg.flow_cache.cache

import kotlinx.coroutines.flow.Flow

interface Cache<R> {
    val cache: Flow<R>

    fun run()
}
