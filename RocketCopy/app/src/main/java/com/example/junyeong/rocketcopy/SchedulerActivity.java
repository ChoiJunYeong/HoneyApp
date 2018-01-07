package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class SchedulerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    UnscrollableGridView schedule;
    SchedulerAdapter adapter;
    ArrayList<String> deleteFiles = new ArrayList<>();
    Set<String> schedules = null;
    HashMap<Integer[],String[]> scheduleHashMap; // HashMap is [day,hour1,min1,hour2,min2], [lecture,professor,color]
    String filepath = Environment.getExternalStorageDirectory().toString() + "/honeyA";
    File myDir = new File(filepath);
    ActionBarDrawerToggle toggle;
    Utils utils = new Utils();
    DialogInterface dialogInterface;
    static int week = 8;
    private Menu menu;
    RelativeLayout currentLayout;
    static int MODE_ADD=0,MODE_MODIFY = 1,CAMERA_ACTIVITY = 2, REQUEST_IMAGE_CAPTURE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);
        //check root directory exists
        //check directory exists
        if(!myDir.mkdirs())
            if(!myDir.getParentFile().exists())
                Toast.makeText(this,"Error" + myDir.getParent(),Toast.LENGTH_SHORT).show();
        if(!myDir.mkdir())
            if(!myDir.exists())
                Toast.makeText(this,"Error" + myDir.toString(),Toast.LENGTH_SHORT).show();
        //action bar setting
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_scheduler);
        setSupportActionBar(toolbar);
        makeToggle(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        scheduleHashMap = getScheduleList();
        loadScheduler();
        setScheduleTextView(scheduleHashMap);

        FloatingActionButton fab = findViewById(R.id.camera2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                /*Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                intent.putExtra("Directory",filepath);
                startActivityForResult(intent,CAMERA_ACTIVITY);*/

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //save 버튼을 눌렸을때
        if(requestCode == MODE_ADD&&resultCode==RESULT_OK) {
            //setTimeTable(data);
            HashMap<Integer[],String[]> newHashMap = getHashMap(data,true);
            setScheduleTextView(newHashMap);
            if(scheduleHashMap == null)
                scheduleHashMap = new HashMap<>();
            if(newHashMap!=null)
                scheduleHashMap.putAll(newHashMap);
        }
        if(requestCode == MODE_MODIFY&&resultCode==RESULT_OK){
            //setTimeTable(data);
            HashMap<Integer[],String[]> newHashMap = getHashMap(data,false);
            String[] value = newHashMap.values().iterator().next();

            ArrayList<Integer[]> removekeyset = new ArrayList<Integer[]>();
            for(Integer[] key : scheduleHashMap.keySet()){
                if(scheduleHashMap.get(key)[0].equals(value[0])){
                    removekeyset.add(key);
                }
            }
            for(Integer[] removeKey : removekeyset)
                scheduleHashMap.remove(removeKey);
            if(scheduleHashMap == null)
                scheduleHashMap = new HashMap<>();
            else
                scheduleHashMap.putAll(newHashMap);
            RelativeLayout rootlayout = findViewById(R.id.scheduleParentLayout);
            for(int i=1;i<rootlayout.getChildCount();i++){
                TextView textView = (TextView) rootlayout.getChildAt(i);
                rootlayout.removeView(rootlayout.getChildAt(i));
                i--;
            }
            setScheduleTextView(scheduleHashMap);
            setModifyMode();
        }
        //
    }
    @Override
    public void onDestroy(){
        System.exit(0);
        super.onDestroy();
    }
    public void makeToggle(Toolbar toolbar){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.scheduler_home);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        toggle.syncState();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scheduler, menu);
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_add:
                addSchedule();
                break;
            case R.id.action_delete:
                onDeleteSchedule();
                break;
            case R.id.action_modify:
                setModifyMode();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //click nav bar
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_scheduler) {
            createView("Schedule");
            menu.setGroupVisible(R.id.default_group,true);
        }
        else if (id == R.id.nav_destinations) {
            createView("Destinations");
            menu.setGroupVisible(R.id.default_group,false);

        }
        else if (id == R.id.nav_settings) {
            createView("Settings");
            menu.setGroupVisible(R.id.default_group,false);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.scheduler_home);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //on navigation click
    void createView(String view){

        //기존 레이아웃 컨텐츠를 삭제
        RelativeLayout scheduleParentLayout = findViewById(R.id.scheduleParentLayout);
        scheduleParentLayout.setVisibility(View.INVISIBLE);
        LinearLayout otherviewLayout = findViewById(R.id.otherviewLayout);
        otherviewLayout.removeAllViews();

            /*변경하고 싶은 레이아웃의 파라미터 값을 가져 옴*/
        RelativeLayout.LayoutParams plControl = (RelativeLayout.LayoutParams) otherviewLayout.getLayoutParams();

            /*해당 margin값 변경*/
        plControl.topMargin = getStatusBarSize();

            /*변경된 값의 파라미터를 해당 레이아웃 파라미터 값에 셋팅*/
        otherviewLayout.setLayoutParams(plControl);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);



        if(view == "Destinations"){

            RelativeLayout destinationsActivity = (RelativeLayout) inflater.inflate(R.layout.activity_destinations, null);
            View action_bar = (View) destinationsActivity.getChildAt(destinationsActivity.getChildCount()-1);
            action_bar.setVisibility(View.GONE);
            otherviewLayout.addView(destinationsActivity);
            setDestinationLayout();



        }
        else if(view == "Schedule"){
            scheduleParentLayout.setVisibility(View.VISIBLE);
            loadScheduler();
        }

        else if(view == "Settings"){
            LinearLayout settingsActivity = (LinearLayout) inflater.inflate(R.layout.activity_settings, null);
            otherviewLayout.addView(settingsActivity);
        }

    }
    public void setDestinationLayout(){
        RelativeLayout rootLayout = findViewById(R.id.destinations);
        //set onclicklistener
        int size = rootLayout.getChildCount();
        for(int i=0;i<size;i++){
            RelativeLayout child = (RelativeLayout) rootLayout.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    redefineDestination((RelativeLayout)view);
                }
            });
        }
        //set textview according to json file
        File jsonFile = new File(myDir,"destInfo.json");
        if(!jsonFile.exists())
            return;
        for(int i=0;i<size;i++){
            try {
                RelativeLayout child = (RelativeLayout) rootLayout.getChildAt(i);
                TextView nameView =(TextView) child.getChildAt(2);
                TextView addressView =(TextView) child.getChildAt(3);

                JSONObject jsonObject = new JSONObject(utils.readJSON(jsonFile));
                JSONObject jsonChild = (JSONObject)jsonObject.get(String.valueOf(i));
                nameView.setText((String)jsonChild.get("name"));
                addressView.setText((String)jsonChild.get("address"));

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    //destination setting alert show
    public void redefineDestination(RelativeLayout relativeLayout){
        currentLayout = relativeLayout;
        //alert dialog selecting send type(email or drive)

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout alertLayout = (RelativeLayout) inflater.inflate(R.layout.alert_select, null);
        RelativeLayout googleDriveLayout = (RelativeLayout)alertLayout.getChildAt(0);
        RelativeLayout emailLayout = (RelativeLayout)alertLayout.getChildAt(1);
        googleDriveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  signIn();
            }
        });
        emailLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                setMailAddress();
                dialogInterface.dismiss();
            }
        });
        alert.setView(alertLayout);
        alert.create();
        dialogInterface = alert.show();
    }
    //alert that modify email address
    public void setMailAddress(){

        //alert dialog view setting
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final EditText destEdit = new EditText(this);
        final EditText addressEdit = new EditText(this);

        TextView textView1 = (TextView) currentLayout.getChildAt(2);
        TextView textView2 = (TextView) currentLayout.getChildAt(3);

        destEdit.setText(textView1.getText());
        addressEdit.setText(textView2.getText());

        TextView textView11 = new TextView(this);
        TextView textView22 = new TextView(this);
        textView11.setText("Destination");
        textView22.setText("Address");
        linearLayout.addView(textView11);
        linearLayout.addView(destEdit);
        linearLayout.addView(textView22);
        linearLayout.addView(addressEdit);

        alert.setView(linearLayout);

        //yes and no button listener
        alert.setPositiveButton("OK",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView textView1 = (TextView) currentLayout.getChildAt(2);
                TextView textView2 = (TextView) currentLayout.getChildAt(3);
                textView1.setText(destEdit.getText());
                textView2.setText(addressEdit.getText());
                utils.writejson(myDir,
                        currentLayout.getContentDescription().toString(),
                        destEdit.getText().toString(),
                        addressEdit.getText().toString());
            }

        });
        alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }

        });
        alert.show();
    }

    //subfunction of createView
    public void checkDefualtDestination(View view){
        Intent intent = new Intent(getApplicationContext(),DestinationsActivity.class);
        startActivity(intent);
    }
    //시간대나 강의명이 겹칠 때 에러만 띄우고 아무것도 하지 않는다
    public boolean isOverlapOther(String[] lectureInfo,HashMap<Integer[],String[]> data) {
        scheduleHashMap=getScheduleList();
        if(scheduleHashMap==null)
            return false;
        //강의명 겹치는지 확인
        for (String[] Unit : scheduleHashMap.values())
            if (lectureInfo[0].equals(Unit[0])) {
                Toast.makeText(this,"이미 존재하는 강의명입니다.",Toast.LENGTH_LONG).show();
                return true;
            }
        //겹치는 시간대가 있는지 확인
        for(Integer[] timedataUnit : data.keySet()){
            for(Integer[] timedataUnit2 : scheduleHashMap.keySet()){
                if(timedataUnit[0].equals(timedataUnit2[0])){
                    if(timedataUnit[1]>=timedataUnit2[1] && timedataUnit[1]<=timedataUnit2[2]) {
                        Toast.makeText(this,"시간이 다른 강의와 겹칩니다.",Toast.LENGTH_LONG).show();
                        return true;
                    }
                    else if(timedataUnit[2]>=timedataUnit2[1] && timedataUnit[2]<=timedataUnit2[2]) {
                        Toast.makeText(this,"시간이 다른 강의와 겹칩니다.",Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
                else
                    continue;
            }
        }
        return false;
    }
    //json파일로 시간표 정보를 로컬에 저장하는 함수
    public String jsonwriting(String name, String professor,String color, int size, Integer[][] timedata){
        JSONObject jsonObject = new JSONObject();
        JSONArray times = new JSONArray();
        try {
            jsonObject.put("color", Integer.parseInt(color));
            jsonObject.put("lecture", name);
            jsonObject.put("professor",professor);
            for(int i=0;i<size;i++) {
                JSONObject time = new JSONObject();
                time.put("day",timedata[i][0]);
                time.put("hour1",timedata[i][1]);
                time.put("min1",timedata[i][2]);
                time.put("hour2",timedata[i][3]);
                time.put("min2",timedata[i][4]);
                times.put(time);
            }
            jsonObject.put("schedule",times);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //make hashMap from intent data
    public HashMap<Integer[],String[]> getHashMap(Intent intent,boolean toAdded) {
        int[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN, Color.MAGENTA}; //color list of scheduler
        HashMap<Integer[], String[]> result = new HashMap<Integer[], String[]>();

        int hashmapsize;
        if (scheduleHashMap == null)
            hashmapsize = 0;
        else
            hashmapsize = scheduleHashMap.size();
        String[] lectureInfo = {intent.getStringExtra("Lecture_name"), intent.getStringExtra("Lecture_professor"), Integer.toString(colors[(int)(Math.random()*colors.length)])};
        int size = intent.getIntExtra("Lecture_size", 1);
        Integer[][] timedata = new Integer[size][5];
        for (int i = 1; i <= size; i++) {
            String[] timeInfoStr = intent.getStringArrayExtra("Lecture" + i);
            Integer[] timeInfo = {0, Integer.parseInt(timeInfoStr[1]), Integer.parseInt(timeInfoStr[2]), Integer.parseInt(timeInfoStr[3]), Integer.parseInt(timeInfoStr[4])};
            switch (timeInfoStr[0]) {
                case "월":
                    timeInfo[0] = 1;
                    break;
                case "화":
                    timeInfo[0] = 2;
                    break;
                case "수":
                    timeInfo[0] = 3;
                    break;
                case "목":
                    timeInfo[0] = 4;
                    break;
                case "금":
                    timeInfo[0] = 5;
                    break;
                case "토":
                    timeInfo[0] = 6;
                    break;
                case "일":
                    timeInfo[0] = 7;
                    break;
            }
            result.put(timeInfo, lectureInfo);
            timedata[i - 1] = timeInfo;
        }
        try {
            if (toAdded && isOverlapOther(lectureInfo, result)) {
                return null;
            }
            File subDir = new File(myDir, lectureInfo[0]);

            //폴더 있는지 체크하고 만듫기
            if (!subDir.mkdirs())
                if (!subDir.getParentFile().exists())
                    Toast.makeText(this, "Error" + subDir.getParent(), Toast.LENGTH_SHORT).show();
            if (!subDir.mkdir())
                if (!subDir.exists())
                    Toast.makeText(this, "Error" + subDir.toString(), Toast.LENGTH_SHORT).show();

            String jsonStr = jsonwriting(lectureInfo[0], lectureInfo[1], lectureInfo[2], size, timedata);
            File jsonFile = new File(subDir, "timesheet.json");
            FileWriter fileWriter = new FileWriter(jsonFile);

            fileWriter.write(jsonStr);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Toast.makeText(this, "json save failed", Toast.LENGTH_SHORT).show();
        }
        return result;
    }
    //시간표를 보여주는 TextView를 적절한 위치에 생성한다.
    public void setScheduleTextView(HashMap<Integer[],String[]> newSchedule){
        if(newSchedule==null)
            return;

        int WIDTH = 135;
        float HEIGHT_HOUR = 150;
        float HEIGHT_MIN = 150/4;


        RelativeLayout scheduleParentLayout = (RelativeLayout) findViewById(R.id.scheduleParentLayout);
        scheduleParentLayout.setPadding(0,getStatusBarSize(),0,0);

        for(Integer[] newScheduleUnit : newSchedule.keySet() ){

            TextView textView = new TextView(this);
            String[] scheduleInfo = newSchedule.get(newScheduleUnit);

            textView.setBackgroundColor(Integer.parseInt(scheduleInfo[2]));

            textView.setText(scheduleInfo[0]);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setTextSize(WIDTH/10);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView text = (TextView) view;
                    if(text.getText()==null)
                        return;
                    showItem(filepath + "/"+ (String)text.getText());
                }
            });

            textView.setX(newScheduleUnit[0]*WIDTH);
            textView.setY((newScheduleUnit[1]-8)*HEIGHT_HOUR + (newScheduleUnit[2]/15)*HEIGHT_MIN);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            float width = WIDTH;
            float height = (newScheduleUnit[3]-newScheduleUnit[1])*HEIGHT_HOUR + (newScheduleUnit[4]/15-newScheduleUnit[2]%15)*HEIGHT_MIN;
            int temp = Math.round(WIDTH);
            textView.setLayoutParams(new ViewGroup.LayoutParams(Math.round(width),Math.round(height)));

            scheduleParentLayout.addView(textView);
        }
    }

    //grid 형식의 스케쥴러
    public void loadScheduler(){
        //show textview
        RelativeLayout scheduleParentLayout = findViewById(R.id.scheduleParentLayout);
        int size = scheduleParentLayout.getChildCount();
        for(int i=1;i<size;i++){
            TextView textView =(TextView) scheduleParentLayout.getChildAt(i);
            textView.setVisibility(View.VISIBLE);
        }
        //load schedule
        schedule = (UnscrollableGridView) findViewById(R.id.schedule);
        schedule.setNumColumns(week);
        adapter = new SchedulerAdapter();
        for(int i=0;i<(21-9+2)*week+1*week;i++) {
            ScheduleItem item = new ScheduleItem();
            item.setTag(Integer.toString(i));
            adapter.addItem(item);
        }
        schedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });

        schedule.setAdapter(adapter);


    }
    //선택한 스케쥴의 폴더를 보여줌
    public void showItem(String folderName){
        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
        intent.putExtra("Directory",folderName);
        startActivity(intent);
    }
    //calculate actionbar height
    private int getStatusBarSize() {
        TypedValue tv = new TypedValue();
        int TitleBarHeight=0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            TitleBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return TitleBarHeight;
    }
    //스케쥴 + 버튼을 누르면 활성화
    public void addSchedule(){
        Intent intent = new Intent(getApplicationContext(),ScheduleAddActivity.class);
        startActivityForResult(intent,MODE_ADD);
    }
    //스케쥴 쓰레기통 버튼을 누르면 활성화
    public void onDeleteSchedule(){
        RelativeLayout scheduleParentLayout = findViewById(R.id.scheduleParentLayout);
        //modify actionbar
        TextView cancle = findViewById(R.id.CancleDelete);
        cancle.setVisibility(View.VISIBLE);
        TextView deleteconfirm = findViewById(R.id.DeleteConfirm);
        deleteconfirm.setVisibility(View.VISIBLE);
        menu.setGroupVisible(R.id.default_group,false);
        //set textview clickable
        for(int i=1;i<scheduleParentLayout.getChildCount();i++){
            TextView textView = (TextView) scheduleParentLayout.getChildAt(i);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView textView = (TextView) view;
                    if(deleteFiles.contains(textView.getText().toString())){
                        deleteFiles.remove(textView.getText().toString());
                        int originalColor = ((ColorDrawable) textView.getBackground()).getColor();
                        textView.setBackgroundColor(originalColor - 0x33000000);
                    }
                    else {
                        deleteFiles.add(textView.getText().toString());
                        int originalColor = ((ColorDrawable) textView.getBackground()).getColor();
                        textView.setBackgroundColor(originalColor + 0x33000000);
                    }
                }
            });
        }

    }
    public void setNormalMode(View view){
        //modify actionbar
        TextView cancle = findViewById(R.id.CancleDelete);
        cancle.setVisibility(View.INVISIBLE);
        TextView deleteconfirm = findViewById(R.id.DeleteConfirm);
        deleteconfirm.setVisibility(View.INVISIBLE);
        menu.setGroupVisible(R.id.default_group,true);
        //clear select list
        RelativeLayout relativeLayout = findViewById(R.id.scheduleParentLayout);
        for(int i=1;i<relativeLayout.getChildCount();i++){
            TextView textView = (TextView) relativeLayout.getChildAt(i);
            if(deleteFiles.contains(textView.getText().toString())){
                int originalColor = ((ColorDrawable) textView.getBackground()).getColor();
                textView.setBackgroundColor(originalColor - 0x33000000);
            }
        }
        deleteFiles.clear();

        //set schedule
        scheduleHashMap = getScheduleList();
        setScheduleTextView(scheduleHashMap);
    }
    public void setModifyMode(){
        //modify actionbar
        TextView cancle = findViewById(R.id.CancleDelete);
        cancle.setVisibility(View.VISIBLE);
        TextView deleteconfirm = findViewById(R.id.DeleteConfirm);
        deleteconfirm.setVisibility(View.VISIBLE);
        menu.setGroupVisible(R.id.default_group,false);
        RelativeLayout relativeLayout = findViewById(R.id.scheduleParentLayout);
        for(int i=1;i<relativeLayout.getChildCount();i++){
            TextView textView = (TextView) relativeLayout.getChildAt(i);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),ScheduleAddActivity.class);
                    //put extras for load information
                    TextView textView = (TextView)view;
                    File file = new File(filepath,textView.getText().toString());
                    String jsonstr = utils.readJSON(new File(file,"timesheet.json"));
                    intent.putExtra("json Data",jsonstr);
                    startActivityForResult(intent,MODE_MODIFY);
                }
            });
        }
    }
    public void onConfirmDeleteResult(){
        //delete directory
        for(String filename : deleteFiles){
            File file = new File(filepath,filename);
            if (file.isDirectory()){
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    new File(file, children[i]).delete();
                }
            }
            file.delete();
        }

    }
    public void confirmDelete(View view){
        //alert massage
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Schedule");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onConfirmDeleteResult();
                //clear schedule
                RelativeLayout relativeLayout = findViewById(R.id.scheduleParentLayout);
                for(int j=1;j<relativeLayout.getChildCount();j++) {
                    TextView textView = (TextView)relativeLayout.getChildAt(j);
                    if(deleteFiles.contains(textView.getText().toString())) {
                        relativeLayout.removeView(textView);
                        j--;
                    }
                }
                //set schedule
                scheduleHashMap = getScheduleList();
                setScheduleTextView(scheduleHashMap);
                setNormalMode(null);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //set schedule
                scheduleHashMap = getScheduleList();
                setScheduleTextView(scheduleHashMap);
                setNormalMode(null);
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //change json file to string 'simply'
    public HashMap<Integer[],String[]> getScheduleList(){
        File[] directories = myDir.listFiles();
        HashMap<Integer[],String[]> result = new HashMap<>();
        //check directory available
        for(File directory : directories){
            if(directory.isDirectory()){
                File[] files = directory.listFiles();
                //find folder contain timesheet.json file
                for(File file : files)
                    if(file.toString().contains("timesheet.json")){
                        //decode json file, mapping <position,{lecture,professor,color}>
                        String jsonStr = utils.readJSON(file);
                        try {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            JSONArray scheduleJsonArray = jsonObject.getJSONArray("schedule");
                            String[] lectureInfo = {jsonObject.get("lecture").toString(),
                                    jsonObject.get("professor").toString(),
                                    jsonObject.get("color").toString()};
                            for(int i=0; i<scheduleJsonArray.length();i++) {
                                JSONObject timeJsonInfo = scheduleJsonArray.getJSONObject(i);
                                Integer[] timeInfo = {timeJsonInfo.getInt("day"),
                                        timeJsonInfo.getInt("hour1"),
                                        timeJsonInfo.getInt("min1"),
                                        timeJsonInfo.getInt("hour2"),
                                        timeJsonInfo.getInt("min2")};
                                result.put(timeInfo, lectureInfo);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
            }
        }
        if(result.size()==0)
            return null;
        return result;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    //adapter for grid
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
            if(position<week){
                view.setTag(days[position]);
            }
            else if(position%week==0){
                String time = Integer.toString(position/week) + "교시";
                view.setTag(time);
            }
            else if(position>=week){
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

    //list 형식의 스케쥴러
    /*public void loadTodo() {
        //set todo
        schedule = (UnscrollableGridView) findViewById(R.id.schedule);
        schedule.setNumColumns(1);
        adapter2 = new TodoAdapter();
        //remove textview
        RelativeLayout scheduleParentLayout = findViewById(R.id.scheduleParentLayout);
        int size = scheduleParentLayout.getChildCount();
        for(int i=1;i<size;i++){
            TextView textView =(TextView) scheduleParentLayout.getChildAt(i);
            textView.setVisibility(View.INVISIBLE);
        }

        //set adapter item
        File[] subDirList = myDir.listFiles();
        TodoItem itemTitle = new TodoItem();
        itemTitle.setTag("Time table");
        adapter2.addItem(itemTitle);
        if(subDirList!=null) {
            for (int i = 0; i < subDirList.length; i++) {
                if (subDirList[i].toString().contains(".")) {
                    continue;
                } else {
                    TodoItem item = new TodoItem();
                    item.setTag(subDirList[i].getName());
                    adapter2.addItem(item);
                }
            }
        }
        schedule.setAdapter(adapter2);

        schedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //구현하기
                ViewGroup viewGroup = (ViewGroup) view;
                RelativeLayout relativeLayout = (RelativeLayout)viewGroup.getChildAt(0);
                TextView textView = (TextView) relativeLayout.getChildAt(0);
                if(view.getContentDescription()=="Title")
                    return;
                showItem(filepath +"/"+ (String)textView.getText());
            }
        });

    }*/
    //adapter for list
    /*public class TodoAdapter extends BaseAdapter {
        ArrayList<TodoItem> items = new ArrayList<TodoItem>();
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
        public void addItem(TodoItem item){
            items.add(item);
        }
        @Override
        public View getView(int position, View oldView, ViewGroup parent){
            TodoItemView view = new TodoItemView(getApplicationContext());
            TodoItem item = items.get(position);
            view.setTag(item.getTag());
            if(position==0){
                view.setContentDescription("Title");
            }
            return view;
        }
        @Override
        public boolean isEnabled(int i){
            return true;
        }
    }*/


}
