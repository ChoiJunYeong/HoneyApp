package com.example.junyeong.rocketcopy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DestSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_setting);


        Intent oldIntent = getIntent();


        oldIntent.getStringExtra("Activitiy_name");
    }
    public void change_dest(View view){
        Intent intent = new Intent(getApplicationContext(),DestChooseActivity.class);
        startActivity(intent);
    }
}
