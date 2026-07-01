@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

external interface Environment: JsAny

external fun getDefaultEnvironment(): Environment