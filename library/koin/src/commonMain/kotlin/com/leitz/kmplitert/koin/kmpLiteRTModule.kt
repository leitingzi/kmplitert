package com.leitz.kmplitert.koin

import com.leitz.kmplitert.LiteRTCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.koin.dsl.module

/**
 * LiteRT Koin module.
 *
 * Provides dependency injection for LiteRT components, including:
 * - [LiteRTCompilerFactory]: used to create and initialize LiteRTCompiler instances.
 * - [LiteRTCompilerAsync]: used to create LiteRTCompiler asynchronously using coroutines.
 *
 * ---
 *
 * ## Usage
 *
 * ### Synchronous usage (Factory)
 *
 * ```kotlin
 * val compilerFactory: LiteRTCompilerFactory = get()
 *
 * runTest {
 *     val compiler = compilerFactory.create(testFilePath)
 *     compiler.close()
 * }
 * ```
 *
 * ---
 *
 * ### Asynchronous usage (Deferred)
 *
 * ```kotlin
 * runTest {
 *     val compilerAsync: LiteRTCompilerAsync = get {
 *         parametersOf(testFilePath, this)
 *     }
 *
 *     val compiler = compilerAsync.deferred.await()
 *     compiler.close()
 * }
 * ```
 */
val kmpLiteRTModule = module {
    single {
        LiteRTCompilerFactory()
    }

    factory<LiteRTCompilerAsync> { (filePath: String, scope: CoroutineScope) ->
        val deferred = scope.async {
            val compiler = LiteRTCompiler()
            compiler.init(filePath = filePath)
            compiler
        }
        LiteRTCompilerAsync(deferred = deferred)
    }
}