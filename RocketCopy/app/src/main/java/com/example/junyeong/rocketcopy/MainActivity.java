package com.example.junyeong.rocketcopy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    /*public void onButtonClicked(View v){
        Intent intent = new Intent(getApplicationContext(),.class);
        startActivity(intent);
    }*/
    public void onButtonClicked2(View v){
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        startActivity(intent);
    }
    public void onButtonClicked3(View v){
        Intent intent = new Intent(getApplicationContext(),SchedulerActivity.class);
        startActivity(intent);
    }
}
