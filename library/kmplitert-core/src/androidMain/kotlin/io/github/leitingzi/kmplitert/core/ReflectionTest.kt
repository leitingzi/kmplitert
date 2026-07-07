package io.github.leitingzi.kmplitert.core

import com.google.ai.edge.litert.TensorBuffer

fun printMethods() {
    val methods = TensorBuffer::class.java.declaredMethods
    for (m in methods) {
        println("Method: ${m.name}")
    }
}
