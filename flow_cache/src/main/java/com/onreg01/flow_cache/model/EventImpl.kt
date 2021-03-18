package com.onreg01.flow_cache.model

open class EventImpl<out T>(private val content: T) : Event<T> {

    private var isHandled = false

    override fun hasBeenHandled() = isHandled

    /**
     * Returns the content and prevents its use again.
     */
    override fun getContentIfNotHandled(): T? {
        return if (isHandled) {
            null
        } else {
            consumeContent()
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    override fun peekContent(): T = content
    override fun consumeContent() {
        isHandled = true
    }
}
