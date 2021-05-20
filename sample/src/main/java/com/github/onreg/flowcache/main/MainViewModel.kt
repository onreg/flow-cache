package com.github.onreg.flowcache.main

import androidx.lifecycle.ViewModel
import com.github.onreg.flow_cache.statusCache
import com.github.onreg.flowcache.db.Database
import com.github.onreg.flowcache.db.TodoEntity

class MainViewModel : ViewModel() {

    val todos by statusCache<List<TodoEntity>> {
        Database.todoDao
            .getTodos()
    }
}