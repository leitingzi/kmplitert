package org.example.kmplitert

import io.github.kmplitert.tool.LiteRTHandler

data class ModelItem(
    val id: String,
    val name: String,
    val description: String,
    val runner: LiteRTHandler<*, *>,
    val defaultInputNames: List<String> = listOf("input"),
    val defaultOutputNames: List<String> = listOf("output")
)
