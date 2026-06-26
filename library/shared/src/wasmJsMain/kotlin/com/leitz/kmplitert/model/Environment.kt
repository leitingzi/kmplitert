@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert.model

external interface Environment: JsAny

external fun getDefaultEnvironment(): Environment