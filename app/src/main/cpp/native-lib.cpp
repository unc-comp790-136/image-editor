#include <jni.h>

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
    void Java_com_example_menozzi_imageeditor_MainActivity_grey(
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
    void Java_com_example_menozzi_imageeditor_MainActivity_colorFilter(
            JNIEnv* env, jobject, jintArray arr, jint red, jint green, jint blue) {
        jsize size = env->GetArrayLength(arr);
        jint* pixels = env->GetIntArrayElements(arr, nullptr);
        jint color = pack(a(pixels[0]), red, green, blue);
        for (int i = 0; i < size; i++) {
            pixels[i] = pixels[i] & color;
        }
        env->ReleaseIntArrayElements(arr, pixels, 0);
    }
}
