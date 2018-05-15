package com.example.honeya.honeya;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by junyeong on 18. 3. 9.
 */

public class ScheduleItemView extends LinearLayout {
    TextView textView;
    public ScheduleItemView(Context context){
        super(context);
        init(context);
    }
    public ScheduleItemView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }
    public ScheduleItemView(Context context,int layout){
        super(context);
        init(context,layout);
    }
    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.schedule_item,this,true);
        textView = (TextView) findViewById(R.id.text_schedule);
    }
    public void init(Context context,int layout){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layout,this,true);
        textView = (TextView) findViewById(R.id.text_schedule);
    }
    public void setTag(String text){
        textView.setText(text);
    }
}