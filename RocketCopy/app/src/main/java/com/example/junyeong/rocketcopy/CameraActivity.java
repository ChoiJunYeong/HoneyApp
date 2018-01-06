package com.example.junyeong.rocketcopy;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;



public class CameraActivity extends AppCompatActivity {
    String filepath;
    Bitmap currentImage;
    GoogleSignInClient googleSignInClient;
    DriveResourceClient driveResourceClient;
    DriveClient driveClient;
    Boolean[] SendList = new Boolean[5];
    final static int REQUEST_CODE_SIGN_IN=1;
    final static int GOOGLE_DRIVE_SEND=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //View create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
        Intent intent = getIntent();
        filepath = intent.getStringExtra("Directory");
        if(filepath==null)
            filepath = Environment.getExternalStorageDirectory().toString() + "/honeyA";
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
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //setting for exit
        Intent intent = new Intent();
        setResult(RESULT_CANCELED,intent);

    }
    //save captured image where I want
    public void SaveImage(Bitmap bitmap){
        File myDir = new File(filepath);
        if(!myDir.mkdirs())
            if(!myDir.getParentFile().exists())
                Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
        Date date = new Date();
        String fname = date.getTime() +".jpeg";
        File file = new File (myDir, fname);

        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //how to broadcast? it doesn't do anything
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));

        SendList[GOOGLE_DRIVE_SEND]=true;
        if(SendList[GOOGLE_DRIVE_SEND]) {
            signIn();
            currentImage = bitmap;
            saveFileToDrive();
        }
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


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    //save login state id_token
                    if (result.isSuccess()) {
                        driveResourceClient = Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                        driveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    }

                }
                break;

        }
    }
    //sign in googledrive
    private void signIn() {
        googleSignInClient = buildGoogleSignInClient();
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }
    // Build a Google SignIn client.
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        driveResourceClient
                .createContents()
                .continueWithTask(
                        new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                return createFileIntentSender(task.getResult(), currentImage);
                            }
                        });
    }
    private Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {
        // Get an output stream for the contents.
        OutputStream outputStream = driveContents.getOutputStream();
        // Write the bitmap data from it.
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"upload fail",Toast.LENGTH_LONG).show();
        }
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg")
                        .setTitle("Android_Photo.jpeg")
                        .build();
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();
        return driveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        new Continuation<IntentSender, Void>() {
                            @Override
                            public Void then(@NonNull Task<IntentSender> task) throws Exception {
                                startIntentSender(task.getResult(), null, 0, 0, 0);
                                return null;
                            }
                        });
    }

}
