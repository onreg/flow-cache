package io.github.onreg.flowcache.model

interface StatusEvent<out T> {
    val consumed: Boolean
    val value: T
}

class StatusEventImpl<out T>(value: T) : StatusEvent<T> {
    override var consumed = false
        private set

    override val value: T = value
        get() {
            consumed = true
            return field
        }
}