// TODO
// Color picker credit: https://github.com/jbruchanov/AndroidColorPicker.git

package com.example.menozzi.imageeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    private static final int BLUR_KERNEL_SIZE = 21;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String IMAGE_NAME = "IMAGE_EDITOR_IMAGE";

    private String mImagePath;

    private ImageView mImageView;

    private int[] orig_pixels;

    private int mColorValue;

    private Bitmap mOrigBitmap;
    private Bitmap mCurrBitmap;
    private Bitmap mBaseImageBitmap;

    private Button mBlurButton;
    private SeekBar mBrightnessBar;
    private SeekBar mContrastBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);

        mImageView.setImageResource(R.mipmap.ic_launcher);

        orig_pixels = null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Create image file
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File image = File.createTempFile(IMAGE_NAME, ".jpg", storageDir);
                    mImagePath = image.getAbsolutePath();

                    // Take picture
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri imageUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.example.menozzi.imageeditor", image);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Failed to take image", Toast.LENGTH_SHORT).show();
                }
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCurrBitmap = null;
        mBaseImageBitmap = null;

        mContrastBar = (SeekBar) findViewById(R.id.contrast_bar);
        mBrightnessBar = (SeekBar) findViewById(R.id.brightness_bar);
        
        mContrastBar.setProgress(255);
        mBrightnessBar.setProgress(255);

        mContrastBar.setOnSeekBarChangeListener(this);
        mBrightnessBar.setOnSeekBarChangeListener(this);

        GradientView bottom = (GradientView) findViewById(R.id.bottom);
        bottom.setOnColorChangedListener(new GradientView.OnColorChangedListener() {
            @Override
            public void onColorChanged(GradientView view, int color) {
                mColorValue = color;
            }
        });

        GradientView g = (GradientView) findViewById(R.id.colorPicker);
        g.setBrightnessGradientView(bottom);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_reset:
                mImageView.setImageBitmap(mOrigBitmap);
                mCurrBitmap = mOrigBitmap;
                break;
            default:
                Toast.makeText(this, "How did we even get here?", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void chooseColor(View view) {
        FrameLayout main = (FrameLayout) findViewById(R.id.content_main);
        main.setBackgroundColor(0x80000000);

        Button b = (Button) findViewById(R.id.color_pick_start);
        b.setVisibility(View.GONE);

        LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
        g.setVisibility(View.VISIBLE);
    }

    public void onColorChosen(View view) {
        int w = 0;
        int h = 0;

        FrameLayout main = (FrameLayout) findViewById(R.id.content_main);
        main.setBackgroundColor(0x00000000);

        try {
            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();

            LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
            g.setVisibility(View.GONE);

            Button b = (Button) findViewById(R.id.color_pick_start);
            b.setVisibility(View.VISIBLE);

            return;
        }

        String hexString = Integer.toHexString(mColorValue);

        int red = Integer.valueOf(hexString.substring(2,4), 16);
        int green = Integer.valueOf(hexString.substring(4,6), 16);
        int blue = Integer.valueOf(hexString.substring(6,8), 16);

        int[] pixels = new int[w*h];
        mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);
        mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        CppTransformations.colorFilter(pixels, red, green, blue);

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);
        LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);

        g.setVisibility(View.GONE);

        Button b = (Button) findViewById(R.id.color_pick_start);
        b.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.v("NAVIGATION ITEM", ""+item.getItemId());

        LinearLayout grey = (LinearLayout) findViewById(R.id.greyscale_buttons);
        LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
        Button cs = (Button) findViewById(R.id.color_pick_start);
        mBlurButton = (Button) findViewById(R.id.blur_bar);

        DrawerLayout drawer;

        if (mCurrBitmap != null) {
            mOrigBitmap = Bitmap.createBitmap(mCurrBitmap);
        }

        switch (item.getItemId()) {
            case R.id.filter:
                grey.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurButton.setVisibility(View.GONE);
                cs.setVisibility(View.VISIBLE);
                break;
            case R.id.grayscale:
                grey.setVisibility(View.VISIBLE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurButton.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.blur:
                grey.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurButton.setVisibility(View.VISIBLE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.contrast:
                mContrastBar.setProgress(255);
                grey.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.VISIBLE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurButton.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.brightness:
                mBrightnessBar.setProgress(255);
                grey.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.VISIBLE);
                mBlurButton.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            default:
                Toast.makeText(this, "How did we even get here?", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent res) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the dimensions of the image view
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mImagePath, opts);
            int photoW = opts.outWidth;
            int photoH = opts.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the image view
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scaleFactor;
            opts.inPurgeable = true;
            mBaseImageBitmap = BitmapFactory.decodeFile(mImagePath, opts);
            mCurrBitmap = Bitmap.createBitmap(mBaseImageBitmap);

            mImageView.setImageBitmap(mCurrBitmap);
        }
    }

    public void changeToGray(View v) {
        int w = 0;
        int h = 0;

        if (orig_pixels != null) {
            return;
        }

        try {
            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] pixels = new int[w*h];

        mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);
        mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        orig_pixels = new int[w*h];
        orig_pixels = Arrays.copyOf(pixels, pixels.length);

        // Convert to greyscale
        CppTransformations.grey(pixels);

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);
    }

    public void chooseBlur(View v) {
        int w = 0;
        int h = 0;

        try {
            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();

        } catch (NullPointerException e) {
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] pixels = new int[w*h];

        mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);

        mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        CppTransformations.blur(pixels, w, h, BLUR_KERNEL_SIZE);

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);
    }

    public void changeBackFromGray(View v) {
        if (orig_pixels == null) {
            return;
        }

        int w = 0;
        int h = 0;

        try {
            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }

        mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);
        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(orig_pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);

        orig_pixels = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int w = 0;
        int h = 0;

        try {
            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] pixels = new int[w*h];

        if (seekBar == mBrightnessBar) {
            int bright = mBrightnessBar.getProgress();

            Log.v("CHECK BRIGHTNESS", ""+bright);

            mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);

            mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

            bright -= 255;

            CppTransformations.brightness(pixels, bright);
        } else {
            int contrast = mContrastBar.getProgress();

            mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);
            mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

            contrast -= 255;

            CppTransformations.contrast(pixels, contrast);
        }

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
