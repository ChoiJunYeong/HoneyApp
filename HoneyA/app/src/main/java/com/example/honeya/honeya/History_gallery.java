package com.example.honeya.honeya;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by junyeong on 17. 11. 8.
 */

public class History_gallery{
    String tag;
    Bitmap img;

    public History_gallery(){
        return;
    }
    public History_gallery(String tag){
        this.tag=tag;
    }
    public History_gallery(String tag,Bitmap img){
        this.tag = tag;
        this.img = img;
    }
    public Bitmap getImg() { return img; }
    public String getTag(){
        return tag;
    }
    public void setImg(Bitmap img){
        this.img=img;
    }
    public void setTag(String tag){
        this.tag=tag;
    }
}
