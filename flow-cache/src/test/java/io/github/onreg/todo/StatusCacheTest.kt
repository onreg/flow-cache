package io.github.onreg.todo

import androidx.lifecycle.ViewModel
import app.cash.turbine.test
import io.github.onreg.flowcache.asDataEvent
import io.github.onreg.flowcache.asErrorEvent
import io.github.onreg.flowcache.asEvent
import io.github.onreg.flowcache.model.Status
import io.github.onreg.flowcache.statusCache
import io.github.onreg.todo.utils.BodyExecutionHandler
import io.github.onreg.todo.utils.BodyExecutionSpy
import io.github.onreg.todo.utils.FakeViewModelDelegate
import io.github.onreg.todo.utils.MainCoroutineRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class StatusCacheTest {

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
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
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
                assertEquals(Status.Data(defaultResult), expectItem())
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = false, shouldn't start immediately`() = runBlocking {
        init(false)

        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = false, shouldn't start immediately, start after run`() = runBlocking {
        init(false)

        viewModel.data.cache
            .test {
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
                viewModel.data.run()
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }
    }


    @Test
    fun `start = true, should start immediately, get an error, refresh, get successful result`() = runBlocking {
        init(true)

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
        init(true)

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
                    assertEquals(Status.Data(defaultResult), expectItem())
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

    @Test
    fun `one shot events`() = runBlocking {
        init(true)
        viewModel.data.cache
            .asEvent()
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }

        viewModel.data.cache
            .asEvent()
            .test {
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
            }

        init(true)
        val exception = RuntimeException("Something went wrong")
        viewModel.throwable = exception

        viewModel.data.cache
            .asEvent()
            .test {
                assertEquals(Status.Empty, expectItem())
                assertEquals(Status.Loading, expectItem())
                assertEquals(Status.Error(exception), expectItem())
                expectNoEvents()
            }

        viewModel.data.cache
            .asEvent()
            .test {
                assertEquals(Status.Empty, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `one shot data`() = runBlocking {
        init(true)
        viewModel.data.cache
            .asDataEvent()
            .test {
                assertEquals(Status.Data(defaultResult), expectItem())
                expectNoEvents()
            }

        viewModel.data.cache
            .asDataEvent()
            .test {
                expectNoEvents()
            }
    }

    @Test
    fun `one shot error`() = runBlocking {
        init(true)
        val exception = RuntimeException("Something went wrong")
        viewModel.throwable = exception

        viewModel.data.cache
            .asErrorEvent()
            .test {
                assertEquals(Status.Error(exception), expectItem())
                expectNoEvents()
            }

        viewModel.data.cache
            .asErrorEvent()
            .test {
                expectNoEvents()
            }
    }

    private class FakeViewModel(
        start: Boolean,
        private val baseViewModel: FakeViewModelDelegate<Int>
    ) : ViewModel(), BodyExecutionSpy by baseViewModel, BodyExecutionHandler<Int> by baseViewModel {

        var emptyFlow: Boolean = false

        val data by statusCache<Int?>(start) {
            if (emptyFlow) return@statusCache emptyFlow<Int>()
            run()
        }
    }
}

/*
    Start immediately
    Start immediately, repeat after run
    Don't start immediately
    Don't start immediately, start after run

    Get an error, refresh, get successful result
    Get an empty status, refresh, get successful result

    One shot event
    One show error
    One show data
*/