package io.github.kmplitert.tool.text

/**
 * Configuration options for BERT Question Answering.
 *
 * @property topK The maximum number of top answers to return.
 */
data class BertQuestionAnswererOptions(
    val topK: Int = 1
)
