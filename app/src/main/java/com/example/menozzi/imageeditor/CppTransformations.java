package com.example.menozzi.imageeditor;

public class CppTransformations {
    static {
        System.loadLibrary("native-lib");
    }
    private static native void greyCpp(int[] pixels);
    private static native void colorFilterCpp(int[] pixels, int red, int green, int blue);
    private static native void blurCpp(int[] pixels, int w, int h, int blur);
    private static native void brightnessCpp(int[] pixels, int brightness);
    private static native void contrastCpp(int[] pixels, int contrast);

    public static void grey(int[] pixels) {
        greyCpp(pixels);
    }

    public static void colorFilter(int[] pixels, int red, int green, int blue) {
        colorFilterCpp(pixels, red, green, blue);
    }

    public static void blur(int[] pixels, int w, int h, int blur) {
        blurCpp(pixels, w, h, blur);
    }

    public static void brightness(int[] pixels, int brightness) {
        brightnessCpp(pixels, brightness);
    }

    public static void contrast(int[] pixels, int contrast) {
        contrastCpp(pixels, contrast);
    }
}
