package com.example.junyeong.rocketcopy;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;


import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //View create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Camera setting
        final CameraSurfaceView cameraView = new CameraSurfaceView(getApplicationContext());
        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.previewFrame);
        previewFrame.addView(cameraView);

        //Camera Button setting
        ImageButton saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                cameraView.capture(new Camera.PictureCallback(){
                    public void onPictureTaken(byte[] data,Camera camera){
                        try{

                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                            /// / String outUriStr = MediaStore.Images.Media.insertImage(getContentResolver(),Environment.getExternalStorageDirectory().toString() + "/your/folder",IMAGE_FILE,"Captured Image using Camera.");
                          //  if(outUriStr==null){
                            //    Log.d("SampleCapture","Image insert failed.");
                              //  return;
                            //}
                                SaveImage(bitmap);
                            camera.startPreview();
                        }
                        catch (Exception e){
                            Log.e("SampleCapture","Failed to insert image",e);
                        }
                    }
                });
            }
        });


    }

    //save captured image where I want
    public void SaveImage(Bitmap bitmap){
        String root = Environment.getExternalStorageDirectory().toString();
        String filepath = root + "/app/rocket/images";
        File myDir = new File(filepath);
        myDir.mkdirs();
        Date date = new Date();
        String fname = date.getTime() +".jpg";
        File file = new File (myDir, fname);

        if (file.exists ()) file.delete ();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        //how to broadcast? it doesn't do anything
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        Toast.makeText(getApplicationContext(), filepath+"/"+fname, Toast.LENGTH_LONG).show();
    }


    private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
        private SurfaceHolder mHolder;
        private Camera camera = null;
        public CameraSurfaceView(Context context){
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceCreated(SurfaceHolder holder){
            camera = Camera.open();
            try{
                camera.setPreviewDisplay(mHolder);
            }
            catch(Exception e){
                Log.e("CameraSurfaceView","Failed to set camera preview",e);
            }
        }
        public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
            camera.startPreview();
        }
        public void surfaceDestroyed(SurfaceHolder holder){
            camera.stopPreview();
            camera.release();
            camera=null;
        }
        public boolean capture(Camera.PictureCallback handler){
            if(camera !=null){
                camera.takePicture(null,null,handler);
                return true;
            }
            else {
                return false;
            }
        }
    }
}
