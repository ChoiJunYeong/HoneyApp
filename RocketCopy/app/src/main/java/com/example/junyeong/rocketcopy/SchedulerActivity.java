package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class SchedulerActivity extends AppCompatActivity {
    GridView schedule;
    SchedulerAdapter adapter;
    TodoAdapter adapter2;
    Set<String> schedules;
    String filepath = Environment.getExternalStorageDirectory().toString() + "/app/rocket/images";
    File myDir = new File(filepath);
    int week = 8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        //action bar setting
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_scheduler);
        setSupportActionBar(toolbar);

        loadTodo();
        loadScheduler();
        //set toggle button to swap state
        ToggleButton toggle = (ToggleButton) findViewById(R.id.scheduler_toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swapState(0);
                    schedule.setNumColumns(1);
                    // The toggle is enabled
                } else {
                    swapState(1);
                    schedule.setNumColumns(8);
                    // The toggle is disabled
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK && requestCode == 1) {
            String newSchedule = data.getStringExtra("schedule");
            File subDir = new File(myDir,newSchedule);
        }
    }
    public void swapState(int state){
        if(state==0) {
            schedule.setAdapter(adapter2);
        }
        else{
            schedule.setAdapter(adapter);

        }
    }
    public void loadTodo() {
        //set todo
        schedule = (GridView) findViewById(R.id.schedule);
        schedule.setPadding(0,getStatusBarSize(),0,0);
        schedule.setNumColumns(1);
        adapter2 = new TodoAdapter();
        //set adapter item
        //
        File[] subDirList = myDir.listFiles();
        for(int i=0;i<subDirList.length;i++){
            if(i==0){
                adapter2.addItem("TimeTable");
            }
            if(subDirList[i].toString().contains(".")){
                continue;
            }
            else{
                adapter2.addItem(subDirList[i].getName());
            }
        }
        //
        schedule.setAdapter(adapter2);
        schedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //구현하기
                TextView textView = (TextView)findViewById(R.id.text_schedule);
                showItem((String)textView.getText());
            }
        });

    }
    public void loadScheduler(){
        //load schedule
        schedule = (GridView) findViewById(R.id.schedule);
        schedule.setPadding(0,getStatusBarSize(),0,0);
        schedule.setNumColumns(8);
        adapter = new SchedulerAdapter();
        for(int i=0;i<(21-9+1)*week+1*week;i++) {
            ScheduleItem item = new ScheduleItem();
            item.setTag(Integer.toString(i));
            adapter.addItem(item);
        }
        schedule.setAdapter(adapter);
        schedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //구현하기
                if(i<8 || i%8==0)
                    return;
                TextView textView = (TextView)findViewById(R.id.text_schedule);
                if(textView.getText()==null)
                    return;
                showItem((String)textView.getText());
            }
        });
    }
    public void showItem(String folderName){
        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
        intent.putExtra("subDir",folderName);
        startActivity(intent);
    }
    private int getStatusBarSize() {
        TypedValue tv = new TypedValue();
        int TitleBarHeight=0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            TitleBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return TitleBarHeight;
    }
    public void starthome(View view){
        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(intent);
    }
    public void addSchedule(View view){
        Intent intent = new Intent(getApplicationContext(),ScheduleAddActivity.class);
        startActivityForResult(intent,0);
    }
    public void deleteSchedule(View view){
    }
    public class SchedulerAdapter extends BaseAdapter {
        ArrayList<ScheduleItem> items = new ArrayList<ScheduleItem>();
        String[] days={" ","월","화","수","목","금","토","일"};
        @Override
        public int getCount(){return items.size();}
        @Override
        public Object getItem(int arg){
            return items.get(arg);
        }
        @Override
        public long getItemId(int arg){
            return arg;
        }
        public void addItem(ScheduleItem item){
            items.add(item);
        }
        @Override
        public View getView(int position, View oldView, ViewGroup parent){

            ScheduleItemView view = new ScheduleItemView(getApplicationContext());
            ScheduleItem item = items.get(position);
            view.setTag(item.getTag());
            if(position<8){
                view.setTag(days[position]);
            }
            else if(position%8==0){
                String time = Integer.toString(position/8) + "교시";
                view.setTag(time);
            }
            else if(position>=8){
                view.setTag("yeah");
            }
            else{
                view.setTag("else");
            }

            return view;

        }
        @Override
        public boolean isEnabled(int i){
            if(i==0)
                return false;
            else
                return true;
        }
    }
    public class TodoAdapter extends BaseAdapter {
        ArrayList<String> items = new ArrayList<String>();
        @Override
        public int getCount(){return items.size();}
        @Override
        public Object getItem(int arg){
            return items.get(arg);
        }
        @Override
        public long getItemId(int arg){
            return arg;
        }
        public void addItem(String item){
            items.add(item);
        }
        @Override
        public View getView(int position, View oldView, ViewGroup parent){
            TodoItemView view = new TodoItemView(getApplicationContext());
            view.setText(getItem(position).toString());
            return view;
        }
        @Override
        public boolean isEnabled(int i){
            return true;
        }
    }

}
