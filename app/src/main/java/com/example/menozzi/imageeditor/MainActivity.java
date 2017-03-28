package com.example.menozzi.imageeditor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        /*

        Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cam, 1);

         */
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    /*

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent res){

            if(requestCode == 1 && resultCode == RESULT_OK){
                Bundle extras = x.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                img = (ImageView) findViewById(R.id.edit_image);
                img.setImageBitmap(imageBitmap);
            }

        }

        public void newPicture(View v){
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cam, 1);

        }

        TODO
            - Touch Input (if we want sliders)
            - Setting Menu (new activity)
            - Save picture when going to settings menu

     */
}
