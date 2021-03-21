package com.onreg01.flow_cache

import app.cash.turbine.test
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime


@ExperimentalCoroutinesApi
@ExperimentalTime
class FlowCacheTest {

    lateinit var viewModel: ViewModelFake

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        viewModel = ViewModelFake()
    }

    @Test
    fun `cache, start = true`() = runBlocking {
        viewModel.data.cache
            .test {
                assertEquals(viewModel.results[DATA], expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `cache, start = false`() = runBlocking {
        viewModel = ViewModelFake(autoStart = mapOf(DATA to false))
        viewModel.data.cache
            .test {
                expectNoEvents()
                viewModel.data.run()
                assertEquals(viewModel.results[DATA], expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `cache, debounce similar execution`() = runBlocking {
        viewModel = ViewModelFake(autoStart = mapOf(DATA to false))
        viewModel.awaitHandlers[DATA] = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.data.cache
                .test {
                    viewModel.data.run()
                    viewModel.data.run()
                    viewModel.data.run()
                    viewModel.awaitHandlers[DATA]?.complete(Unit)
                    assertEquals(viewModel.results[DATA], expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(viewModel.results[DATA], bodyExecutions[DATA])
    }

    @Test
    fun `cache, many subscribers single body execution`() = runBlocking {
        val bodyExecutions = viewModel.recordBodyExecutions {
            merge(viewModel.data.cache, viewModel.data.cache, viewModel.data.cache)
                .test {
                    assertEquals(viewModel.results[DATA], expectItem())
                    assertEquals(viewModel.results[DATA], expectItem())
                    assertEquals(viewModel.results[DATA], expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(viewModel.results[DATA], bodyExecutions[DATA])
    }
}