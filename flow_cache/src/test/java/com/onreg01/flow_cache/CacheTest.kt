package com.onreg01.flow_cache

import androidx.lifecycle.ViewModel
import app.cash.turbine.test
import com.onreg01.flow_cache.utils.BodyExecutionHandler
import com.onreg01.flow_cache.utils.BodyExecutionSpy
import com.onreg01.flow_cache.utils.FakeViewModelDelegate
import com.onreg01.flow_cache.utils.MainCoroutineRule
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
            FakeViewModelDelegate(defaultResult)
        )
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
    fun `start = true, should start immediately and after run`() = runBlocking {
        init(true)

        viewModel.data.cache
            .test {
                assertEquals(defaultResult, expectItem())
                viewModel.data.run()
                assertEquals(defaultResult, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `start = false, shouldn't start immediately`() = runBlocking {
        init(false)

        viewModel.data.cache
            .test {
                expectNoEvents()
                viewModel.data.run()
                assertEquals(defaultResult, expectItem())
            }
    }

    @Test
    fun `cache, debounce similar execution`() = runBlocking {
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
    fun `cache, many subscribers single body execution`() = runBlocking {
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

        val data by cache<Int>(start) {
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