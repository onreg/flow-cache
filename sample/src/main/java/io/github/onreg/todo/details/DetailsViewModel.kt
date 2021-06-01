package io.github.onreg.todo.details

import androidx.lifecycle.ViewModel
import io.github.onreg.flowcache.model.Status
import io.github.onreg.flowcache.statusCache
import io.github.onreg.todo.db.Database
import io.github.onreg.todo.db.TodoEntity
import io.github.onreg.todo.utils.MessageException
import io.github.onreg.todo.utils.sample
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
        screenState.take(1)
            .filterIsInstance<ScreenState.EditTodo>()
            .map { Database.todoDao.getTodo(it.id) }
    }

    val deleteTodo by statusCache<Unit>(false) {
        screenState.take(1)
            .filterIsInstance<ScreenState.EditTodo>()
            .map { Database.todoDao.deleteTodo(it.id) }
    }

    val saveTodo by statusCache<String, Unit>(start = false) { text ->
        screenState.take(1)
            .map {
                if (text.isBlank()) throw MessageException("Todo shouldn't be empty!")
                val data = if (it is ScreenState.EditTodo) {
                    TodoEntity(text, Instant.now(), it.id)
                } else {
                    TodoEntity(text, Instant.now())
                }
                Database.todoDao.saveTodo(data)
            }
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