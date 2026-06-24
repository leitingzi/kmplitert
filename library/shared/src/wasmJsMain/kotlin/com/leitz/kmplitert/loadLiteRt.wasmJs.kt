@file:JsModule("@litertjs/core")
@file:OptIn(ExperimentalWasmJsInterop::class)

package com.leitz.kmplitert

import kotlin.js.Promise

internal actual external fun loadLiteRt(path: String): Promise<JsAny>