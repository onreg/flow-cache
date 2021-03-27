package com.onreg01.flow_cache

import androidx.lifecycle.ViewModel
import app.cash.turbine.test
import com.onreg01.flow_cache.model.Status
import com.onreg01.flow_cache.utils.BodyExecutionHandler
import com.onreg01.flow_cache.utils.BodyExecutionSpy
import com.onreg01.flow_cache.utils.FakeViewModelDelegate
import com.onreg01.flow_cache.utils.MainCoroutineRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class ParamStatusCacheTest {
    @ExperimentalCoroutinesApi
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
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `initialParam == null, start = false, should'n start immediately and after run()`() = runBlocking {
        init(null, false)
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
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
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
                viewModel.data.run("123")
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
            }
    }

    @Test
    fun `initialParam != null, start = false, should'n start immediately, start after run()`() = runBlocking {
        init("12345", false)
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
            }
    }

    @Test
    fun `initialParam != null, start = true, should start immediately`() = runBlocking {
        init("12345", true)
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `initialParam != null, start = true, should start immediately and after run`() = runBlocking {
        init("12345", true)
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = true, should start immediately, get an error, refresh, get successful result`() = runBlocking {
        init("12345", true)

        val exception = RuntimeException("Something went wrong")
        viewModel.throwable = exception
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Error(exception), expectItem())
                viewModel.throwable = null
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = true, should start immediately, get an empty status, refresh, get successful result`() = runBlocking {
        init("12345", true)

        viewModel.emptyFlow = true
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Empty, expectItem())
                viewModel.emptyFlow = false
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `debounce similar execution`() = runBlocking {
        init("12345", false)

        viewModel.awaitHandler = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.data.cache
                .test {
                    viewModel.data.run()
                    viewModel.data.run()
                    viewModel.data.run()
                    viewModel.awaitHandler?.complete(Unit)
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Loading, expectItem())
                    assertEquals(Status.Data(defaultResult), expectItem())
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
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Loading, expectItem())
                    assertEquals(Status.Data(defaultResult), expectItem())
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
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Loading, expectItem())
                    assertEquals(Status.Data(defaultResult), expectItem())
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Data(defaultResult), expectItem())
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Data(defaultResult), expectItem())
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

        var emptyFlow: Boolean = false

        val data by statusCache(initialParam, start) {
            if (emptyFlow) return@statusCache emptyFlow<Int>()
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

    Get an error, refresh, get successful result
    Get an empty status, refresh, get successful result
*/