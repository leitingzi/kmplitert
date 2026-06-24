package com.leitz.kmplitert.koin

import com.leitz.kmplitert.LiteRTCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.koin.dsl.module

val kmpLiteRTModule = module {
    single {
        LiteRTCompilerFactory()
    }

    factory<Deferred<LiteRTCompiler>> { (filePath: String, scope: CoroutineScope) ->

        scope.async {
            val compiler = LiteRTCompiler()
            compiler.init(filePath = filePath)
            compiler
        }
    }
}