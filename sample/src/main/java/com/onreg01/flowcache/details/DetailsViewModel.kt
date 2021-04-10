package com.onreg01.flowcache.details

import androidx.lifecycle.ViewModel
import com.onreg01.flow_cache.model.Status
import com.onreg01.flow_cache.statusCache
import com.onreg01.flowcache.db.Database
import com.onreg01.flowcache.db.TodoEntity
import com.onreg01.flowcache.sample
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.time.Instant

@FlowPreview
@ExperimentalCoroutinesApi
class DetailsViewModel(id: Long?) : ViewModel() {

    sealed class ScreenState {
        object CreateTodo : ScreenState()
        data class EditTodo(val id: Long) : ScreenState()
    }

    val screenState: Flow<ScreenState> =
        MutableStateFlow(if (id == null || id == -1L) ScreenState.CreateTodo else ScreenState.EditTodo(id))

    private var text: String = ""

    val todo by statusCache<TodoEntity> {
        screenState.filterIsInstance<ScreenState.EditTodo>()
            .map { Database.todoDao.getTodo(it.id) }
    }

    val deleteTodo by statusCache<Unit> {
        screenState.filterIsInstance<ScreenState.EditTodo>()
            .map { Database.todoDao.deleteTodo(it.id) }
    }

    val saveTodo by statusCache<String, Unit> {
        flow { emit(Database.todoDao.saveTodo(TodoEntity(it, Instant.now()))) }
    }

    val progress = combine(
        todo.cache,
        deleteTodo.cache,
        saveTodo.cache
    ) { data ->
        data.forEach {
            if (it is Status.Loading) {
                return@combine true
            }
        }
        false
    }
        .sample()

    val error = merge(
        todo.cache.filterIsInstance<Status.Error>(),
        deleteTodo.cache.filterIsInstance(),
        saveTodo.cache.filterIsInstance()
    )
        .sample()

    fun onTextChanged(text: String) {
        this.text = text
    }

    fun onSaveClicked() {
        saveTodo.run(text)
    }

    fun onDeleteClicked() {
        deleteTodo.run()
    }
}