package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by junyeong on 18. 1. 9.
 */

public class FolderIconItemView extends LinearLayout {
    TextView textView;
    ImageView imageView;
    public FolderIconItemView(Context context){
        super(context);
        init(context);
    }
    public FolderIconItemView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }
    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.folder_icon_item,this,true);
        textView = (TextView) findViewById(R.id.folder_name);
        imageView = (ImageView) findViewById(R.id.folder_icon);
    }
    public void setTag(String text){
        textView.setText(text);
    }
    public void setImageView(){
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_folder_24dp));
    }
    public void setImageView(Drawable drawableimage){
        imageView.setImageDrawable(drawableimage);
    }
}
