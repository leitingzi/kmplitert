package com.leitz.kmplitert.koin

import com.leitz.kmplitert.LiteRTCompiler

/**
 * Factory for creating and initializing [LiteRTCompiler] instances.
 *
 * The initialization process of [LiteRTCompiler] contains suspend operations,
 * so the creation logic is encapsulated uniformly via Factory.
 */
class LiteRTCompilerFactory {
    /**
     * Creates and initializes a [LiteRTCompiler].
     *
     * @param filePath Path to the model file.
     * @return Fully initialized compiler instance.
     */
    suspend fun create(filePath: String): LiteRTCompiler {
        val compiler = LiteRTCompiler()
        compiler.init(filePath)
        return compiler
    }
}