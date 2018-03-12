package com.example.honeya.honeya;

import android.graphics.drawable.Drawable;

/**
 * Created by junyeong on 18. 3. 9.
 */
public class FolderIconItem {
    String tag;
    Drawable drawableimage;
    public FolderIconItem(){
        return;
    }
    public String getTag(){
        return tag;
    }
    public Drawable getImage(){ return drawableimage;}
    public void setTag(String tag){
        this.tag=tag;
    }
    public void setImage(Drawable drawableimage){this.drawableimage = drawableimage;}
}

