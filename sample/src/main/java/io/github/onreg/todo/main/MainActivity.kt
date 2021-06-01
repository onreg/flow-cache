package io.github.onreg.todo.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.onreg.flowcache.model.Status
import io.github.onreg.todo.R
import io.github.onreg.todo.databinding.ActivityMainBinding
import io.github.onreg.todo.details.DetailsActivity
import io.github.onreg.todo.details.EXTRA_ID
import io.github.onreg.todo.utils.changeVisibility
import io.github.onreg.todo.utils.handleException
import io.github.onreg.todo.utils.throttleFirst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(ActivityMainBinding::bind, R.id.main_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = TodosAdapter(lifecycleScope)
        binding.mainContent.adapter = adapter
        binding.mainContent.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        viewModel.todos
            .cache
            .onEach {
                binding.mainProgress.changeVisibility(it is Status.Loading)
                when(it){
                    is Status.Data -> adapter.submitList(it.value)
                    is Status.Error -> handleException(binding.root, it.value)
                }
            }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        binding.mainAdd.clicks()
            .throttleFirst()
            .onEach { startActivity(Intent(this, DetailsActivity::class.java)) }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        adapter.clicks
            .throttleFirst()
            .onEach {
                startActivity(Intent(this, DetailsActivity::class.java).apply {
                    putExtra(EXTRA_ID, it)
                })
            }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)
    }
}