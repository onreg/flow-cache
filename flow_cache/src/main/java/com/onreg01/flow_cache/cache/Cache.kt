package com.onreg01.flow_cache.cache

import kotlinx.coroutines.flow.Flow

interface Cache<R> {
    val cache: Flow<R>

    fun run()
}
