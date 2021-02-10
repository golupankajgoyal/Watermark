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
import android.graphics.drawable.Drawable;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST =1 ;
    private static final int MY_STORAGE_PERMISSION_CODE =2 ;
    private static ImageView imageView;
    private static ImageView test_imageview;
    Button button;
    Button selectButton;
    private static Context context;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.image_view);
        button=findViewById(R.id.button);
        test_imageview=findViewById(R.id.test_image_view);
        context=this;
        selectButton=findViewById(R.id.select_image);
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);


        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(imageView);

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
                Bitmap source = draw.getBitmap();
                String image_url="https://i.imgur.com/tGbaZCY.jpg";
                createBitmap("cMobile","@rohini",source,image_url);
            }
        });

    }

    public static void createBitmap(String postedon_name,String postedby_name,Bitmap source,String dpUrl){
        final Bitmap[] avatar_image = {BitmapFactory.decodeResource(imageView.getContext().getResources(),
                R.drawable.circular_avatar)};

        Picasso.get().load(dpUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Scale the avatar to be approximately to the ratio given of the source image height
                avatar_image[0] =bitmap;
                addWatermark(postedon_name,postedby_name,source,avatar_image[0]);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

    }

    /**
     * Embeds an image watermark over a source image to produce
     * a watermarked one.
     */
    public static void addWatermark(String postedon_name,String postedby_name,Bitmap source,Bitmap avatar_image) {
        float ratio=0.1f;
        Canvas canvas;
        Paint paint;
        Bitmap bmp;
        Matrix matrix;
        Matrix campus24_matrix;
        Matrix play_store_matrix;
        RectF play_store_rect;
        RectF r;
        RectF campus24;
        int width, height;
        float scale_x;
        float scale_y;
        float campus24_scale_x;
        float campus24_scale_y;
        float play_store_scale_x;
        float play_store_scale_y;
        Matrix imagematrix = new Matrix();
        RectF imageRec;
        float imagescale = 1.0f;



        //        For google play store image
        Bitmap play_store_image = BitmapFactory.decodeResource(imageView.getContext().getResources(),
                R.drawable.google_play_badge);
        // Scale the text_image to be approximately to the ratio given of the source image height
        play_store_scale_x = (float) (((float) source.getWidth() * 0.25f) / (float) play_store_image.getWidth());
        play_store_scale_y = (float) (((float) source.getHeight() * 0.25f) / (float) play_store_image.getHeight());
        float play_store_scale=Math.min(play_store_scale_x,play_store_scale_y);

        Bitmap watermark = BitmapFactory.decodeResource(imageView.getContext().getResources(),
                R.drawable.campus_logo);
        // Scale the watermark to be approximately to the ratio given of the source image height
        scale_y = (float) (((float) source.getHeight() * ratio) / (float) watermark.getHeight());
        scale_x = (float) (((float) source.getWidth() * ratio) / (float) watermark.getWidth());
        float scale= Math.min(scale_x,scale_y);

        float shift_factor=Math.max(play_store_image.getHeight()*play_store_scale,watermark.getHeight()*scale+20);

        width = source.getWidth();
        height = source.getHeight()+(int)(shift_factor*2);

        // Create the new bitmap
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setTextSize(shift_factor/4);
        paint.setFakeBoldText(true);

        // Copy the original bitmap into the new one
        canvas = new Canvas(bmp);
        canvas.drawColor(ContextCompat.getColor(context,R.color.white));
        imagematrix.postScale(imagescale,imagescale);
        // Determine the post-scaled size of the image
        imageRec = new RectF(0, 0, source.getWidth(), source.getHeight());
        imagematrix.mapRect(imageRec);
        imagematrix.postTranslate(0, shift_factor);
        canvas.drawBitmap(source,imagematrix, paint);



        // Create the matrix
        matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Determine the post-scaled size of the watermark
        r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
        matrix.mapRect(r);

        // Move the watermark to the bottom right corner

        matrix.postTranslate(16, source.getHeight()+shift_factor+(shift_factor-watermark.getHeight()*scale)/2);
        // Draw the watermark
        canvas.drawBitmap(watermark, matrix, paint);


//        For Campus24 text image
        Bitmap text_image = BitmapFactory.decodeResource(imageView.getContext().getResources(),
                R.drawable.black_logo);
        // Scale the text_image to be approximately to the ratio given of the source image height
        campus24_scale_x = (float) (((float) watermark.getWidth()*scale*2) / (float) text_image.getWidth());
        campus24_scale_y = (float) (((float) watermark.getHeight()*scale*2) / (float) text_image.getHeight());
        float campu24_scale=Math.min(campus24_scale_x,campus24_scale_y);
        campus24=new RectF(0, 0, text_image.getWidth(), text_image.getHeight());
        // Create the matrix
        campus24_matrix = new Matrix();
        campus24_matrix.postScale(campu24_scale,campu24_scale);
        campus24_matrix.mapRect(campus24);
        // Move the text_image to the bottom right corner
        campus24_matrix.postTranslate(28+watermark.getWidth()*scale,source.getHeight()+shift_factor+(shift_factor-text_image.getHeight()*campu24_scale)/2);
        canvas.drawBitmap(text_image, campus24_matrix, paint);


        play_store_rect=new RectF(0, 0, play_store_image.getWidth(), play_store_image.getHeight());
        // Create the matrix
        play_store_matrix = new Matrix();
        play_store_matrix.postScale(play_store_scale,play_store_scale);
        play_store_matrix.mapRect(play_store_rect);
        // Move the text_image to the bottom right corner
        play_store_matrix.postTranslate(source.getWidth()-play_store_image.getWidth()*play_store_scale, source.getHeight()+shift_factor+(shift_factor-play_store_image.getHeight()*play_store_scale)/2);
        canvas.drawBitmap(play_store_image, play_store_matrix, paint);

//        For circular avatar
        final float[] avatar_scale = {0.0f};


        float avatar_scale_x = (float) (((float)watermark.getWidth()*scale) / (float) avatar_image.getWidth());
        float avatar_scale_y = (float) (((float) watermark.getHeight()*scale) / (float) avatar_image.getHeight());

        avatar_scale[0] = Math.min(avatar_scale_x,avatar_scale_y);
        RectF avatar_rect=new RectF(0, 0, avatar_image.getWidth(), avatar_image.getHeight());
        // Create the matrix
        Matrix avatar_matrix = new Matrix();
        avatar_matrix.postScale(avatar_scale[0], avatar_scale[0]);
        avatar_matrix.mapRect(avatar_rect);
        // Move the avatar to the top left corner
        avatar_matrix.postTranslate(16,(shift_factor- avatar_image.getHeight()* avatar_scale[0])/2);
        canvas.drawBitmap(avatar_image, avatar_matrix, paint);


//        For circular heart
        Bitmap heart_image = BitmapFactory.decodeResource(imageView.getContext().getResources(),
                R.drawable.heart);
        // Scale the avatar to be approximately to the ratio given of the source image height
        float heart_scale_x = (float) (((float)watermark.getWidth()*scale) / (float) heart_image.getWidth());
        float heart_scale_y = (float) (((float) watermark.getHeight()*scale) / (float) heart_image.getHeight());
        float heart_scale=Math.min(heart_scale_x,heart_scale_y);
        RectF heart_rect=new RectF(0, 0, heart_image.getWidth(), heart_image.getHeight());
        // Create the matrix
        Matrix heart_matrix = new Matrix();
        heart_matrix.postScale(heart_scale,heart_scale);
        heart_matrix.mapRect(heart_rect);
        // Move the avatar to the top left corner
        heart_matrix.postTranslate(source.getWidth()-heart_image.getWidth()*heart_scale-16,(shift_factor-heart_image.getHeight()*heart_scale)/2);
        canvas.drawBitmap(heart_image, heart_matrix, paint);

        String postedby="Posted by:" ;
        String postedon="Posted on:";
        Rect textBoundsRect=new Rect();
        paint.getTextBounds(postedby,0,postedby.length(),textBoundsRect);
        float drawStartX = 32+ avatar_image.getWidth()* avatar_scale[0];
        float drawStartY = shift_factor/2.5f;
        canvas.drawText(postedby, drawStartX, drawStartY, paint);
        canvas.drawText(postedby_name, drawStartX, drawStartY+shift_factor/3, paint);
        paint.getTextBounds(postedon,0,postedon.length(),textBoundsRect);
        canvas.drawText(postedon, source.getWidth()-heart_image.getWidth()*heart_scale-textBoundsRect.width()-30, drawStartY, paint);
        paint.getTextBounds(postedon_name,0,postedon_name.length(),textBoundsRect);
        canvas.drawText(postedon_name, source.getWidth()-heart_image.getWidth()*heart_scale-textBoundsRect.width()-30, drawStartY+shift_factor/3, paint);

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