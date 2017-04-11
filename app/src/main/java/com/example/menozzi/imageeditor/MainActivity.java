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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static {
        System.loadLibrary("native-lib");
    }
    public native void grey(int[] pixels);
    public native void colorFilter(int[] pixels, int red, int green, int blue);

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String IMAGE_NAME = "IMAGE_EDITOR_IMAGE";

    private String mImagePath;

    private ImageView mImageView;

    private Bitmap mOrigBitmap;
    private Bitmap mCurrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                break;
            case R.id.grayscale:
                int w = mCurrBitmap.getWidth();
                int h = mCurrBitmap.getHeight();

                int[] pixels = new int[w*h];
                mCurrBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

                // Convert to greyscale
                grey(pixels);

                mCurrBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                mCurrBitmap.setPixels(pixels, 0, w, 0, 0, w, h);

                mImageView.setImageBitmap(mCurrBitmap);

                break;
            case R.id.blur:
                break;
            default:
                Toast.makeText(this, "How did we even get here?", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
}
