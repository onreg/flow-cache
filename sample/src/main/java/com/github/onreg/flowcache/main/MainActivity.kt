package com.github.onreg.flowcache.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.onreg.flow_cache.model.Status
import com.github.onreg.flowcache.R
import com.github.onreg.flowcache.databinding.ActivityMainBinding
import com.github.onreg.flowcache.details.DetailsActivity
import com.github.onreg.flowcache.details.EXTRA_ID
import com.github.onreg.flowcache.utils.changeVisibility
import com.github.onreg.flowcache.utils.handleException
import com.github.onreg.flowcache.utils.throttleFirst
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