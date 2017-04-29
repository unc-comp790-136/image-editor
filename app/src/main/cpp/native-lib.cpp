#include <jni.h>

#include <omp.h>

#include <vector>
#include <algorithm>

#define SHIFT_A 24
#define SHIFT_R 16
#define SHIFT_G  8
#define SHIFT_B  0

extern "C" {
    // Extract individual channels from packed ARGB pixel
    static int a(int argb) { return (argb >> SHIFT_A) & 0xFF; }
    static int r(int argb) { return (argb >> SHIFT_R) & 0xFF; }
    static int g(int argb) { return (argb >> SHIFT_G) & 0xFF; }
    static int b(int argb) { return (argb >> SHIFT_B) & 0xFF; }

    // Pack channel values into a single ARGB pixel
    static int pack(int a, int r, int g, int b) {
        return (a << SHIFT_A) | (r << SHIFT_R) | (g << SHIFT_G) | (b << SHIFT_B);
    }

    // Convert image to greyscale
    void Java_com_example_menozzi_imageeditor_CppTransformations_greyCpp(
            JNIEnv* env, jobject, jintArray arr) {
        jsize size = env->GetArrayLength(arr);
        jint* pixels = env->GetIntArrayElements(arr, nullptr);
        for (int i = 0; i < size; i++) {
            int p = pixels[i];
            int grey = (int)(0.2126f*r(p) + 0.7152f*g(p) + 0.0722f*b(p));
            pixels[i] = pack(a(p), grey, grey, grey);
        }
        env->ReleaseIntArrayElements(arr, pixels, 0);
    }

    // Apply a color filter to image
    void Java_com_example_menozzi_imageeditor_CppTransformations_colorFilterCpp(
            JNIEnv* env, jobject, jintArray arr, jint red, jint green, jint blue) {
        jsize size = env->GetArrayLength(arr);
        jint* pixels = env->GetIntArrayElements(arr, nullptr);
        jfloat rscale = red/255.f;
        jfloat gscale = green/255.f;
        jfloat bscale = blue/255.f;
        for (int i = 0; i < size; i++) {
            jint newr = (jint)(r(pixels[i]) * rscale);
            jint newg = (jint)(g(pixels[i]) * gscale);
            jint newb = (jint)(b(pixels[i]) * bscale);
            pixels[i] = pack(a(pixels[i]), newr, newg, newb);
        }
        env->ReleaseIntArrayElements(arr, pixels, 0);
    }

    // Apply a box filter blur to image
    void Java_com_example_menozzi_imageeditor_CppTransformations_blurCpp(
            JNIEnv* env, jobject, jintArray arr, jint w, jint h, jint blur) {
        jsize size = env->GetArrayLength(arr);
        jint* pixels = env->GetIntArrayElements(arr, nullptr);

        // Ensure kernel size is odd
        if (blur % 2 == 0) {
            blur--;
            if (blur < 3) {
                blur = 3;
            }
        }

        // Helper lambda for translating 2D indices to a 1D index
        auto idx = [&w](jint x, jint y) -> jint {
            return x + w*y;
        };

        // Copy original pixels
        std::vector<jint> orig_pixels(pixels, pixels+size);

        // Horizontal filter
        #pragma omp parallel for
        for (int y = blur/2; y < h-(blur/2); y++) {
            for (int x = blur/2; x < w-(blur/2); x++) {
                jint i = idx(x,y);

                jint rsum = 0;
                jint gsum = 0;
                jint bsum = 0;

                int kstart = x - blur/2;
                int kend   = x + blur/2;
                for (int k = kstart; k <= kend; k++) {
                    rsum += r(orig_pixels[idx(k,y)]);
                    gsum += g(orig_pixels[idx(k,y)]);
                    bsum += b(orig_pixels[idx(k,y)]);
                }

                rsum /= blur;
                gsum /= blur;
                bsum /= blur;

                pixels[i] = pack(a(pixels[i]), rsum, gsum, bsum);
            }
        }

        // Vertical filter
        #pragma omp parallel for
        for (int y = blur/2; y < h-(blur/2); y++) {
            for (int x = blur/2; x < w-(blur/2); x++) {
                jint i = idx(x,y);

                jint rsum = 0;
                jint gsum = 0;
                jint bsum = 0;

                int kstart = y - blur/2;
                int kend   = y + blur/2;
                for (int k = kstart; k <= kend; k++) {
                    rsum += r(pixels[idx(x,k)]);
                    gsum += g(pixels[idx(x,k)]);
                    bsum += b(pixels[idx(x,k)]);
                }

                rsum /= blur;
                gsum /= blur;
                bsum /= blur;

                pixels[i] = pack(a(pixels[i]), rsum, gsum, bsum);
            }
        }

        env->ReleaseIntArrayElements(arr, pixels, 0);
    }

    // Scale brightness of every pixel
    void Java_com_example_menozzi_imageeditor_CppTransformations_brightnessCpp(
            JNIEnv* env, jobject, jintArray arr, jint brightness) {
        jsize size = env->GetArrayLength(arr);
        jint* pixels = env->GetIntArrayElements(arr, nullptr);
        if (brightness > 0) {
            #pragma omp parallel for
            for (int i = 0; i < size; i++) {
                int newr = std::min(r(pixels[i]) + brightness, 255);
                int newg = std::min(g(pixels[i]) + brightness, 255);
                int newb = std::min(b(pixels[i]) + brightness, 255);
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        } else if (brightness < 0) {
            #pragma omp parallel for
            for (int i = 0; i < size; i++) {
                int newr = std::max(r(pixels[i]) + brightness, 0);
                int newg = std::max(g(pixels[i]) + brightness, 0);
                int newb = std::max(b(pixels[i]) + brightness, 0);
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        }
        env->ReleaseIntArrayElements(arr, pixels, 0);
    }

    // Set image contrast
    void Java_com_example_menozzi_imageeditor_CppTransformations_contrastCpp(
            JNIEnv* env, jobject, jintArray arr, jint contrast) {
        jsize size = env->GetArrayLength(arr);
        jint* pixels = env->GetIntArrayElements(arr, nullptr);
        float f = (float)(259*(contrast + 255)) / (255*(259 - contrast));
        if (contrast > 0) {
            #pragma omp parallel for
            for (int i = 0; i < size; i++) {
                int newr = std::max(0, std::min((int)(f*(r(pixels[i]) - 128) + 128), 255));
                int newg = std::max(0, std::min((int)(f*(g(pixels[i]) - 128) + 128), 255));
                int newb = std::max(0, std::min((int)(f*(b(pixels[i]) - 128) + 128), 255));
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        } else if (contrast < 0) {
            #pragma omp parallel for
            for (int i = 0; i < size; i++) {
                int newr = std::max((int)(f*(r(pixels[i]) - 128) + 128), 0);
                int newg = std::max((int)(f*(g(pixels[i]) - 128) + 128), 0);
                int newb = std::max((int)(f*(b(pixels[i]) - 128) + 128), 0);
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        }
        env->ReleaseIntArrayElements(arr, pixels, 0);
    }
}
