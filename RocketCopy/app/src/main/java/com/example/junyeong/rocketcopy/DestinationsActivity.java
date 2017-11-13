package com.example.junyeong.rocketcopy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class DestinationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);
        //make buttons
        RelativeLayout relay1 = (RelativeLayout) findViewById(R.id.relay1);
        relay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),DestSettingActivity.class);
                intent.putExtra("Activity_name","relay1");
                startActivity(intent);
            }
        });

        RelativeLayout relay2 = (RelativeLayout) findViewById(R.id.relay2);
        relay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),DestSettingActivity.class);
                intent.putExtra("Activity_name","relay2");
                startActivity(intent);
            }
        });

        RelativeLayout relay3 = (RelativeLayout) findViewById(R.id.relay3);
        relay3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),DestSettingActivity.class);
                intent.putExtra("Activity_name","relay3");
                startActivity(intent);
            }
        });
    }


}
