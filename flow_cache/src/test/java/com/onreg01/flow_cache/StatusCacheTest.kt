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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.lang.RuntimeException
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class StatusCacheTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: FakeViewModel
    private val defaultResult = 1

    private fun init(start: Boolean) {
        viewModel = FakeViewModel(
            start,
            FakeViewModelDelegate(defaultResult)
        )
    }

    @Test
    fun `start = true, should start immediately`() = runBlocking {
        init(true)

        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Result.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = true, should start immediately and after run`() = runBlocking {
        init(true)

        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Result.Data(defaultResult), expectItem())
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Result.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = false, shouldn't start immediately`() = runBlocking {
        init(false)

        viewModel.data.cache
            .test {
                viewModel.data.run()
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Result.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = true, should start immediately get an error refresh get success result`() = runBlocking {
        init(true)

        val exception = RuntimeException("Something went wrong")
        viewModel.throwable = exception
        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Result.Error(exception), expectItem())
                viewModel.throwable = null
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Result.Data(defaultResult), expectItem())
                expectNoEvents()
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
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Loading, expectItem())
                    assertEquals(Status.Result.Data(defaultResult), expectItem())
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
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Loading, expectItem())
                    assertEquals(Status.Result.Data(defaultResult), expectItem())
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Result.Data(defaultResult), expectItem())
                    assertEquals(Status.Empty, expectItem())
                    assertEquals(Status.Result.Data(defaultResult), expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions)
    }

    private class FakeViewModel(
        start: Boolean,
        private val baseViewModel: FakeViewModelDelegate<Int>
    ) : ViewModel(), BodyExecutionSpy by baseViewModel, BodyExecutionHandler<Int> by baseViewModel {

        val data by statusCache<Int>(start) {
            run()
        }
    }
}

/*
    Start immediately
    Start immediately, repeat after run
    Don't start immediately
    Don't start immediately, start after run

    Errors
*/