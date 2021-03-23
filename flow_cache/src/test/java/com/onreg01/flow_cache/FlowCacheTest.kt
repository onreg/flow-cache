package com.onreg01.flow_cache

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
        assertEquals(1, bodyExecutions[DATA])
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
        assertEquals(1, bodyExecutions[DATA])
    }

    @Test
    fun `param cache, initialParam == null, start = true`() = runBlocking {
        viewModel.paramData.cache
            .test {
                expectNoEvents()
            }
    }

    @Test
    fun `param cache, initialParam == null, start = false, run without param`() = runBlocking {
        viewModel = ViewModelFake(autoStart = mapOf(PARAM_DATA to false))
        viewModel.paramData.cache
            .test {
                expectNoEvents()
                viewModel.paramData.run()
                expectNoEvents()
            }
    }

    @Test
    fun `param cache, initialParam == null, start = false, run with param`() = runBlocking {
        viewModel = ViewModelFake(autoStart = mapOf(PARAM_DATA to false))
        viewModel.paramData.cache
            .test {
                expectNoEvents()
                viewModel.paramData.run("123")
                assertEquals(viewModel.results[PARAM_DATA], expectItem())
            }
    }

    @Test
    fun `param cache, initialParam != null, start = true`() = runBlocking {
        viewModel = ViewModelFake(mapOf(PARAM_DATA to "123"))
        viewModel.paramData.cache
            .test {
                assertEquals(viewModel.results[PARAM_DATA], expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `param cache, initialParam != null, start = false`() = runBlocking {
        viewModel = ViewModelFake(mapOf(PARAM_DATA to "12345"), autoStart = mapOf(PARAM_DATA to false))
        viewModel.paramData.cache
            .test {
                expectNoEvents()
                viewModel.paramData.run()
                assertEquals(viewModel.results[PARAM_DATA], expectItem())
            }
    }

    @Test
    fun `param cache, debounce similar execution`() = runBlocking {
        viewModel = ViewModelFake(autoStart = mapOf(PARAM_DATA to false))
        viewModel.awaitHandlers[PARAM_DATA] = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.paramData.cache
                .test {
                    viewModel.paramData.run("12345")
                    viewModel.paramData.run("12345")
                    viewModel.paramData.run("12345")
                    viewModel.awaitHandlers[PARAM_DATA]?.complete(Unit)
                    assertEquals(viewModel.results[PARAM_DATA], expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions[PARAM_DATA])
    }

    @Test
    fun `param cache, cancel previous execution if param changed`() = runBlocking {
        viewModel = ViewModelFake(autoStart = mapOf(PARAM_DATA to false))
        viewModel.awaitHandlers[PARAM_DATA] = CompletableDeferred()
        val bodyExecutions = viewModel.recordBodyExecutions {
            viewModel.paramData.cache
                .test {
                    viewModel.paramData.run("123")
                    viewModel.paramData.run("1234")
                    viewModel.paramData.run("12345")
                    viewModel.awaitHandlers[PARAM_DATA]?.complete(Unit)
                    assertEquals(viewModel.results[PARAM_DATA], expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(3, bodyExecutions[PARAM_DATA])
    }

    @Test
    fun `param cache, many subscribers single body execution`() = runBlocking {
        viewModel = ViewModelFake(mapOf(PARAM_DATA to "123"))
        val bodyExecutions = viewModel.recordBodyExecutions {
            merge(viewModel.paramData.cache, viewModel.paramData.cache, viewModel.paramData.cache)
                .test {
                    assertEquals(viewModel.results[PARAM_DATA], expectItem())
                    assertEquals(viewModel.results[PARAM_DATA], expectItem())
                    assertEquals(viewModel.results[PARAM_DATA], expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions[PARAM_DATA])
    }
}