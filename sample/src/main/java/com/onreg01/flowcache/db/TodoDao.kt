package com.onreg01.flowcache.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY date ASC")
    fun getTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo WHERE id = :id")
    suspend fun getTodo(id: Long): TodoEntity

    @Insert
    suspend fun saveTodo(todo: TodoEntity)

    @Query("DELETE FROM TODO WHERE id = :id")
    suspend fun deleteTodo(id: Long)

    @Query("DELETE FROM todo WHERE id in (:ids)")
    suspend fun deleteTodos(ids: List<Long>)
}