package com.example.tarun.retrofitdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tarun.retrofitdemo.Networking.ApiManager;
import com.example.tarun.retrofitdemo.util.RealPathUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private final int PICK_IMAGE = 1234, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1235;
    private ImageView profilePic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assert (findViewById(R.id.picUpload)) != null;
        (findViewById(R.id.picUpload)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        profilePic = (ImageView) findViewById(R.id.profilePic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PICK_IMAGE) {
            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11) {
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
                setImage(realPath);
            }
                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19) {
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());
                setImage(realPath);
            }
                // SDK > 19 (Android 4.4)
            else {

                // Here, thisActivity is the current activity
               if (checkReadStoragePermission()) {

                   realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
                   setImage(realPath);

               } else {
                   intent = data;
                   ActivityCompat.requestPermissions(MainActivity.this,
                           new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                           MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
               }
            }


        }
    }
    private Intent intent;

    private boolean checkReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                return false;
        }
        return true;
    }


    public String getPath(Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void setImage(String imagePath) {
       if (imagePath == null) {
           Log.i("ImagePath", "Image path is null");
       }
        File file = new File(imagePath);
        Uri uriFromPath = Uri.fromFile(file);
        profilePic.setImageURI(uriFromPath);

        ApiManager.getInstance().uploadFile(file);
        // you have two ways to display selected image

        // ( 1 ) imageView.setImageURI(uriFromPath);

        // ( 2 ) imageView.setImageBitmap(bitmap);
      /*  Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    String realPath = RealPathUtil.getRealPathFromURI_API19(this, intent.getData());
                    setImage(realPath);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission rejected by the user", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
