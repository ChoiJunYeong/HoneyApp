package com.example.junyeong.rocketcopy;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ActionBarDrawerToggle toggle;
    GridView gridView;
    private Menu menu;
    History_gallery_Adapter adapter;
    File[] imageList;
    String filepath = Environment.getExternalStorageDirectory().toString() + "/app/rocket/images";
    File myDir = new File(filepath);
    static int Newest=1,Oldest=-1;
    int status=Newest;
    List<String> selectedFiles= new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//start of default job
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//end of default job

        //action bar setting

        showHistory();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK && requestCode == 1) {
            String imgPath = data.getStringExtra("filepath");
            File img = new File(imgPath);
            img.delete();
            showHistory();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_new_sort:
                status=Newest;
                showHistory();
                break;
            case R.id.action_old_sort:
                Toast.makeText(this, String.valueOf(item.getItemId()),Toast.LENGTH_LONG).show();
                status=Oldest;
                showHistory();
                break;
            case R.id.action_delete_all:
                checkDelete_All();
                break;
            case R.id.action_select:
                setSelectMode();
                break;
            case R.id.action_delete:
                for(String element : selectedFiles){
                    File file = new File(filepath,element);
                    file.delete();
                    showHistory();
                }
                selectedFiles.clear();
                setNormalMode();
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
        if (id == R.id.nav_history) {
            createView("History");
            // Handle the camera action
        }
        else if (id == R.id.nav_destinations) {
            createView("Destinations");

        }
        else if (id == R.id.nav_settings) {
            createView("Settings");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //return status-bar size for getting layout offset
//to understand
    private int getStatusBarSize() {
        TypedValue tv = new TypedValue();
        int TitleBarHeight=0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            TitleBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return TitleBarHeight;
    }


    void createView(String view){

        //기존 레이아웃 컨텐츠를 삭제
        LinearLayout linearLayout = findViewById(R.id.target);
        GridView gridView = findViewById(R.id.gridView);
        linearLayout.removeAllViewsInLayout();
        gridView.removeAllViewsInLayout();

            /*변경하고 싶은 레이아웃의 파라미터 값을 가져 옴*/
        RelativeLayout.LayoutParams plControl = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();

            /*해당 margin값 변경*/
        plControl.topMargin = getStatusBarSize();

            /*변경된 값의 파라미터를 해당 레이아웃 파라미터 값에 셋팅*/
        linearLayout.setLayoutParams(plControl);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if(view == "Destinations"){
            LinearLayout destinationsActivity = (LinearLayout) inflater.inflate(R.layout.activity_destinations, null);
            linearLayout.addView(destinationsActivity);

            RelativeLayout relay1 = (RelativeLayout) findViewById(R.id.relay1);
            relay1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),DestSettingActivity.class);
                    intent.putExtra("Activity_name","relay1");
                    startActivity(intent);
                }
            });

            RelativeLayout relay2 = (RelativeLayout) findViewById(R.id.relay2);
            relay2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),DestSettingActivity.class);
                    intent.putExtra("Activity_name","relay2");
                    startActivity(intent);
                }
            });

            RelativeLayout relay3 = (RelativeLayout) findViewById(R.id.relay3);
            relay3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),DestSettingActivity.class);
                    intent.putExtra("Activity_name","relay3");
                    startActivity(intent);
                }
            });
        }
        else if(view == "History"){
            showHistory();
        }

        else if(view == "Settings"){
            LinearLayout settingsActivity = (LinearLayout) inflater.inflate(R.layout.activity_settings, null);
            linearLayout.addView(settingsActivity);
        }

    }



    public class History_gallery_Adapter extends BaseAdapter {
        ArrayList<History_gallery> items = new ArrayList<History_gallery>();

        @Override
        public int getCount(){
            return items.size();
        }
        public void addItem(History_gallery item){
            items.add(item);
        }
        @Override
        public Object getItem(int position){
            return items.get(position);
        }
        @Override
        public long getItemId(int position){
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup){
            History_gallery_View view = new History_gallery_View(getApplicationContext());
            History_gallery item = items.get(position);
            view.setImage(item.getImg());
            view.setTag(item.getTag());
            view.setContentDescription(item.getTag());
            view.setBackgroundColor(getColor(R.color.pure));
            if(selectedFiles.contains(item.getTag()))
                view.setBackgroundColor(getColor(R.color.select));
            return view;
        }
        @Override
        public boolean isEnabled(int i){
            return true;
        }
    }

    public void showHistory(){


        //decalre
        gridView = (GridView) findViewById(R.id.gridView);
        adapter = new History_gallery_Adapter();

        //make list
        if(myDir.length()!=0) {
            imageList = myDir.listFiles();
            imageList = sortImage(imageList);
            long len = imageList.length;
            String lenStr = Long.toString(len);
            for(int i=0;i<imageList.length;i++) {
                History_gallery item = new History_gallery();
                item.setImg(BitmapFactory.decodeFile(imageList[i].getPath()));
                item.setTag(imageList[i].getName());
                adapter.addItem(item);
            }
        }
        //show
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
                TextView textView = findViewById(R.id.text);
                String imgPath = textView.getText().toString();
                File imgFile = new File (myDir, imgPath);
                Intent intent = new Intent(getApplicationContext(), FocusImage.class);
                intent.putExtra("file name",imgFile.toString());
                startActivityForResult(intent,1);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setBackgroundColor(getColor(R.color.select));
                selectedFiles.add((String)view.getContentDescription());
                setSelectMode();
                return true;
            }
        });


            /*변경하고 싶은 레이아웃의 파라미터 값을 가져 옴*/
        RelativeLayout.LayoutParams plControl = (RelativeLayout.LayoutParams) gridView.getLayoutParams();

            /*해당 margin값 변경*/
        plControl.topMargin = getStatusBarSize();

            /*변경된 값의 파라미터를 해당 레이아웃 파라미터 값에 셋팅*/
        gridView.setLayoutParams(plControl);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public Bitmap resize(Bitmap bitmap){
        return Bitmap.createScaledBitmap(bitmap, 150, 150, false);
    }

    public void Oldest_to_Newest(View view){
        status = Oldest;
        showHistory();
    }

    public void Newest_to_Oldest(View view){
        status = Newest;
        showHistory();
    }

    public File[] sortImage(File[] imageList){
        //base case
        if(imageList.length<=1)
            return imageList;
        //base case 2
        else if(imageList.length==2){
            if(imageList[0].getName().compareTo(imageList[1].getName())*status<0) {
                File[] copy = new File[2];
                copy[0]=imageList[1];
                copy[1]=imageList[0];
                return copy;
            }
            else
                return imageList;
        }
        //normal case
        else{
            File[] head = new File[imageList.length/2];
            File[] tail = new File[imageList.length - head.length];
            //split and sort each part
            System.arraycopy(imageList,0,head,0,head.length);
            System.arraycopy(imageList,head.length,tail,0,tail.length);
            head = sortImage(head);
            tail = sortImage(tail);
            //merge
            int Hindex=0,Tindex=0;
            for(int i=0;i<imageList.length;i++){
                if(Hindex>=head.length) {
                    imageList[i] = tail[Tindex];
                    Tindex++;
                }
                else if(Tindex>=tail.length) {
                    imageList[i] = head[Hindex];
                    Hindex++;
                }
                else if(head[Hindex].getName().compareTo(tail[Tindex].getName())*status>0){
                    imageList[i]=head[Hindex];
                    Hindex++;
                }
                else{
                    imageList[i]=tail[Tindex];
                    Tindex++;
                }
            }
            return imageList;
        }
    }
    public void checkDelete_All(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All");
        builder.setTitle("Are you sure you want to delete all scans?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteDirectoryChild(myDir);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void deleteDirectoryChild(File folder){
        for(File child : folder.listFiles()){
            child.delete();
            showHistory();
        }
    }
    public void setSelectMode(){
        menu.setGroupVisible(R.id.selection_group,true);
        menu.setGroupVisible(R.id.default_group,false);
        ImageButton imageButton = findViewById(R.id.goBackButton);
        imageButton.setVisibility(View.VISIBLE);
        toggle.setDrawerIndicatorEnabled(false);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int background_color = ((ColorDrawable) view.getBackground()).getColor();
                if(background_color == getColor(R.color.pure)) {
                    view.setBackgroundColor(getColor(R.color.select));
                    selectedFiles.add((String)view.getContentDescription());
                }
                else{
                    view.setBackgroundColor(getColor(R.color.pure));
                    selectedFiles.remove((String)view.getContentDescription());
                }
            }
        });
    }
    public void setNormalMode(){

        menu.setGroupVisible(R.id.selection_group,false);
        menu.setGroupVisible(R.id.default_group,true);
        ImageButton imageButton = findViewById(R.id.goBackButton);
        imageButton.setVisibility(View.INVISIBLE);
        toggle.setDrawerIndicatorEnabled(true);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
                TextView textView = findViewById(R.id.text);
                String imgPath = textView.getText().toString();
                File imgFile = new File (myDir, imgPath);
                Intent intent = new Intent(getApplicationContext(), FocusImage.class);
                intent.putExtra("file name",imgFile.toString());
                startActivityForResult(intent,1);
            }
        });
        selectedFiles.clear();
        for(int i=0;i<gridView.getChildCount();i++){
            View child = gridView.getChildAt(i);
            child.setBackgroundColor(getColor(R.color.pure));
        }
    }
    public void goHome(View view){
        setNormalMode();
    }
}
