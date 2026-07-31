package io.github.kmplitert.tool

import io.github.kmplitert.tool.image.LiteRtImage

/**
 * High-level tool extensions for LiteRT tasks.
 *
 * This object provides a unified namespace for data models and interfaces used in various AI domains,
 * such as computer vision, natural language processing, and audio/video analysis.
 */
object LiteRTExt {

    /**
     * Represents a bounding box in 2D space.
     *
     * @property left The leftmost coordinate.
     * @property top The topmost coordinate.
     * @property right The rightmost coordinate.
     * @property bottom The bottommost coordinate.
     */
    data class BoundingBox(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        /** The width of the bounding box. */
        val width: Float get() = right - left

        /** The height of the bounding box. */
        val height: Float get() = bottom - top

        /** The area of the bounding box. */
        val area: Float get() = width * height
    }

    /**
     * Represents a single classification category.
     *
     * @property label The label name of this category.
     * @property score The confidence score (probability) for this category, typically in the range [0, 1].
     * @property index The index of this category in the model's output tensor.
     */
    data class Category(val label: String, val score: Float, val index: Int)

    /**
     * Represents a single detection result.
     *
     * @property boundingBox The bounding box of the detected object.
     * @property categories A list of possible categories for the detected object, sorted by score.
     */
    data class Detection(val boundingBox: BoundingBox, val categories: List<Category>)

    /**
     * Face analysis tools and models.
     */
    object Face {
        /**
         * Represents detailed information about a detected face.
         *
         * @param boundingBox The bounding box of the face.
         * @param landmarks A list of landmarks found on the face.
         * @param orientation The 3D orientation of the face.
         * @param score The detection confidence.
         * @param emotions A map of emotion labels to confidence scores.
         */
        data class Result(
            val boundingBox: BoundingBox,
            val landmarks: List<Landmark>,
            val orientation: Orientation? = null,
            val score: Float,
            val emotions: Map<String, Float>? = null
        )

        /**
         * Represents a specific landmark on a face.
         *
         * @param x The x-coordinate.
         * @param y The y-coordinate.
         * @param type The type of landmark (e.g., [LEFT_EYE], [NOSE_TIP]).
         */
        data class Landmark(val x: Float, val y: Float, val type: Int) {
            companion object {
                const val LEFT_EYE = 0
                const val RIGHT_EYE = 1
                const val NOSE_TIP = 2
                const val MOUTH_CENTER = 3
                const val LEFT_EAR_TRAGION = 4
                const val RIGHT_EAR_TRAGION = 5
            }
        }

        /**
         * Represents the 3D orientation (Euler angles) of a face.
         *
         * @property pitch The pitch angle in degrees.
         * @property yaw The yaw angle in degrees.
         * @property roll The roll angle in degrees.
         */
        data class Orientation(val pitch: Float, val yaw: Float, val roll: Float)
    }

    /**
     * Hand tracking and gesture recognition models.
     */
    object Hand {
        /**
         * Represents a specific landmark on a hand.
         *
         * @property x The x-coordinate.
         * @property y The y-coordinate.
         * @property z The z-coordinate (optional).
         * @property type The type of hand landmark.
         */
        data class Landmark(val x: Float, val y: Float, val z: Float, val type: Int)

        /**
         * Represents a recognized hand gesture.
         *
         * @param label The label of the gesture (e.g., "palm", "pinch").
         * @param score The confidence score.
         * @param landmarks The list of hand landmarks.
         */
        data class Gesture(val label: String, val score: Float, val landmarks: List<Landmark>)
    }

    /**
     * Natural Language Processing (NLP) tools.
     */
    object Nlp {
        /**
         * Interface for KMP-compatible tokenizers.
         *
         * Tokenizers convert text into a sequence of tokens (integers) processed by models like BERT.
         */
        interface Tokenizer {
            /**
             * Converts a string into a list of token IDs.
             *
             * @param text The input text.
             * @return A list of integer token IDs.
             */
            fun tokenize(text: String): List<Int>

            /**
             * Converts a list of token IDs back into a string.
             *
             * @param tokens The list of token IDs.
             * @return The reconstructed text.
             */
            fun detokenize(tokens: List<Int>): String

            /**
             * Returns the vocabulary size of the tokenizer.
             */
            val vocabSize: Int
        }
    }

    /**
     * Human pose estimation models.
     */
    object Pose {
        /**
         * Represents a single keypoint in a pose.
         *
         * @param x The x-coordinate of the keypoint.
         * @param y The y-coordinate of the keypoint.
         * @param z The z-coordinate (optional, for 3D poses).
         * @param score The confidence score of the keypoint.
         * @param label The label or name of the keypoint (e.g., "nose", "left_shoulder").
         */
        data class Keypoint(
            val x: Float,
            val y: Float,
            val z: Float? = null,
            val score: Float,
            val label: String? = null
        )

        /**
         * Represents a detected human pose containing multiple keypoints.
         *
         * @param keypoints The list of keypoints forming the pose.
         * @param score The overall confidence score of the pose detection.
         */
        data class Result(val keypoints: List<Keypoint>, val score: Float)
    }

    /**
     * Image segmentation tools.
     */
    object Segmentation {
        /**
         * Represents a segmentation mask.
         *
         * @param width Width of the mask.
         * @param height Height of the mask.
         * @param data Flat array of mask values (confidence or category indices).
         */
        data class Mask(val width: Int, val height: Int, val data: FloatArray) {
            /**
             * Returns the mask value at the specified (x, y) coordinates.
             */
            fun getValue(x: Int, y: Int): Float {
                if (x !in 0..<width || y < 0 || y >= height) return 0f
                return data[y * width + x]
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Mask) return false
                if (width != other.width) return false
                if (height != other.height) return false
                return data.contentEquals(other.data)
            }

            override fun hashCode(): Int {
                var result = width
                result = 31 * result + height
                result = 31 * result + data.contentHashCode()
                return result
            }
        }
    }

    /**
     * Optical Character Recognition (OCR) models.
     */
    object Text {
        /**
         * Represents a single text element (typically a word).
         *
         * @property text The detected text.
         * @property boundingBox The bounding box of the element.
         * @property score The confidence score.
         */
        data class Element(val text: String, val boundingBox: BoundingBox, val score: Float)

        /**
         * Represents a line of text composed of multiple elements.
         *
         * @property text The text content of the line.
         * @property boundingBox The bounding box containing all elements in the line.
         * @property elements The list of text elements.
         */
        data class Line(val text: String, val boundingBox: BoundingBox, val elements: List<Element>)

        /**
         * Represents a block of text composed of multiple lines.
         *
         * @property text The text content of the block.
         * @property boundingBox The bounding box containing all lines in the block.
         * @property lines The list of text lines.
         */
        data class Block(val text: String, val boundingBox: BoundingBox, val lines: List<Line>)

        /**
         * The final result of an OCR task.
         *
         * @property text The full text extracted from the image.
         * @property blocks The list of detected text blocks.
         */
        data class Result(val text: String, val blocks: List<Block>)
    }

    /**
     * Video processing models.
     */
    object Video {
        /**
         * Represents a single frame in a video stream.
         *
         * @param image The underlying [LiteRtImage].
         * @param timestampMs The timestamp of the frame in milliseconds.
         * @param frameIndex The index of the frame in the sequence.
         */
        data class Frame(val image: LiteRtImage, val timestampMs: Long, val frameIndex: Long)
    }
}
