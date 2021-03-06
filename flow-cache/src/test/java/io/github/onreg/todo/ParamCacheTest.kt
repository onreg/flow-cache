package io.github.onreg.todo

import androidx.lifecycle.ViewModel
import app.cash.turbine.test
import io.github.onreg.flowcache.cache
import io.github.onreg.todo.utils.BodyExecutionHandler
import io.github.onreg.todo.utils.BodyExecutionSpy
import io.github.onreg.todo.utils.FakeViewModelDelegate
import io.github.onreg.todo.utils.MainCoroutineRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ParamCacheTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: FakeViewModel
    private val defaultResult = 1

    private fun init(initialParam: String?, start: Boolean) {
        viewModel = FakeViewModel(
            initialParam,
            start,
            FakeViewModelDelegate()
        ).apply {
            result = defaultResult
        }
    }

    @Test
    fun `initialParam == null, start = false, should'n start immediately`() = runBlocking {
        init(null, true)

        viewModel.data.cache
            .test {
                expectNoEvents()
            }
    }

    @Test
    fun `initialParam == null, start = false, should'n start immediately and after run()`() = runBlocking {
        init(null, false)
        viewModel.data.cache
            .test {
                expectNoEvents()
                viewModel.data.run()
                expectNoEvents()
            }
    }

    @Test
    fun `initialParam == null, start = false, should'n start immediately, start after run(param)`() = runBlocking {
        init(null, false)
        viewModel.data.cache
            .test {
                expectNoEvents()
                viewModel.data.run("123")
                assertEquals(defaultResult, expectItem())
            }
    }

    @Test
    fun `initialParam != null, start = false, should'n start immediately, start after run()`() = runBlocking {
        init("12345", false)
        viewModel.data.cache
            .test {
                expectNoEvents()
                viewModel.data.run()
                assertEquals(defaultResult, expectItem())
            }
    }

    @Test
    fun `initialParam != null, start = true, should start immediately`() = runBlocking {
        init("12345", true)
        viewModel.data.cache
            .test {
                assertEquals(defaultResult, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `initialParam != null, start = true, should start immediately and after run`() = runBlocking {
        init("12345", true)
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
    fun `debounce similar execution`() = runBlocking {
        init(null, false)
        viewModel.awaitHandler = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.data.cache
                .test {
                    viewModel.data.run("12345")
                    viewModel.data.run("12345")
                    viewModel.data.run("12345")
                    viewModel.awaitHandler?.complete(Unit)
                    assertEquals(defaultResult, expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions)
    }

    @Test
    fun `cancel previous execution if param changed`() = runBlocking {
        init(null, false)
        viewModel.awaitHandler = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.data.cache
                .test {
                    viewModel.data.run("123")
                    viewModel.data.run("1234")
                    viewModel.data.run("12345")
                    viewModel.awaitHandler?.complete(Unit)
                    assertEquals(defaultResult, expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(3, bodyExecutions)
    }

    @Test
    fun `many subscribers single body execution`() = runBlocking {
        init("12345", true)
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
        initialParam: String?,
        start: Boolean,
        private val baseViewModel: FakeViewModelDelegate<Int>
    ) : ViewModel(), BodyExecutionSpy by baseViewModel, BodyExecutionHandler<Int> by baseViewModel {
        val data by cache(initialParam, start) {
            run()
        }
    }
}

/*
    Don't start immediately
    Don't start immediately, don't start after run()
    Don't start immediately, start after run() (initial param)
    Don't start immediately start after run(param)
    Start immediately
    Start immediately repeat after run
*/