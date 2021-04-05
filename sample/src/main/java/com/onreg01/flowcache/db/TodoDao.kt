package com.onreg01.flowcache.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY date ASC")
    fun getTodos(): Flow<List<TodoEntity>>
}