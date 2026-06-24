@file:JsModule("@litertjs/core")
@file:JsNonModule

package com.leitz.kmplitert

import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
internal actual external fun loadLiteRt(path: String): Promise<JsAny>