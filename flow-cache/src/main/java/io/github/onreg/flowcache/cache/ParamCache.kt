package io.github.onreg.flowcache.cache

interface ParamCache<T, R> : Cache<R> {
    fun run(value: T)
}
