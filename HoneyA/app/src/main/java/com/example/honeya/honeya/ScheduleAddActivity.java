package com.example.honeya.honeya;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Set;

public class ScheduleAddActivity extends AppCompatActivity {
    Spinner spinner;
    Utils utils;
    int spinnum=0;
    int DAY=0,HOUR1=1,MIN1=2,HOUR2=3,MIN2=4;
    String lecture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_add);
        LinearLayout linearLayout = findViewById(R.id.schedule_add_layout);
        addTime(linearLayout);
        Intent intent = getIntent();
        lecture = intent.getStringExtra("lecture");
        if(lecture!=null){
            decodePreference();
        }
        TextView saveButton = (TextView) findViewById(R.id.confirm);
    }
    public RelativeLayout addSpinner(RelativeLayout relativeLayout,int Array_id){
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        spinnum++;
        //add spinner to layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                Array_id, android.R.layout.simple_spinner_item);
        Spinner sp = new Spinner(this);
        sp.setAdapter(adapter);
        sp.setId(spinnum);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relativeLayout.addView(sp,new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        //set layout position
        if(spinnum%5==1){
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        }
        else if(spinnum%5==4){
            params.addRule(RelativeLayout.START_OF,spinnum-2);
            params.addRule(RelativeLayout.BELOW,spinnum-2);
        }

        else if(spinnum%5==2 || spinnum%5==3){
            params.addRule(RelativeLayout.END_OF,(spinnum-1)+500);
        }
        else{
            params.addRule(RelativeLayout.END_OF,(spinnum-1)+500);
            params.addRule(RelativeLayout.ALIGN_TOP,(spinnum-1)+500);
        }
        sp.setLayoutParams(params);
        return relativeLayout;
    }
    public RelativeLayout addSpinnerText(RelativeLayout relativeLayout,String text){
        TextView textView = new TextView(this);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        textView.setId(spinnum+500);
        textView.setText(text);
        params.addRule(RelativeLayout.END_OF,spinnum);
        if(spinnum%5==4 || spinnum%5==0)
            params.addRule(RelativeLayout.ALIGN_TOP,spinnum);
        textView.setLayoutParams(params);
        relativeLayout.addView(textView);
        return relativeLayout;
    }
    public void addTime(View view){
        //add relative layout
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.schedule_add_layout);
        RelativeLayout relativeLayout = (RelativeLayout)new RelativeLayout(this);
        //set spinner and textview
        relativeLayout = addSpinner(relativeLayout,R.array.array_days);
        relativeLayout = addSpinnerText(relativeLayout,"요일");

        relativeLayout = addSpinner(relativeLayout,R.array.array_hours);
        relativeLayout = addSpinnerText(relativeLayout,"시");
        relativeLayout = addSpinner(relativeLayout,R.array.array_mins);
        relativeLayout = addSpinnerText(relativeLayout,"분 ~");
        relativeLayout = addSpinner(relativeLayout,R.array.array_hours);
        relativeLayout = addSpinnerText(relativeLayout,"시");
        relativeLayout = addSpinner(relativeLayout,R.array.array_mins);
        relativeLayout = addSpinnerText(relativeLayout,"분");
        //add delete button
        ImageButton imageButton = new ImageButton(this);
        imageButton.setImageDrawable(getResources().getDrawable(R.drawable.trash_small));
        imageButton.setBackgroundColor(getResources().getColor(R.color.pure));
        relativeLayout.addView(imageButton);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        imageButton.setLayoutParams(params);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteLayout((RelativeLayout)view.getParent());
            }
        });


        //set to the view
        Button button = (Button) findViewById(R.id.add);
        ViewGroup viewGroup = (ViewGroup)linearLayout;
        viewGroup.removeView(button);
        linearLayout.addView(relativeLayout);
        viewGroup.addView(button);
        //add spinner and text view to layout
    }
    public String[] resolveSpinners(int id){
        String[] ret = new String[5];
        for(int i=0;i<5;i++){
            Spinner sp = (Spinner) findViewById(i+id);
            if(sp==null)
                return null;
            ret[i] = sp.getSelectedItem().toString();
        }
        return ret;
    }
    public void deleteLayout(RelativeLayout view){
        ViewGroup parent = (ViewGroup)view.getParent();
        parent.removeView(view);
    }
    public boolean isValidTime(String[] lec_time){
        if(Integer.parseInt(lec_time[HOUR1])<Integer.parseInt(lec_time[HOUR2]))
            return true;
        else if(Integer.parseInt(lec_time[HOUR1])>Integer.parseInt(lec_time[HOUR2]))
            return false;
        else if(Integer.parseInt(lec_time[MIN1])<Integer.parseInt(lec_time[MIN2]))
            return true;
        else
            return false;
    }
    public void save(View view){
        //save item
        Intent intent= new Intent();
        EditText professor,lecture;
        String professor_text,lecture_text,day,hour1,hour2,min1,min2;
        String[] lec_time = new String[5];
        professor = findViewById(R.id.professor);
        lecture = findViewById(R.id.lecture);
        professor_text = professor.getText().toString();
        lecture_text = lecture.getText().toString();
        if(professor_text.isEmpty() ||lecture_text.isEmpty()){
            Toast.makeText(this,"Invaild input exists",Toast.LENGTH_SHORT).show();
            return;
        }

        int removeCount=0;
        for(int i=1; i<=spinnum;i+=5){
            lec_time = resolveSpinners(i);
            if(lec_time == null) {
                removeCount++;
                continue;
            }
            if(!isValidTime(lec_time)){
                return;
            }
            intent.putExtra("Lecture"+Integer.toString(i/5+1 - removeCount),lec_time);
        }
        if(this.lecture!=null){
            //delete all information of lecture(old information)
            SharedPreferences preferences = getSharedPreferences(this.lecture,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor= preferences.edit();
            editor.clear().apply();
            //delete key of this lecture(may be old key)
            preferences = getSharedPreferences("schedule",Context.MODE_PRIVATE);
            Set<String> lectures = preferences.getStringSet("lectures",null);
            if(lectures!=null)
                lectures.remove(this.lecture);
            editor = preferences.edit();
            editor.putStringSet("lectures",lectures);
        }
        intent.putExtra("Lecture_name",lecture_text);
        intent.putExtra("Lecture_professor",professor_text);
        intent.putExtra("Lecture_size",spinnum/5 - removeCount);
        setResult(RESULT_OK,intent);
        finish();
    }
    public void decodePreference(){
        String[] lectureInfo = new String[3];
        lectureInfo[0] = lecture;
        SharedPreferences preferences = getSharedPreferences(lecture, Context.MODE_PRIVATE);
        lectureInfo[1] = preferences.getString("professor","");
        lectureInfo[2] = preferences.getString("color","000000");
        //set textview for lecture name and professor name
        TextView lectureTextView = findViewById(R.id.lecture);
        TextView professorTextView = findViewById(R.id.professor);
        lectureTextView.setText(lectureInfo[0]);
        professorTextView.setText(lectureInfo[1]);
        //remove layout(created at onCreate)
        LinearLayout rootLayout = findViewById(R.id.schedule_add_layout);
        RelativeLayout relativeLayout = (RelativeLayout)rootLayout.getChildAt(2);
        deleteLayout(relativeLayout);

        //spinners setting
        int size = preferences.getInt("size",0);
        for(int i=0; i<size;i++) {
            //get time informations
            Integer[] timeInfo = {preferences.getInt("day"+i,1)-1,
                    preferences.getInt("Shour"+i,9)-9,
                    preferences.getInt("Smin"+i,0),
                    preferences.getInt("Ehour"+i,9)-9,
                    preferences.getInt("Emin"+i,0)};
            //set time informations
            addTime(rootLayout);
            RelativeLayout layout = (RelativeLayout) rootLayout.getChildAt(rootLayout.getChildCount()-2);
            for(int j=0;j<5;j++){
                Spinner spinner = (Spinner)layout.getChildAt(j*2);
                spinner.setSelection(timeInfo[j]);
            }
        }
    }
}
