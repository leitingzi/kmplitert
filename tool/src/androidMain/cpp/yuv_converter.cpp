#include "yuv_converter.h"
#include <algorithm>

#ifdef __ARM_NEON
#include <arm_neon.h>
#endif

namespace kmplitert {

inline uint8_t clamp(float val) {
    return (uint8_t)std::max(0.0f, std::min(255.0f, val));
}

#ifdef __ARM_NEON
// NEON implementation for YUV420 to RGBA conversion
void convertYUV420ToRGBA_NEON(
    const uint8_t* y_data, int y_row_stride,
    const uint8_t* u_data, const uint8_t* v_data, int uv_row_stride, int uv_pixel_stride,
    uint32_t* out_rgba, int width, int height,
    Rotation rotation, Flip flip
) {
    int out_width = (rotation == Rotation::ROTATION_90 || rotation == Rotation::ROTATION_270) ? height : width;

    // Constants for YUV to RGB conversion (fixed point 8-bit)
    uint8x8_t v128 = vdup_n_u8(128);
    int16x8_t v351 = vdupq_n_s16(351);
    int16x8_t v88 = vdupq_n_s16(88);
    int16x8_t v183 = vdupq_n_s16(183);
    int16x8_t v454 = vdupq_n_s16(454);
    uint8x8_t v255 = vdup_n_u8(255);
    uint8x8_t v0 = vdup_n_u8(0);

    for (int y = 0; y < height; ++y) {
        const uint8_t* y_ptr = y_data + y * y_row_stride;
        const uint8_t* u_ptr = u_data + (y / 2) * uv_row_stride;
        const uint8_t* v_ptr = v_data + (y / 2) * uv_row_stride;

        for (int x = 0; x < width; x += 8) {
            // Load 8 Y pixels
            uint8x8_t y_val = vld1_u8(y_ptr + x);

            // Load 4 U and 4 V pixels
            uint8x8_t u_val_raw, v_val_raw;
            if (uv_pixel_stride == 1) {
                u_val_raw = vld1_u8(u_ptr + x / 2);
                v_val_raw = vld1_u8(v_ptr + x / 2);
            } else {
                // Interleaved (e.g. NV21 or NV12)
                uint8x8x2_t uv_interleaved = vld2_u8(u_ptr + (x / 2) * uv_pixel_stride);
                u_val_raw = uv_interleaved.val[0];
                v_val_raw = uv_interleaved.val[1];
            }

            // Upsample U and V (4 -> 8)
            // vzip_u8 returns a pair of uint8x8_t, we use the first one to duplicate
            uint8x8x2_t u_pair = vzip_u8(u_val_raw, u_val_raw);
            uint8x8x2_t v_pair = vzip_u8(v_val_raw, v_val_raw);
            uint8x8_t u_val = u_pair.val[0];
            uint8x8_t v_val = v_pair.val[0];

            // Adjust YUV: Y is uint8, U/V need -128
            int16x8_t Y_s16 = vreinterpretq_s16_u16(vmovl_u8(y_val));
            int16x8_t U_s16 = vsubq_s16(vreinterpretq_s16_u16(vmovl_u8(u_val)), vreinterpretq_s16_u16(vmovl_u8(v128)));
            int16x8_t V_s16 = vsubq_s16(vreinterpretq_s16_u16(vmovl_u8(v_val)), vreinterpretq_s16_u16(vmovl_u8(v128)));

            // R = Y + (351 * V) >> 8
            int16x8_t R_s16 = vaddq_s16(Y_s16, vshrq_n_s16(vmulq_s16(V_s16, v351), 8));
            // G = Y - (88 * U + 183 * V) >> 8
            int16x8_t G_s16 = vsubq_s16(Y_s16, vshrq_n_s16(vaddq_s16(vmulq_s16(U_s16, v88), vmulq_s16(V_s16, v183)), 8));
            // B = Y + (454 * U) >> 8
            int16x8_t B_s16 = vaddq_s16(Y_s16, vshrq_n_s16(vmulq_s16(U_s16, v454), 8));

            // Clamp to [0, 255]
            uint8x8_t R_u8 = vqmovun_s16(R_s16);
            uint8x8_t G_u8 = vqmovun_s16(G_s16);
            uint8x8_t B_u8 = vqmovun_s16(B_s16);
            uint8x8_t A_u8 = vdup_n_u8(255);

            // Pack RGBA (Note: Android Bitmap is often RGBA or BGRA depending on config,
            // but JNI graphics usually expects specific order. We'll follow scalar logic.)
            // Scalar logic: (0xFF << 24) | (b << 16) | (g << 8) | r -> BGRA in memory (little endian)
            uint8x8x4_t rgba;
            rgba.val[0] = R_u8;
            rgba.val[1] = G_u8;
            rgba.val[2] = B_u8;
            rgba.val[3] = A_u8;

            // If no rotation/flip, we can store 8 pixels at once
            if (rotation == Rotation::ROTATION_0 && !flip.horizontal && !flip.vertical) {
                vst4_u8((uint8_t*)(out_rgba + y * width + x), rgba);
            } else {
                // Slower path: Handle rotation/flip pixel by pixel but still use NEON for calculation
                uint32_t temp_rgba[8];
                vst4_u8((uint8_t*)temp_rgba, rgba);

                for (int i = 0; i < 8; ++i) {
                    int curr_x = x + i;
                    if (curr_x >= width) break;

                    int tx = curr_x, ty = y;
                    if (flip.horizontal) tx = width - 1 - tx;
                    if (flip.vertical) ty = height - 1 - ty;

                    int fx, fy;
                    if (rotation == Rotation::ROTATION_90) {
                        fx = out_width - 1 - ty;
                        fy = tx;
                    } else if (rotation == Rotation::ROTATION_180) {
                        fx = out_width - 1 - tx;
                        fy = height - 1 - ty;
                    } else if (rotation == Rotation::ROTATION_270) {
                        fx = ty;
                        fy = out_width - 1 - tx;
                    } else {
                        fx = tx;
                        fy = ty;
                    }
                    out_rgba[fy * out_width + fx] = temp_rgba[i];
                }
            }
        }
    }
}
#endif

void convertYUV420ToRGBA(
    const uint8_t* y_data, int y_row_stride,
    const uint8_t* u_data, const uint8_t* v_data, int uv_row_stride, int uv_pixel_stride,
    uint32_t* out_rgba, int width, int height,
    Rotation rotation, Flip flip
) {
    if (!y_data || !u_data || !v_data || !out_rgba) return;

#ifdef __ARM_NEON
    // Use NEON if width is multiple of 8 for simplicity, or handle tail separately.
    // For most camera frames, width is a multiple of 8 (e.g. 640, 1280, 1920).
    if (width % 8 == 0) {
        convertYUV420ToRGBA_NEON(y_data, y_row_stride, u_data, v_data, uv_row_stride, uv_pixel_stride, out_rgba, width, height, rotation, flip);
        return;
    }
#endif

    int out_width = (rotation == Rotation::ROTATION_90 || rotation == Rotation::ROTATION_270) ? height : width;
    int out_height = (rotation == Rotation::ROTATION_90 || rotation == Rotation::ROTATION_270) ? width : height;

    // Use a pre-calculated mapping to avoid heavy branching in the inner loop
    // This allows better pipelining.
    for (int y = 0; y < height; ++y) {
        const uint8_t* y_row = y_data + y * y_row_stride;
        const uint8_t* u_row = u_data + (y / 2) * uv_row_stride;
        const uint8_t* v_row = v_data + (y / 2) * uv_row_stride;

        for (int x = 0; x < width; ++x) {
            int uv_col = (x / 2) * uv_pixel_stride;

            int Y = y_row[x];
            int U = u_row[uv_col] - 128;
            int V = v_row[uv_col] - 128;

            // Optimized integer math for YUV to RGB (approximates the float version)
            // R = Y + (1.37 * V) -> Y + (351 * V) >> 8
            // G = Y - (0.344 * U + 0.714 * V) -> Y - (88 * U + 183 * V) >> 8
            // B = Y + (1.77 * U) -> Y + (454 * U) >> 8

            int r = std::max(0, std::min(255, Y + ((351 * V) >> 8)));
            int g = std::max(0, std::min(255, Y - ((88 * U + 183 * V) >> 8)));
            int b = std::max(0, std::min(255, Y + ((454 * U) >> 8)));

            uint32_t rgba = (0xFF << 24) | (b << 16) | (g << 8) | r;

            int tx = x, ty = y;
            if (flip.horizontal) tx = width - 1 - tx;
            if (flip.vertical) ty = height - 1 - ty;

            int fx, fy;
            if (rotation == Rotation::ROTATION_90) {
                fx = out_width - 1 - ty;
                fy = tx;
            } else if (rotation == Rotation::ROTATION_180) {
                fx = out_width - 1 - tx;
                fy = out_height - 1 - ty;
            } else if (rotation == Rotation::ROTATION_270) {
                fx = ty;
                fy = out_height - 1 - tx;
            } else {
                fx = tx;
                fy = ty;
            }

            out_rgba[fy * out_width + fx] = rgba;
        }
    }
}

} // namespace kmplitert
