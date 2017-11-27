package com.example.junyeong.rocketcopy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class FocusImage extends AppCompatActivity {
    String imgPath;
    Bitmap img;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_image);
        imgPath = getIntent().getExtras().getString("file name");
        img = BitmapFactory.decodeFile(imgPath);
        img = Bitmap.createScaledBitmap(img, 1000, 1000, false);
        imageView = findViewById(R.id.focusImage);
        imageView.setImageBitmap(img);

    }
    public void checkDelete(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Scan");
        builder.setMessage("Are you sure you want to delete this scan?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.putExtra("filepath",imgPath);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void share(View view){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
// Set default text message
// 카톡, 이메일, MMS 다 이걸로 설정 가능
//String subject = "문자의 제목";
        String text = "다른 앱에 공유하기";
//intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
// Title of intent
        Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
        startActivity(chooser);
    }
}
