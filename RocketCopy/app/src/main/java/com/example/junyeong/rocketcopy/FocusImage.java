package com.example.junyeong.rocketcopy;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class FocusImage extends AppCompatActivity {
    String imgPath;
    Bitmap img;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_image);
        //load image
        imgPath = getIntent().getExtras().getString("file name");
        img = BitmapFactory.decodeFile(imgPath);
        //set imageview
        imageView = findViewById(R.id.focusImage);
        imageView.setImageBitmap(img);

        //set onclick lister for full image view
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams) view.getLayoutParams();
                if(view.getLayoutParams().height != RelativeLayout.LayoutParams.MATCH_PARENT){
                    params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    view.setLayoutParams(params);
                }

                else{
                    params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    view.setLayoutParams(params);
                }

            }
        });
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
        intent.setType("image/jpeg");
        String text = "다른 앱에 공유하기";
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgPath));
        intent.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(intent, "이미지 공유하기");
        startActivity(chooser);
    }
}
