#include "yuv_converter.h"
#include <algorithm>

#ifdef __ARM_NEON
#include <arm_neon.h>
#endif

namespace kmplitert {

inline uint8_t clamp(float val) {
    return (uint8_t)std::max(0.0f, std::min(255.0f, val));
}

void convertYUV420ToRGBA(
    const uint8_t* y_data, int y_row_stride,
    const uint8_t* u_data, const uint8_t* v_data, int uv_row_stride, int uv_pixel_stride,
    uint32_t* out_rgba, int width, int height,
    Rotation rotation, Flip flip
) {
    int out_width = (rotation == Rotation::ROTATION_90 || rotation == Rotation::ROTATION_270) ? height : width;
    int out_height = (rotation == Rotation::ROTATION_90 || rotation == Rotation::ROTATION_270) ? width : height;

    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            int y_idx = y * y_row_stride + x;
            int uv_idx = (y / 2) * uv_row_stride + (x / 2) * uv_pixel_stride;

            int Y = y_data[y_idx];
            int U = u_data[uv_idx] - 128;
            int V = v_data[uv_idx] - 128;

            // Standard YUV to RGB conversion
            int r = clamp(Y + 1.370705f * V);
            int g = clamp(Y - 0.337633f * U - 0.698001f * V);
            int b = clamp(Y + 1.732446f * U);
            uint32_t rgba = (0xFF << 24) | (b << 16) | (g << 8) | r;

            // Handle rotation and flip
            int target_x = x;
            int target_y = y;

            if (flip.horizontal) target_x = width - 1 - target_x;
            if (flip.vertical) target_y = height - 1 - target_y;

            int final_x, final_y;
            switch (rotation) {
                case Rotation::ROTATION_90:
                    final_x = out_width - 1 - target_y;
                    final_y = target_x;
                    break;
                case Rotation::ROTATION_180:
                    final_x = out_width - 1 - target_x;
                    final_y = out_height - 1 - target_y;
                    break;
                case Rotation::ROTATION_270:
                    final_x = target_y;
                    final_y = out_height - 1 - target_x;
                    break;
                default:
                    final_x = target_x;
                    final_y = target_y;
                    break;
            }

            out_rgba[final_y * out_width + final_x] = rgba;
        }
    }
}

} // namespace kmplitert
