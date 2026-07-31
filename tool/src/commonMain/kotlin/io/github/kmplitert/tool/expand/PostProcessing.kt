package io.github.kmplitert.tool.expand

import io.github.kmplitert.tool.LiteRTExt
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/* ---------- Classification Post-processing ---------- */

/**
 * Computes the Softmax probabilities for the [FloatArray].
 */
fun FloatArray.softmax(): FloatArray {
    if (isEmpty()) return floatArrayOf()
    val max = this.maxOrNull() ?: 0f
    val exp = FloatArray(size) { exp(this[it] - max) }
    val sum = exp.sum()
    return FloatArray(size) { exp[it] / sum }
}

/**
 * Computes the Sigmoid function for each element in the [FloatArray].
 */
fun FloatArray.sigmoid(): FloatArray {
    return FloatArray(size) { 1.0f / (1.0f + exp(-this[it])) }
}

/**
 * Returns the index of the maximum value in the [FloatArray].
 *
 * Returns -1 if the array is empty.
 */
fun FloatArray.argmax(): Int {
    if (isEmpty()) return -1
    var maxIdx = 0
    var maxVal = this[0]
    for (i in 1 until size) {
        if (this[i] > maxVal) {
            maxVal = this[i]
            maxIdx = i
        }
    }
    return maxIdx
}

/**
 * Returns the top [k] elements as a list of `Pair(index, score)`.
 */
fun FloatArray.topK(k: Int): List<Pair<Int, Float>> {
    return indices.map { it to this[it] }
        .sortedByDescending { it.second }
        .take(k)
}

/**
 * Converts the [FloatArray] scores into a list of [LiteRTExt.Category].
 *
 * @param labels The labels corresponding to each index.
 * @param threshold The minimum score to include a category.
 * @return A sorted list of categories.
 */
fun FloatArray.toCategories(labels: List<String>, threshold: Float = 0f): List<LiteRTExt.Category> {
    return mapIndexed { index, score ->
        LiteRTExt.Category(
            label = labels.getOrElse(index) { "Unknown" },
            score = score,
            index = index
        )
    }.filter { it.score >= threshold }
        .sortedByDescending { it.score }
}

/* ---------- Detection Post-processing ---------- */

/**
 * Calculates the Intersection over Union (IoU) of two bounding boxes.
 *
 * The format of each box must be `[left, top, right, bottom]`.
 */
fun calculateIou(box1: FloatArray, box2: FloatArray): Float {
    val x1 = max(box1[0], box2[0])
    val y1 = max(box1[1], box2[1])
    val x2 = min(box1[2], box2[2])
    val y2 = min(box1[3], box2[3])

    val intersectionWidth = max(0f, x2 - x1)
    val intersectionHeight = max(0f, y2 - y1)
    val intersectionArea = intersectionWidth * intersectionHeight

    val area1 = (box1[2] - box1[0]) * (box1[3] - box1[1])
    val area2 = (box2[2] - box2[0]) * (box2[3] - box2[1])
    val unionArea = area1 + area2 - intersectionArea

    return if (unionArea > 0) intersectionArea / unionArea else 0f
}

/**
 * Performs Non-Maximum Suppression (NMS) on a set of bounding boxes.
 *
 * @param boxes An array of bounding boxes, where each box is `[left, top, right, bottom]`.
 * @param scores A float array containing confidence scores for each box.
 * @param iouThreshold The IoU threshold above which boxes are considered overlapping.
 * @return A list of indices of the selected boxes.
 */
fun performNms(
    boxes: Array<FloatArray>,
    scores: FloatArray,
    iouThreshold: Float
): List<Int> {
    val indices = scores.indices.sortedByDescending { scores[it] }.toMutableList()
    val selected = mutableListOf<Int>()

    while (indices.isNotEmpty()) {
        val current = indices.removeAt(0)
        selected.add(current)

        val iterator = indices.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (calculateIou(boxes[current], boxes[next]) > iouThreshold) {
                iterator.remove()
            }
        }
    }
    return selected
}
