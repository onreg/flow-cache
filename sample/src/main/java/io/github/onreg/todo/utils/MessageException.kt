package io.github.onreg.todo.utils

import java.lang.RuntimeException

class MessageException(override val message: String) : RuntimeException()
