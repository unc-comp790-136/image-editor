package com.example.menozzi.imageeditor;

import android.os.AsyncTask;
import android.util.Log;

public class PerfRunnerTask extends AsyncTask<Void, Void, String> {
    private static final String BASE_TAG = "PerfRunnerTask";
    private static final int NUM_RUNS = 5;

    private int[] mPixels;

    public PerfRunnerTask(int[] pixels) {
        mPixels = pixels.clone();
    }

    private static void runGrayscale(int[] pixels, String tag) {
        final String TAG = BASE_TAG + ": " + tag;

        long[] grayscaleTimesCpp = new long[NUM_RUNS];
        long[] grayscaleTimesJava = new long[NUM_RUNS];
        for (int i = 0; i < NUM_RUNS; i++) {
            // C++
            Log.v(TAG, "Running iteration " + i + " for C++...");
            long t0 = System.nanoTime();
            CppTransformations.grey(pixels);
            long t1 = System.nanoTime();
            grayscaleTimesCpp[i] = timeInMS(t0,t1);

            // Java
            Log.v(TAG, "Running iteration " + i + " for Java...");
            t0 = System.nanoTime();
            JavaTransformations.grey(pixels);
            t1 = System.nanoTime();
            grayscaleTimesJava[i] = timeInMS(t0,t1);
        }

        Log.v(TAG, "All C++ times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + grayscaleTimesCpp[i]);
        }
        Log.v(TAG, "Average C++ time: " + average(grayscaleTimesCpp));

        Log.v(TAG, "All Java times:");
        for (int i = 0; i < NUM_RUNS; i++) {
            Log.v(TAG, "\t" + grayscaleTimesJava[i]);
        }
        Log.v(TAG, "Average Java time: " + average(grayscaleTimesJava));
    }

    private static void runColorFilter(int[] pixels, int red, int green, int blue, String tag) {

    }

    private static void runContrast(int[] pixels, int contrast, String tag) {

    }

    private static void runBrightness(int[] pixels, int brightness, String tag) {

    }

    private static void runBlur(int[] pixels, String tag) {

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

        runGrayscale(mPixels, "Grayscale");
        runColorFilter(mPixels, 200, 100, 150, "Color Filter");
        runContrast(mPixels, 200, "Contrast");
        runBrightness(mPixels, 200, "Brightness");
        runBlur(mPixels, "Blur");

        return "Ok";
    }
}
