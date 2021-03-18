package com.onreg01.flow_cache.cache

interface ParamCache<T, R> : Cache<R> {
    fun run(value: T)
}
