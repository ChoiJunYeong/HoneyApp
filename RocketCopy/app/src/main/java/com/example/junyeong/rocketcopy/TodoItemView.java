package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by junyeong on 17. 11. 27.
 */

public class TodoItemView extends LinearLayout {
    TextView textView;
    TodoItemView(Context context){
        super(context);
        init(context);
    }

    TodoItemView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(context);
    }
    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.todo_item,this,true);
        textView = (TextView) findViewById(R.id.todo_text);
    }
    public void setText(String text){
        textView.setText(text);
    }
}
