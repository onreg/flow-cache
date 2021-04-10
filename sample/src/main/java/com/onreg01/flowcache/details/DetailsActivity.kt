package com.onreg01.flowcache.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.onreg01.flow_cache.asDataEvent
import com.onreg01.flow_cache.asErrorEvent
import com.onreg01.flow_cache.model.Status
import com.onreg01.flowcache.R
import com.onreg01.flowcache.changeVisibility
import com.onreg01.flowcache.databinding.ActivityDetailsBinding
import com.onreg01.flowcache.sample
import com.onreg01.flowcache.throttleFirst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.appcompat.itemClicks
import reactivecircus.flowbinding.appcompat.navigationClicks
import timber.log.Timber

const val EXTRA_ID = "EXTRA_ID"

@FlowPreview
@ExperimentalCoroutinesApi
class DetailsActivity : AppCompatActivity(R.layout.activity_details) {

    @Suppress("UNCHECKED_CAST")
    private val viewModel by viewModels<DetailsViewModel> {
        object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailsViewModel(intent?.getLongExtra(EXTRA_ID, -1)) as T
            }
        }
    }

    private val binding by viewBinding(ActivityDetailsBinding::bind, R.id.main_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.screenState
            .onEach {
                val menuDelete = binding.detailsToolbar.menu.findItem(R.id.menu_delete)
                menuDelete.isVisible = it is DetailsViewModel.ScreenState.EditTodo
            }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        viewModel.todo
            .cache
            .asDataEvent()
            .onEach { binding.detailsText.setText(it.value.text) }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        viewModel.progress
            .onEach { binding.detailsProgress.changeVisibility(it) }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        viewModel.error
            .asErrorEvent()
            .onEach { Snackbar.make(binding.root, "Something went wrong!", Snackbar.LENGTH_SHORT).show() }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        merge(
            viewModel.deleteTodo.cache.filterIsInstance<Status.Data<Unit>>(),
            viewModel.saveTodo.cache.filterIsInstance()
        )
            .asDataEvent()
            .sample()
            .onEach { onBackPressed() }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        binding.detailsToolbar.itemClicks()
            .throttleFirst()
            .onEach {
                when (it.itemId) {
                    R.id.menu_delete -> viewModel.onDeleteClicked()
                    R.id.menu_save -> viewModel.onSaveClicked()
                }
            }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        binding.detailsToolbar.navigationClicks()
            .throttleFirst()
            .onEach { onBackPressed() }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        binding.detailsText
            .textChanges()
            .debounce(400)
            .map(CharSequence::toString)
            .distinctUntilChanged()
            .onEach { viewModel.onTextChanged(it) }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)


        binding.detailsText.requestFocus()
    }
}