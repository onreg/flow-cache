package com.github.onreg.flow_cache.cache

interface ParamCache<T, R> : Cache<R> {
    fun run(value: T)
}
