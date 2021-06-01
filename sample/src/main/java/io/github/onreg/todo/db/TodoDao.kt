package io.github.onreg.todo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

const val THROTTLING = 500L

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY date DESC")
    fun getTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo WHERE id = :id")
    suspend fun getTodo(id: Long): TodoEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTodo(todo: TodoEntity)

    @Query("DELETE FROM TODO WHERE id = :id")
    suspend fun deleteTodo(id: Long)
}

class TodoDaoDelayWrapper(private val todoDao: TodoDao) : TodoDao {

    override fun getTodos(): Flow<List<TodoEntity>> {
        return flow {
            delay(THROTTLING)
            emitAll(todoDao.getTodos())
        }
    }

    override suspend fun getTodo(id: Long): TodoEntity {
        delay(THROTTLING)
        return todoDao.getTodo(id)
    }

    override suspend fun saveTodo(todo: TodoEntity) {
        delay(THROTTLING)
        todoDao.saveTodo(todo)
    }

    override suspend fun deleteTodo(id: Long) {
        delay(THROTTLING)
        todoDao.deleteTodo(id)
    }
}