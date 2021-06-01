package io.github.onreg.flowcache.cache

import kotlinx.coroutines.flow.Flow

interface Cache<R> {
    val cache: Flow<R>

    fun run()
}
