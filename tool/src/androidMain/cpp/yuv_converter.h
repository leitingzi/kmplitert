#ifndef KMPLITERT_YUV_CONVERTER_H
#define KMPLITERT_YUV_CONVERTER_H

#include <stdint.h>

namespace kmplitert {

enum class Rotation {
    ROTATION_0 = 0,
    ROTATION_90 = 90,
    ROTATION_180 = 180,
    ROTATION_270 = 270
};

struct Flip {
    bool horizontal;
    bool vertical;
};

/**
 * Converts YUV_420_888 (NV21/NV12) to RGBA.
 * Optimized with NEON if available.
 */
void convertYUV420ToRGBA(
    const uint8_t* y_data, int y_row_stride,
    const uint8_t* u_data, const uint8_t* v_data, int uv_row_stride, int uv_pixel_stride,
    uint32_t* out_rgba, int width, int height,
    Rotation rotation, Flip flip
);

} // namespace kmplitert

#endif // KMPLITERT_YUV_CONVERTER_H
