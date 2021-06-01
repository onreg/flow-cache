package io.github.onreg.todo.main

import androidx.lifecycle.ViewModel
import io.github.onreg.flowcache.statusCache
import io.github.onreg.todo.db.Database
import io.github.onreg.todo.db.TodoEntity

class MainViewModel : ViewModel() {

    val todos by statusCache<List<TodoEntity>> {
        Database.todoDao
            .getTodos()
    }
}