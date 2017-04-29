package com.example.menozzi.imageeditor;

public class JavaTransformations {
    // Shift amounts for pixel channel packing
    private static int SHIFT_A = 24;
    private static int SHIFT_R = 16;
    private static int SHIFT_G =  8;
    private static int SHIFT_B =  0;

    // Extract individual channels from packed ARGB pixel
    private static int a(int argb) { return (argb >> SHIFT_A) & 0xFF; }
    private static int r(int argb) { return (argb >> SHIFT_R) & 0xFF; }
    private static int g(int argb) { return (argb >> SHIFT_G) & 0xFF; }
    private static int b(int argb) { return (argb >> SHIFT_B) & 0xFF; }

    // Pack channel values into a single ARGB pixel
    private static int pack(int a, int r, int g, int b) {
        return (a << SHIFT_A) | (r << SHIFT_R) | (g << SHIFT_G) | (b << SHIFT_B);
    }

    // Convert image to greyscale
    public static void grey(int[] pixels) {
        int size = pixels.length;
        for (int i = 0; i < size; i++) {
            int p = pixels[i];
            int grey = (int)(0.2126f*r(p) + 0.7152f*g(p) + 0.0722f*b(p));
            pixels[i] = pack(a(p), grey, grey, grey);
        }
    }

    // Apply a color filter to image
    public static void colorFilter(int[] pixels, int red, int green, int blue) {
        int size = pixels.length;
        float rscale = red/255.f;
        float gscale = green/255.f;
        float bscale = blue/255.f;
        for (int i = 0; i < size; i++) {
            int newr = (int)(r(pixels[i]) * rscale);
            int newg = (int)(g(pixels[i]) * gscale);
            int newb = (int)(b(pixels[i]) * bscale);
            pixels[i] = pack(a(pixels[i]), newr, newg, newb);
        }
    }

    private static int idx(int x, int y, int w) {
        return x + w*y;
    }

    // Apply a box filter blur to image
    public static void blur(int[] pixels, int w, int h, int blur) {
        // Ensure kernel size is odd
        if (blur % 2 == 0) {
            blur--;
            if (blur < 3) {
                blur = 3;
            }
        }

        // Copy original pixels
        int[] orig_pixels = pixels.clone();

        // Horizontal filter
        for (int y = blur/2; y < h-(blur/2); y++) {
            for (int x = blur/2; x < w-(blur/2); x++) {
                int i = idx(x,y,w);

                int rsum = 0;
                int gsum = 0;
                int bsum = 0;

                int kstart = x - blur/2;
                int kend   = x + blur/2;

                for (int k = kstart; k <= kend; k++) {
                    rsum += r(orig_pixels[idx(k,y,w)]);
                    gsum += g(orig_pixels[idx(k,y,w)]);
                    bsum += b(orig_pixels[idx(k,y,w)]);
                }

                rsum /= blur;
                gsum /= blur;
                bsum /= blur;

                pixels[i] = pack(a(pixels[i]), rsum, gsum, bsum);
            }
        }

        // Vertical filter
        for (int y = blur/2; y < h-(blur/2); y++) {
            for (int x = blur/2; x < w-(blur/2); x++) {
                int i = idx(x,y,w);

                int rsum = 0;
                int gsum = 0;
                int bsum = 0;

                int kstart = y - blur/2;
                int kend   = y + blur/2;

                for (int k = kstart; k <= kend; k++) {
                    rsum += r(pixels[idx(x,k,w)]);
                    gsum += g(pixels[idx(x,k,w)]);
                    bsum += b(pixels[idx(x,k,w)]);
                }

                rsum /= blur;
                gsum /= blur;
                bsum /= blur;

                pixels[i] = pack(a(pixels[i]), rsum, gsum, bsum);
            }
        }
    }

    // Scale image brightness
    public static void brightness(int[] pixels, int brightness) {
        int size = pixels.length;
        if (brightness > 0) {
            for (int i = 0; i < size; i++) {
                int newr = Math.min(r(pixels[i]) + brightness, 255);
                int newg = Math.min(g(pixels[i]) + brightness, 255);
                int newb = Math.min(b(pixels[i]) + brightness, 255);
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        } else if (brightness < 0) {
            for (int i = 0; i < size; i++) {
                int newr = Math.max(r(pixels[i]) + brightness, 0);
                int newg = Math.max(g(pixels[i]) + brightness, 0);
                int newb = Math.max(b(pixels[i]) + brightness, 0);
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        }
    }

    // Set image contrast
    public static void contrast(int[] pixels, int contrast) {
        int size = pixels.length;
        float f = (float)(259*(contrast + 255)) / (255*(259 - contrast));
        if (contrast > 0) {
            for (int i = 0; i < size; i++) {
                int newr = Math.max(0, Math.min((int)(f*(r(pixels[i]) - 128) + 128), 255));
                int newg = Math.max(0, Math.min((int)(f*(g(pixels[i]) - 128) + 128), 255));
                int newb = Math.max(0, Math.min((int)(f*(b(pixels[i]) - 128) + 128), 255));
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        } else if (contrast < 0) {
            for (int i = 0; i < size; i++) {
                int newr = Math.max((int)(f*(r(pixels[i]) - 128) + 128), 0);
                int newg = Math.max((int)(f*(g(pixels[i]) - 128) + 128), 0);
                int newb = Math.max((int)(f*(b(pixels[i]) - 128) + 128), 0);
                pixels[i] = pack(a(pixels[i]), newr, newg, newb);
            }
        }
    }
}
