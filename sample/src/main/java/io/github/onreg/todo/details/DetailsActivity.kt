package io.github.onreg.todo.details

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.onreg.flowcache.asDataEvent
import io.github.onreg.flowcache.asErrorEvent
import io.github.onreg.flowcache.model.Status
import io.github.onreg.todo.*
import io.github.onreg.todo.databinding.ActivityDetailsBinding
import io.github.onreg.todo.utils.changeVisibility
import io.github.onreg.todo.utils.handleException
import io.github.onreg.todo.utils.sample
import io.github.onreg.todo.utils.throttleFirst
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
            .onEach {
                binding.detailsText.setText(it.value.text)
                binding.detailsText.setSelection(it.value.text.length)
            }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        viewModel.progress
            .onEach { binding.detailsProgress.changeVisibility(it) }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)

        viewModel.error
            .asErrorEvent()
            .onEach { handleException(binding.root, it.value) }
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
            .map(CharSequence::toString)
            .distinctUntilChanged()
            .onEach { viewModel.onTextChanged(it) }
            .catch { Timber.e(it) }
            .launchIn(lifecycleScope)


        binding.detailsText.requestFocus()
    }
}