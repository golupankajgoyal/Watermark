package com.example.watermark;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST =1 ;
    private static final int MY_STORAGE_PERMISSION_CODE =2 ;
    private static ImageView imageView;
    Button button;
    Button selectButton;
    Button downloadButton;
    private static Context context;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.image_view);
        button=findViewById(R.id.button);
        context=this;
        selectButton=findViewById(R.id.select_image);
        downloadButton=findViewById(R.id.button_download);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
                    }else{
                    downloadFile();
                    }
                }
            }
        });
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(imageView);

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWatermark(0.20f);
            }
        });

    }

    /**
     * Embeds an image watermark over a source image to produce
     * a watermarked one.
     * @param ratio A float value < 1 to give the ratio of watermark's height to image's height,
     *             try changing this from 0.20 to 0.60 to obtain right results
     */
    public static void addWatermark( float ratio) {
        Canvas canvas;
        Paint paint;
        Bitmap bmp;
        Matrix matrix;
        RectF r;
        float valueInPixels= (float)dpToPx(16f,context);
        int width, height;
        float scale;
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap source = draw.getBitmap();
        Matrix imagematrix = new Matrix();
        RectF imageRec;
        float imagescale = 1.0f;

        imagematrix.postScale(imagescale,imagescale);
        // Determine the post-scaled size of the watermark
        imageRec = new RectF(0, 0, source.getWidth(), source.getHeight());
        imagematrix.mapRect(imageRec);
        imagematrix.postTranslate(0, 0);

        Bitmap watermark = BitmapFactory.decodeResource(imageView.getContext().getResources(),
                R.drawable.healthy_eating);

        width = source.getWidth();
        height = source.getHeight();

        // Create the new bitmap
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setTextSize(valueInPixels);

        // Copy the original bitmap into the new one
        canvas = new Canvas(bmp);
        canvas.drawBitmap(source,imagematrix, paint);

        // Scale the watermark to be approximately to the ratio given of the source image height
        scale = (float) (((float) height * ratio) / (float) watermark.getHeight());

        // Create the matrix
        matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Determine the post-scaled size of the watermark
        r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
        matrix.mapRect(r);

        // Move the watermark to the bottom right corner
//        matrix.postTranslate(width - r.width(), height - r.height());
        matrix.postTranslate(0,0);
        // Draw the watermark
        canvas.drawBitmap(watermark, matrix, paint);

        String watmarkText="Image by google";
        Rect textBoundsRect=new Rect();
        paint.getTextBounds(watmarkText,0,watmarkText.length(),textBoundsRect);
        float drawStartX = width - textBoundsRect.right - (float)dpToPx(8f, context);
        float drawStartY = height - dpToPx(4f, context);
        canvas.drawText(watmarkText, drawStartX, drawStartY, paint);

        imageView.setImageBitmap(bmp);

    }


    private void openFileChooser(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Toast.makeText(MainActivity.this, "Photo Selected", Toast.LENGTH_SHORT).show();
            mImageUri = data.getData();

            Glide.with(this)
                    .load(mImageUri)
                    .into(imageView);

                Bitmap bitmap=null;

                // Do something with the bitmap
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_STORAGE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission was granted. Now you can call your method to open camera, fetch contact or whatever
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        downloadFile();
                    }
                } else {
                    // Permission was denied.......
                    // You can again ask for permission from here
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void downloadFile(){

            //to get the image from the ImageView
            BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = draw.getBitmap();

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/FolderName");
            dir.mkdirs();
            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);
            try {
                FileOutputStream outStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    private static int dpToPx(float dp, Context context) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, context.getResources().getDisplayMetrics());
    }


}