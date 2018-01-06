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
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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
import android.widget.EditText;
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

import org.json.JSONObject;

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
    String filepath;
    File rootDir,myDir;
    RelativeLayout currentLayout;
    Utils utils = new Utils();
    DialogInterface dialogInterface;
    static int Newest=1,Oldest=-1;
    int status=Newest;
    public List<String> selectedFiles= new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        filepath = intent.getStringExtra("Directory") + "/images";
        myDir = new File(filepath);
        rootDir = new File(intent.getStringExtra("Directory"));
//start of default job
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                intent.putExtra("Directory",filepath);
                startActivityForResult(intent,2);

            }
        });
        //menu toggle button make
        makeToggle(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//end of default job

        //action bar setting

        showHistory();

    }
    public void makeToggle(Toolbar toolbar){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        toggle.syncState();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK && requestCode == 1) {
            String imgPath = data.getStringExtra("filepath");
            File img = new File(imgPath);
            img.delete();
            showHistory();
        }
        else if(resultCode==RESULT_CANCELED){
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
                checkDelete_Select();
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
            //remove floating camera button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
            fab.setVisibility(View.INVISIBLE);

            RelativeLayout destinationsActivity = (RelativeLayout) inflater.inflate(R.layout.activity_destinations, null);
            View action_bar = (View) destinationsActivity.getChildAt(destinationsActivity.getChildCount()-1);
            action_bar.setVisibility(View.GONE);
            linearLayout.addView(destinationsActivity);
            setDestinationLayout();

        }
        else if(view == "History"){
            //floating camera button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
            fab.setVisibility(View.VISIBLE);
            showHistory();
        }

        else if(view == "Settings"){
            //remove floating camera button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
            fab.setVisibility(View.INVISIBLE);
            LinearLayout settingsActivity = (LinearLayout) inflater.inflate(R.layout.activity_settings, null);
            linearLayout.addView(settingsActivity);
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
        File jsonFile = new File(rootDir,"destInfo.json");
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

        //check directory exists
        if(!myDir.mkdirs())
            if(!myDir.getParentFile().exists())
                Toast.makeText(this,"Error" + myDir.getParent(),Toast.LENGTH_SHORT).show();
        if(!myDir.mkdir())
            if(!myDir.exists())
                Toast.makeText(this,"Error" + myDir.toString(),Toast.LENGTH_SHORT).show();


        //make list
        if(myDir.length()!=0) {
            imageList = myDir.listFiles();
            imageList = sortImage(imageList);
            long len = imageList.length;
            String lenStr = Long.toString(len);
            for(int i=0;i<imageList.length;i++) {
                History_gallery item = new History_gallery();
                item.setImg(resize(BitmapFactory.decodeFile(imageList[i].getPath())));
                item.setTag(imageList[i].getName());
                adapter.addItem(item);
            }
        }
        //show
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
        return Bitmap.createScaledBitmap(bitmap, 180, 150, false);
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

    public void checkDelete_Select(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setTitle("Are you sure you want to delete selected scan(s)?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(String element : selectedFiles){
                    File file = new File(filepath,element);
                    file.delete();
                    showHistory();
                }
                selectedFiles.clear();
                setNormalMode();
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
        //swap toolbar setting
        menu.setGroupVisible(R.id.selection_group,true);
        menu.setGroupVisible(R.id.default_group,false);
        ImageButton imageButton = findViewById(R.id.goBackButton);
        imageButton.setVisibility(View.VISIBLE);
        toggle.setDrawerIndicatorEnabled(false);
        LinearLayout linearLayout = findViewById(R.id.bottom_buttons);
        linearLayout.setVisibility(View.VISIBLE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
        fab.setVisibility(View.INVISIBLE);

        //toolbar title point setting
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.RIGHT_OF, R.id.goBackButton);
        toolbar.setLayoutParams(params);

        //make image selector normal
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
        //swap setting
        menu.setGroupVisible(R.id.selection_group,false);
        menu.setGroupVisible(R.id.default_group,true);
        ImageButton imageButton = findViewById(R.id.goBackButton);
        imageButton.setVisibility(View.INVISIBLE);
        toggle.setDrawerIndicatorEnabled(true);
        LinearLayout linearLayout = findViewById(R.id.bottom_buttons);
        linearLayout.setVisibility(View.INVISIBLE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.camera);
        fab.setVisibility(View.VISIBLE);

        //make toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.removeRule(RelativeLayout.RIGHT_OF);
        toolbar.setLayoutParams(params);

        //set image select mode
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = findViewById(R.id.text);
                String imgPath = textView.getText().toString();
                File imgFile = new File (myDir, imgPath);
                Intent intent = new Intent(getApplicationContext(), FocusImage.class);
                intent.putExtra("file name",imgFile.toString());
                startActivityForResult(intent,1);
            }
        });
        //reset select file list
        selectedFiles.clear();
        for(int i=0;i<gridView.getChildCount();i++){
            View child = gridView.getChildAt(i);
            child.setBackgroundColor(getColor(R.color.pure));
        }
    }
    public void goHome(View view){
        setNormalMode();
    }
    public void checkDelete(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Scan");
        builder.setMessage("Are you sure you want to delete this scan?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(String element : selectedFiles){
                    File file = new File(filepath,element);
                    file.delete();
                    showHistory();
                }
                selectedFiles.clear();
                setNormalMode();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }
    public void share(View view){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
// Set default text message
// 카톡, 이메일, MMS 다 이걸로 설정 가능
//String subject = "문자의 제목";
        String text = "다른 앱에 공유하기";
//intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
// Title of intent
        Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
        startActivity(chooser);
    }
//----------------------------------------------------------------------------------------------------------------
    public void checkDefualtDestination(View view){
        Intent intent = new Intent(getApplicationContext(),DestinationsActivity.class);
        startActivity(intent);
    }
    public void showAbout(View view){
        Intent intent = new Intent(getApplicationContext(),AboutActivity.class);
        startActivity(intent);
    }
    public void showTips(View view){}
    public void showScanSetting(View view){
        Intent intent = new Intent(getApplicationContext(),ScanSettingActivity.class);
        startActivity(intent);
    }
}
