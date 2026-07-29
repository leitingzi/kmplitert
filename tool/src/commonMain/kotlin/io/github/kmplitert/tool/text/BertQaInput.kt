package io.github.kmplitert.tool.text

/**
 * Input data for BERT-based question answering.
 *
 * @property inputIds Tokenized IDs for context and question.
 * @property inputMask Mask for input IDs.
 * @property segmentIds Segment IDs to distinguish context and question.
 * @property tokens The original tokens used to reconstruct the answer text.
 */
data class BertQaInput(
    val inputIds: IntArray,
    val inputMask: IntArray,
    val segmentIds: IntArray,
    val tokens: List<String>
)
