package io.github.onreg.todo.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "todo")
data class TodoEntity(
    val text: String,
    val date: Instant,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)