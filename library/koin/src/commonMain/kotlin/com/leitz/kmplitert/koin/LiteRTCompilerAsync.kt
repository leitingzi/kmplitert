package com.leitz.kmplitert.koin

import com.leitz.kmplitert.LiteRTCompiler
import kotlinx.coroutines.Deferred

/**
 * Represents an asynchronously created and initialized [LiteRTCompiler].
 *
 * This wrapper provides a dedicated type for dependency injection and avoids
 * exposing raw [Deferred] instances throughout the application.
 *
 * @property deferred Deferred that completes with the initialized compiler.
 */
class LiteRTCompilerAsync(
    val deferred: Deferred<LiteRTCompiler>
)