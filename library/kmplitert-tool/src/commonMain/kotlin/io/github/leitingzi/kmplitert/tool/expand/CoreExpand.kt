package io.github.leitingzi.kmplitert.tool.expand

import io.github.leitingzi.kmplitert.core.LiteRTCompiler
import io.github.leitingzi.kmplitert.core.TFBuffer
import kotlin.math.roundToInt

/* ---------- TFBuffer Extensions ---------- */

/**
 * Writes the [FloatArray] data into the [TFBuffer].
 */
fun FloatArray.writeTo(buffer: TFBuffer) = buffer.writeFloat(this)

/**
 * Writes the [IntArray] data into the [TFBuffer].
 */
fun IntArray.writeTo(buffer: TFBuffer) = buffer.writeInt(this)

/**
 * Writes the [ByteArray] data into the [TFBuffer].
 */
fun ByteArray.writeTo(buffer: TFBuffer) = buffer.writeInt8(this)

/**
 * Writes the [BooleanArray] data into the [TFBuffer].
 */
fun BooleanArray.writeTo(buffer: TFBuffer) = buffer.writeBoolean(this)

/**
 * Writes the [LongArray] data into the [TFBuffer].
 */
fun LongArray.writeTo(buffer: TFBuffer) = buffer.writeLong(this)

/**
 * Reads the content of the [TFBuffer] as a [FloatArray].
 */
suspend fun TFBuffer.toFloatArray(): FloatArray = readFloat()

/**
 * Reads the content of the [TFBuffer] as an [IntArray].
 */
suspend fun TFBuffer.toIntArray(): IntArray = readInt()

/**
 * Reads the content of the [TFBuffer] as a [ByteArray].
 */
suspend fun TFBuffer.toByteArray(): ByteArray = readInt8()

/**
 * Reads the content of the [TFBuffer] as a [BooleanArray].
 */
suspend fun TFBuffer.toBooleanArray(): BooleanArray = readBoolean()

/**
 * Reads the content of the [TFBuffer] as a [LongArray].
 */
suspend fun TFBuffer.toLongArray(): LongArray = readLong()

/**
 * Writes a single [Float] value into the [TFBuffer].
 */
fun TFBuffer.writeFloatValue(value: Float) = writeFloat(floatArrayOf(value))

/**
 * Reads a single [Float] value from the [TFBuffer].
 */
suspend fun TFBuffer.readFloatValue(): Float = readFloat().firstOrNull() ?: 0f

/* ---------- LiteRTCompiler Extensions ---------- */

/**
 * Returns the [TFBuffer] for the input tensor at the specified [index].
 */
suspend fun LiteRTCompiler.getInputBuffer(index: Int): TFBuffer = getInputBuffers()[index]

/**
 * Returns the [TFBuffer] for the output tensor at the specified [index].
 */
suspend fun LiteRTCompiler.getOutputBuffer(index: Int): TFBuffer = getOutputBuffers()[index]
