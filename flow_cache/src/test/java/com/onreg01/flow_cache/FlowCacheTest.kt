package com.onreg01.flow_cache

import app.cash.turbine.test
import junit.framework.Assert.assertEquals
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
                assertEquals(1, expectItem())
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
                assertEquals(1, expectItem())
                expectNoEvents()
            }
    }

    @Test
    fun `cache, many subscribers single body execution`() = runBlocking {
        val bodyExecutions = viewModel.recordBodyExecutions {
            merge(viewModel.data.cache, viewModel.data.cache, viewModel.data.cache)
                .test {
                    assertEquals(1, expectItem())
                    assertEquals(1, expectItem())
                    assertEquals(1, expectItem())
                    expectNoEvents()
                }
        }
        assertEquals(1, bodyExecutions[DATA])
    }
}