#include <jni.h>
#include <android/bitmap.h>
#include "yuv_converter.h"

extern "C"
JNIEXPORT void JNICALL
Java_io_github_kmplitert_tool_image_LiteRtImage_1androidKt_nativeConvertYUV(
    JNIEnv* env, jclass clazz,
    jobject y_buf, jint y_row_stride,
    jobject u_buf, jobject v_buf, jint uv_row_stride, jint uv_pixel_stride,
    jobject out_bitmap, jint width, jint height,
    jint rotation_deg, jboolean flip_h, jboolean flip_v
) {
    auto* y_data = (uint8_t*)env->GetDirectBufferAddress(y_buf);
    auto* u_data = (uint8_t*)env->GetDirectBufferAddress(u_buf);
    auto* v_data = (uint8_t*)env->GetDirectBufferAddress(v_buf);

    AndroidBitmapInfo info;
    void* pixels;
    if (AndroidBitmap_getInfo(env, out_bitmap, &info) < 0) return;
    if (AndroidBitmap_lockPixels(env, out_bitmap, &pixels) < 0) return;

    auto rot = static_cast<kmplitert::Rotation>(rotation_deg);
    kmplitert::Flip flip = { (bool)flip_h, (bool)flip_v };

    kmplitert::convertYUV420ToRGBA(
        y_data, y_row_stride,
        u_data, v_data, uv_row_stride, uv_pixel_stride,
        (uint32_t*)pixels, width, height,
        rot, flip
    );

    AndroidBitmap_unlockPixels(env, out_bitmap);
}