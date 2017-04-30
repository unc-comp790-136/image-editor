package com.example.menozzi.imageeditor;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

public class PerfRunnerTask extends AsyncTask<Void, Void, String> {
    private static final String BASE_TAG = "PerfRunnerTask";
    private static final int NUM_RUNS = 5;

    private int[] mPixels;
    private int mWidth;
    private int mHeight;
    private int mBlurKernelSize;

    public PerfRunnerTask(Bitmap bitmap, int blurKernelSize) {
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mPixels = new int[mWidth*mHeight];
        bitmap.getPixels(mPixels, 0, mWidth, 0, 0, mWidth, mHeight);
        mBlurKernelSize = blurKernelSize;
    }

    private static void runGrayscale(int[] pixels, String tag) {
        final String TAG = BASE_TAG + ": " + tag;

        long[] cppTimes = new long[NUM_RUNS];
        long[] javaTimes = new long[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            // C++
            Log.v(TAG, "Running iteration " + i + " for C++...");
            long t0 = System.nanoTime();
            CppTransformations.grey(pixels);
            long t1 = System.nanoTime();
            cppTimes[i] = timeInMS(t0,t1);

            // Java
            Log.v(TAG, "Running iteration " + i + " for Java...");
            t0 = System.nanoTime();
            JavaTransformations.grey(pixels);
            t1 = System.nanoTime();
            javaTimes[i] = timeInMS(t0,t1);
        }

        Log.v(TAG, "All C++ times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + cppTimes[i]);
        }
        Log.v(TAG, "Average C++ time: " + average(cppTimes));

        Log.v(TAG, "All Java times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + javaTimes[i]);
        }
        Log.v(TAG, "Average Java time: " + average(javaTimes));
    }

    private static void runColorFilter(int[] pixels, int red, int green, int blue, String tag) {
        final String TAG = BASE_TAG + ": " + tag;

        long[] cppTimes = new long[NUM_RUNS];
        long[] javaTimes = new long[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            // C++
            Log.v(TAG, "Running iteration " + i + " for C++...");
            long t0 = System.nanoTime();
            CppTransformations.colorFilter(pixels, red, green, blue);
            long t1 = System.nanoTime();
            cppTimes[i] = timeInMS(t0,t1);

            // Java
            Log.v(TAG, "Running iteration " + i + " for Java...");
            t0 = System.nanoTime();
            JavaTransformations.colorFilter(pixels, red, green, blue);
            t1 = System.nanoTime();
            javaTimes[i] = timeInMS(t0,t1);
        }

        Log.v(TAG, "All C++ times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + cppTimes[i]);
        }
        Log.v(TAG, "Average C++ time: " + average(cppTimes));

        Log.v(TAG, "All Java times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + javaTimes[i]);
        }
        Log.v(TAG, "Average Java time: " + average(javaTimes));
    }

    private static void runContrast(int[] pixels, int contrast, String tag) {
        final String TAG = BASE_TAG + ": " + tag;

        long[] cppTimes = new long[NUM_RUNS];
        long[] javaTimes = new long[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            // C++
            Log.v(TAG, "Running iteration " + i + " for C++...");
            long t0 = System.nanoTime();
            CppTransformations.contrast(pixels, contrast);
            long t1 = System.nanoTime();
            cppTimes[i] = timeInMS(t0,t1);

            // Java
            Log.v(TAG, "Running iteration " + i + " for Java...");
            t0 = System.nanoTime();
            JavaTransformations.contrast(pixels, contrast);
            t1 = System.nanoTime();
            javaTimes[i] = timeInMS(t0,t1);
        }

        Log.v(TAG, "All C++ times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + cppTimes[i]);
        }
        Log.v(TAG, "Average C++ time: " + average(cppTimes));

        Log.v(TAG, "All Java times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + javaTimes[i]);
        }
        Log.v(TAG, "Average Java time: " + average(javaTimes));
    }

    private static void runBrightness(int[] pixels, int brightness, String tag) {
        final String TAG = BASE_TAG + ": " + tag;

        long[] cppTimes = new long[NUM_RUNS];
        long[] javaTimes = new long[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            // C++
            Log.v(TAG, "Running iteration " + i + " for C++...");
            long t0 = System.nanoTime();
            CppTransformations.brightness(pixels, brightness);
            long t1 = System.nanoTime();
            cppTimes[i] = timeInMS(t0,t1);

            // Java
            Log.v(TAG, "Running iteration " + i + " for Java...");
            t0 = System.nanoTime();
            JavaTransformations.brightness(pixels, brightness);
            t1 = System.nanoTime();
            javaTimes[i] = timeInMS(t0,t1);
        }

        Log.v(TAG, "All C++ times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + cppTimes[i]);
        }
        Log.v(TAG, "Average C++ time: " + average(cppTimes));

        Log.v(TAG, "All Java times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + javaTimes[i]);
        }
        Log.v(TAG, "Average Java time: " + average(javaTimes));
    }

    private static void runBlur(int[] pixels, int w, int h, int blurKernelSize, String tag) {
        final String TAG = BASE_TAG + ": " + tag;

        long[] cppTimes = new long[NUM_RUNS];
        long[] javaTimes = new long[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            // C++
            Log.v(TAG, "Running iteration " + i + " for C++...");
            long t0 = System.nanoTime();
            CppTransformations.blur(pixels, w, h, blurKernelSize);
            long t1 = System.nanoTime();
            cppTimes[i] = timeInMS(t0,t1);

            // Java
            Log.v(TAG, "Running iteration " + i + " for Java...");
            t0 = System.nanoTime();
            JavaTransformations.blur(pixels, w, h, blurKernelSize);
            t1 = System.nanoTime();
            javaTimes[i] = timeInMS(t0,t1);
        }

        Log.v(TAG, "All C++ times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + cppTimes[i]);
        }
        Log.v(TAG, "Average C++ time: " + average(cppTimes));

        Log.v(TAG, "All Java times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + javaTimes[i]);
        }
        Log.v(TAG, "Average Java time: " + average(javaTimes));
    }

    private static long timeInMS(long t0, long t1) {
        return (t1-t0)/1000000;
    }

    private static double average(long[] arr) {
        double sum = 0.0;
        for (long l : arr) {
            sum += l;
        }
        return sum / arr.length;
    }

    @Override
    protected String doInBackground(Void... tags) {
        if (mPixels == null) {
            Log.v(BASE_TAG, "Error in doInBackground(): Null pixel array");
            return null;
        }

        // Before beginning perf assessment, run some C++ code in order
        // to get OpenMP's threadpool started up. That way, the grayscale
        // benchmark's average isn't skewed by a higher initial time.
        CppTransformations.grey(mPixels);

        runGrayscale(mPixels, "Grayscale");
        runColorFilter(mPixels, 200, 100, 150, "Color Filter");
        runContrast(mPixels, 200, "Contrast");
        runBrightness(mPixels, 200, "Brightness");
        runBlur(mPixels, mWidth, mHeight, mBlurKernelSize, "Blur");

        return "Ok";
    }
}
