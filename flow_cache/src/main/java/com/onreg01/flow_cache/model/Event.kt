package com.onreg01.flow_cache.model

interface Event<out T> {

    fun hasBeenHandled(): Boolean

    fun getContentIfNotHandled(): T?

    fun peekContent(): T

    fun consumeContent()
}
