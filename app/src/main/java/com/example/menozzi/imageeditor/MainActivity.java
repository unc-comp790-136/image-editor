// TODO
// Color picker credit: https://github.com/jbruchanov/AndroidColorPicker.git


package com.example.menozzi.imageeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    static {
        System.loadLibrary("native-lib");
    }
    public native void grey(int[] pixels);
    public native void colorFilter(int[] pixels, int red, int green, int blue);
    public native void blur(int[] pixels, int w, int h, int blur);
    public native void brightness(int[] pixels, int brightness);
    public native void contrast(int[] pixels, int contrast);

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String IMAGE_NAME = "IMAGE_EDITOR_IMAGE";

    private String mImagePath;

    private ImageView mImageView;

    private int[] orig_pixels;

    private int mColorValue;

    private Bitmap mOrigBitmap;
    private Bitmap mCurrBitmap;

    private SeekBar mBlurBar;
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

        mContrastBar = (SeekBar) findViewById(R.id.contrast_bar);
        mBlurBar = (SeekBar) findViewById(R.id.blur_bar);
        mBrightnessBar = (SeekBar) findViewById(R.id.brightness_bar);

        GradientView g = (GradientView) findViewById(R.id.colorPicker);
        GradientView bottom = (GradientView) findViewById(R.id.bottom);
        g.setBrightnessGradientView(bottom);
        //TextView tv = (TextView) findViewById(R.id.color_value);

        // Implement anonymous function
        bottom.setOnColorChangedListener( new GradientView.OnColorChangedListener() {
            //TextView tv = (TextView) findViewById(R.id.color_value);

            @Override
            public void onColorChanged(GradientView view, int color) {
                //   mTextView.setTextColor(color);

                mColorValue = color;

                //tv.setText("#" + Integer.toHexString(color));
            }
        });

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

    public void chooseColor(View view){
        //ImageView pic = (ImageView) findViewById(R.id.image);
        //pic.setVisibility(View.GONE);
        //view.setVisibility(View.GONE);

        //Button b = (Button) findViewById(R.id.choose_color);
        LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
        //GradientView bottom = (GradientView) findViewById(R.id.bottom);
        g.setVisibility(View.VISIBLE);
        //b.setVisibility(View.VISIBLE);
        //bottom.setVisibility(View.VISIBLE);

    }

    public void onColorChosen(View view){

        int w = 0;
        int h = 0;

        try{

            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();

        }catch(NullPointerException e){
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
            //GradientView bottom = (GradientView) findViewById(R.id.bottom);

            //bottom.setVisibility(View.GONE);
            g.setVisibility(View.GONE);
            //view.setVisibility(View.GONE);

           /* ImageView pic = (ImageView) findViewById(R.id.image);
            Button b = (Button) findViewById(R.id.color_pick_start);
            pic.setVisibility(View.VISIBLE);
            b.setVisibility(View.VISIBLE);*/
            return;

        }

        String hexString = Integer.toHexString(mColorValue);

        int red = Integer.valueOf(hexString.substring(2,4), 16);
        int green = Integer.valueOf(hexString.substring(4,6), 16);
        int blue = Integer.valueOf(hexString.substring(6,8), 16);

        int[] pixels = new int[w*h];
        mOrigBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        colorFilter(pixels, red, green, blue);

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);
        LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
        //GradientView bottom = (GradientView) findViewById(R.id.bottom);

        //bottom.setVisibility(View.GONE);
        g.setVisibility(View.GONE);
        /*view.setVisibility(View.GONE);

        ImageView pic = (ImageView) findViewById(R.id.image);
        Button b = (Button) findViewById(R.id.color_pick_start);
        pic.setVisibility(View.VISIBLE);
        b.setVisibility(View.VISIBLE);*/

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.v("NAVIGATION ITEM", ""+item.getItemId());

        TextView intro = (TextView) findViewById(R.id.intro);
        LinearLayout grey = (LinearLayout) findViewById(R.id.greyscale_buttons);
        LinearLayout g = (LinearLayout) findViewById(R.id.color_picker_view);
        Button cs = (Button) findViewById(R.id.color_pick_start);

        DrawerLayout drawer;

        switch (item.getItemId()) {
            case R.id.filter:

                grey.setVisibility(View.GONE);
                intro.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurBar.setVisibility(View.GONE);
                cs.setVisibility(View.VISIBLE);
                break;
            case R.id.grayscale:
                grey.setVisibility(View.VISIBLE);
                intro.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurBar.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.blur:
                grey.setVisibility(View.GONE);
                intro.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurBar.setVisibility(View.VISIBLE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.home:
                grey.setVisibility(View.GONE);
                intro.setVisibility(View.VISIBLE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurBar.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.contrast:
                grey.setVisibility(View.GONE);
                intro.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.VISIBLE);
                mBrightnessBar.setVisibility(View.GONE);
                mBlurBar.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            case R.id.brightness:
                grey.setVisibility(View.GONE);
                intro.setVisibility(View.GONE);
                mContrastBar.setVisibility(View.GONE);
                mBrightnessBar.setVisibility(View.VISIBLE);
                mBlurBar.setVisibility(View.GONE);
                g.setVisibility(View.GONE);
                cs.setVisibility(View.GONE);
                break;
            default:
                Toast.makeText(this, "How did we even get here?", Toast.LENGTH_SHORT).show();
        }


        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent res){
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
            mOrigBitmap = BitmapFactory.decodeFile(mImagePath, opts);
            mCurrBitmap = Bitmap.createBitmap(mOrigBitmap);

            mImageView.setImageBitmap(mOrigBitmap);
        }
    }

    public void changeToGray(View v) {

        int w = 0;
        int h = 0;

        if(orig_pixels != null){
            return;
        }

        try{

            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();

        }catch(NullPointerException e){
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }


        int[] pixels = new int[w*h];


        mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        orig_pixels = new int[w*h];
        orig_pixels = Arrays.copyOf(pixels, pixels.length);

        // Convert to greyscale
        grey(pixels);

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);

    }

    public void changeBackFromGray(View v){

        if(orig_pixels == null){
            return;
        }

        int w = 0;
        int h = 0;

        try{

            w = mCurrBitmap.getWidth();
            h = mCurrBitmap.getHeight();

        }catch(NullPointerException e){
            Toast.makeText(this, "Take a picture first!", Toast.LENGTH_SHORT).show();
            return;
        }

        mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCurrBitmap.setPixels(orig_pixels, 0, w, 0, 0, w, h);

        mImageView.setImageBitmap(mCurrBitmap);

        orig_pixels = null;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      int blur = mBlurBar.getProgress();

       // int w = mCurrBitmap.getWidth();
       // int h = mCurrBitmap.getHeight();

       // int[] pixels = new int[w*h];
       // mOrigBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

       // colorFilter(pixels, r, g, b);

       // mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        // mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

       //mImageView.setImageBitmap(mCurrBitmap);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
