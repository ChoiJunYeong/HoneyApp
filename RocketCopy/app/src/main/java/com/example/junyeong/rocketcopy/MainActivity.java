package com.example.junyeong.rocketcopy;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import static com.example.junyeong.rocketcopy.SchedulerActivity.REQUEST_IMAGE_CAPTURE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onButtonClicked(View v){
        Intent intent = new Intent(getApplicationContext(),FileActivity.class);
        startActivity(intent);
    }
    public void onButtonClicked2(View v){
        dispatchTakePictureIntent();
    }
    public void onButtonClicked3(View v){
        Intent intent = new Intent(getApplicationContext(),SchedulerActivity.class);
        startActivity(intent);
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
