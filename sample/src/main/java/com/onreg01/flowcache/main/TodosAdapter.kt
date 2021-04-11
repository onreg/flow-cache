package com.onreg01.flowcache.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onreg01.flowcache.databinding.ItemTodoBinding
import com.onreg01.flowcache.db.TodoEntity
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TodosAdapter : ListAdapter<TodoEntity, TodoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private object DiffCallback : DiffUtil.ItemCallback<TodoEntity>() {
    override fun areItemsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
        return oldItem == newItem
    }

}

class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {

    private val formatter =
        DateTimeFormatter.ofPattern("dd.MMM HH:mm")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())

    fun bind(todoEntity: TodoEntity) {
        binding.itemTodoText.text = todoEntity.text
        binding.itemTodoDate.text = formatter.format(todoEntity.date)
    }
}