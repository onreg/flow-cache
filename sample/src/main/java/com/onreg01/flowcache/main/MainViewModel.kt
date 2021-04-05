package com.onreg01.flowcache.main

import androidx.lifecycle.ViewModel
import com.onreg01.flow_cache.statusCache
import com.onreg01.flowcache.db.Database
import com.onreg01.flowcache.db.TodoEntity

class MainViewModel : ViewModel() {

    val todos by statusCache<List<TodoEntity>> {
        Database.todoDao
            .getTodos()
    }
}