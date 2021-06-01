package io.github.onreg.todo

import androidx.lifecycle.ViewModel
import io.github.onreg.flowcache.cache
import io.github.onreg.todo.utils.BodyExecutionHandler
import io.github.onreg.todo.utils.BodyExecutionSpy
import io.github.onreg.todo.utils.FakeViewModelDelegate
import io.github.onreg.todo.utils.MainCoroutineRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class CacheTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: FakeViewModel
    private val defaultResult = 1

    private fun init(start: Boolean) {
        viewModel = FakeViewModel(
            start,
            FakeViewModelDelegate()
        ).apply {
            result = defaultResult
        }
    }

    @Test
    fun `start = true, should start immediately`() = runBlocking {
        init(true)

        viewModel.data.cache
            .test {
                assertEquals(defaultResult, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = true, should start immediately repeat after run`() = runBlocking {
        init(true)

        viewModel.data.cache
            .test {
                assertEquals(defaultResult, expectItem())
                val result = 2
                viewModel.result = result
                viewModel.data.run()
                assertEquals(result, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = false, shouldn't start immediately`() = runBlocking {
        init(false)

        viewModel.data.cache
            .test {
                expectNoEvents()
            }
    }

    @Test
    fun `start = false, shouldn't start immediately, start after run`() = runBlocking {
        init(false)

        viewModel.data.cache
            .test {
                expectNoEvents()
                viewModel.data.run()
                assertEquals(defaultResult, expectItem())
            }
    }

    @Test
    fun `debounce similar execution`() = runBlocking {
        init(false)

        viewModel.awaitHandler = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.data.cache
                .test {
                    viewModel.data.run()
                    viewModel.data.run()
                    viewModel.data.run()
                    viewModel.awaitHandler?.complete(Unit)
                    assertEquals(defaultResult, expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions)
    }

    @Test
    fun `many subscribers single body execution`() = runBlocking {
        init(true)

        val bodyExecutions = viewModel.recordBodyExecutions {
            merge(viewModel.data.cache, viewModel.data.cache, viewModel.data.cache)
                .test {
                    assertEquals(defaultResult, expectItem())
                    assertEquals(defaultResult, expectItem())
                    assertEquals(defaultResult, expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions)
    }

    private class FakeViewModel(
        start: Boolean,
        private val baseViewModel: FakeViewModelDelegate<Int>
    ) : ViewModel(), BodyExecutionSpy by baseViewModel, BodyExecutionHandler<Int> by baseViewModel {

        val data by cache<Int?>(start) {
            run()
        }
    }
}

/*
    Start immediately
    Start immediately, repeat after run
    Don't start immediately
    Don't start immediately, start after run
*/