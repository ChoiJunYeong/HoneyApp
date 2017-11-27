package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by junyeong on 17. 11. 27.
 */

public class ScheduleItem{
    String tag;
    public ScheduleItem(){
        return;
    }
    public String getTag(){
        return tag;
    }
    public void setTag(String tag){
        this.tag=tag;
    }

}
