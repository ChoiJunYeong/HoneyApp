package com.example.junyeong.rocketcopy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ScheduleAddActivity extends AppCompatActivity {
    Spinner spinner;
    int spinnum=0;
    int DAY=0,HOUR1=1,MIN1=2,HOUR2=3,MIN2=4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_add);
        LinearLayout linearLayout = findViewById(R.id.schedule_add_layout);
        addTime(linearLayout);
        Intent intent = getIntent();
        String jsonstr = intent.getStringExtra("json Data");
        if(jsonstr != null){
            decodeJson(jsonstr);
        }

    }
    public RelativeLayout addSpinner(RelativeLayout relativeLayout,int Array_id){
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        spinnum++;
        //add spinner to layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                Array_id, android.R.layout.simple_spinner_item);
        Spinner sp = new Spinner(this);
        sp.setBackground(getResources().getDrawable(android.R.drawable.edit_text));
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
        textView.setTextColor(getColor(R.color.black));
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
        imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_delete));
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
    public void saveItem(View view){
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
        intent.putExtra("Lecture_name",lecture_text);
        intent.putExtra("Lecture_professor",professor_text);
        intent.putExtra("Lecture_size",spinnum/5 - removeCount);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void decodeJson(String jsonstr){
        try {
            //get lecture name and professor name
            JSONObject jsonObject = new JSONObject(jsonstr);
            JSONArray scheduleJsonArray = jsonObject.getJSONArray("schedule");
            String[] lectureInfo = {jsonObject.get("lecture").toString(),
                    jsonObject.get("professor").toString(),
                    jsonObject.get("color").toString()};
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
            for(int i=0; i<scheduleJsonArray.length();i++) {
                //get time informations
                JSONObject timeJsonInfo = scheduleJsonArray.getJSONObject(i);
                Integer[] timeInfo = {timeJsonInfo.getInt("day")-1,
                        timeJsonInfo.getInt("hour1")-9,
                        timeJsonInfo.getInt("min1"),
                        timeJsonInfo.getInt("hour2")-9,
                        timeJsonInfo.getInt("min2")};
                //set time informations
                addTime(rootLayout);
                RelativeLayout layout = (RelativeLayout) rootLayout.getChildAt(rootLayout.getChildCount()-2);
                for(int j=0;j<5;j++){
                    Spinner spinner = (Spinner)layout.getChildAt(j*2);
                    spinner.setSelection(timeInfo[j]);
                }
            }
        }catch (Exception e){ e.printStackTrace();}
    }
}

