package com.example.honeya.honeya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by junyeong on 17. 11. 8.
 */

public class History_gallery_View extends LinearLayout {
    TextView textView;
    ImageView imageView;

    public History_gallery_View(Context context){
        super(context);
        init(context);
    }
    public History_gallery_View(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }
    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.history_item,this,true);
        textView = (TextView) findViewById(R.id.text);
        imageView = (ImageView) findViewById(R.id.img);
    }
    public void setTag(String tag){
        textView.setText(tag);
    }
    public void setImage(Bitmap img){
        imageView.setImageBitmap(img);
    }
}
