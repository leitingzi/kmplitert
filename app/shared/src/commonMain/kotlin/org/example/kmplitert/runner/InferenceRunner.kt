package org.example.kmplitert.runner

interface InferenceRunner<I, O> {

    suspend fun run(input: I): Result<O>

    suspend fun close()
}
