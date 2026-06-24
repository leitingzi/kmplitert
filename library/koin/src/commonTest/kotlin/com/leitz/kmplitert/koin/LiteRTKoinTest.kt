package com.leitz.kmplitert.koin

import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LiteRTKoinTest: KoinTest {

    private val testFilePath = "src/commonTest/resources/CelsiusToFahrenheit.tflite"

    @BeforeTest
    fun before() {
        startKoin {
            modules(kmpLiteRTModule)
        }
    }

    @AfterTest
    fun after() {
        stopKoin()
    }

    @Test
    fun `should load model`() {
        val compilerFactory: LiteRTCompilerFactory = get()

        runTest {
            val compiler1 = compilerFactory.create(testFilePath)
            compiler1.close()

            val compilerAsync: LiteRTCompilerAsync = get {
                parametersOf(testFilePath, this)
            }

            val compiler2 = compilerAsync.deferred.await()
            compiler2.close()
        }
    }
}