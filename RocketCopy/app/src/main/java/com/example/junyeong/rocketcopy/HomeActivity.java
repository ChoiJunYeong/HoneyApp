package com.example.junyeong.rocketcopy;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import android.support.annotation.NonNull;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.junyeong.rocketcopy.Utils.*;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks{
    ActionBarDrawerToggle toggle;
    GridView gridView;
    private Menu menu;
    History_gallery_Adapter adapter;
    File[] imageList;
    String filepath,dest_id;
    File rootDir,myDir;
    RelativeLayout currentLayout;
    Utils utils = new Utils();
    DialogInterface dialogInterface;
    static int Newest=1,Oldest=-1;
    int status=Newest;

    GoogleSignInClient googleSignInClient;
    DriveResourceClient driveResourceClient;
    DriveClient driveClient;
    GoogleApiClient googleApiClient;
    Bitmap currentImage;
    String currentImageName;
    Boolean[] SendList = new Boolean[5];
    private int StatusBarSize;
    final static int GOOGLE_DRIVE_SEND=1;
    public List<String> selectedFiles= new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        StatusBarSize = utils.getStatusBarSize(this);
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
                dispatchTakePictureIntent();
                /*Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                intent.putExtra("Directory",filepath);
                startActivityForResult(intent,2);*/

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
        switch (requestCode) {
            case IMAGE_FOCUS:
                if (resultCode == RESULT_OK) {
                    String imgPath = data.getStringExtra("filepath");
                    File img = new File(imgPath);
                    img.delete();
                    showHistory();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {

                    Bundle extras = data.getExtras();
                    Bitmap image = (Bitmap) extras.get("data");
                    SaveImage(image);
                }
                break;
            case REQUEST_MOVE_IMAGE:
                showHistory();
            default:
                showHistory();
                setNormalMode();
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
                select_All();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //click nav bar
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_all_picture:
                Intent intent = new Intent(getApplicationContext(),PictureListActivity.class);
                startActivity(intent);
            case R.id.nav_history:
                createView("History");
                menu.setGroupVisible(R.id.default_group,true);
                break;
            case R.id.nav_destinations:
                createView("Destinations");
                menu.setGroupVisible(R.id.default_group,false);
                break;
            case R.id.nav_settings:
                createView("Settings");
                menu.setGroupVisible(R.id.default_group,false);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        plControl.topMargin = StatusBarSize;

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
                    dest_id = "destination"+view.getContentDescription();
                    redefineDestination((RelativeLayout)view);
                }
            });
        }
        for(int i=0;i<size;i++){
            SharedPreferences preferences = getSharedPreferences("destination"+String.valueOf(i+1),Context.MODE_PRIVATE);
            RelativeLayout child = (RelativeLayout) rootLayout.getChildAt(i);
            TextView nameView =(TextView) child.getChildAt(2);
            TextView addressView =(TextView) child.getChildAt(3);
            nameView.setText(preferences.getString("name","destination"+String.valueOf(i+1)));
            addressView.setText(preferences.getString("address","address"+String.valueOf(i+1)));
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
                String[] destInfo = {destEdit.getText().toString(),addressEdit.getText().toString(),"email"};
                utils.setDestinationPreference(getSharedPreferences(dest_id,Context.MODE_PRIVATE),destInfo);
            }

        });
        alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }

        });
        alert.show();
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
                startActivityForResult(intent,IMAGE_FOCUS);
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
        plControl.topMargin = StatusBarSize;

            /*변경된 값의 파라미터를 해당 레이아웃 파라미터 값에 셋팅*/
        gridView.setLayoutParams(plControl);

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

    public void select_All(){
        for(View view : adapter.item_views){
            if(!selectedFiles.contains(view.getContentDescription().toString())){
                selectedFiles.add(view.getContentDescription().toString());
                view.setBackgroundColor(getColor(R.color.select));
            }
        }
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
                startActivityForResult(intent,IMAGE_FOCUS);
            }
        });
        //reset select file list
        selectedFiles.clear();
        for(int i=0;i<gridView.getChildCount();i++){
            View child = gridView.getChildAt(i);
            child.setBackgroundColor(getColor(R.color.pure));
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
        ArrayList<Uri> files = new ArrayList<Uri>();
        for(String element : selectedFiles)
            files.add(Uri.parse((new File(filepath,element)).toString()));
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        String text = "다른 앱에 공유하기";
        intent.putExtra(Intent.EXTRA_STREAM, files);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(intent, "이미지 공유하기");
        startActivity(chooser);
    }
    public void moveItems(View view){
        String[] images = new String[selectedFiles.size()];
        for(int i=0;i<selectedFiles.size();i++){
            images[i] = (new File(filepath,selectedFiles.get(i))).toString();
        }
        Intent intent = new Intent(getApplicationContext(),SchedulerActivity.class);
        intent.putExtra("image2move",images);
        startActivityForResult(intent, REQUEST_MOVE_IMAGE);
    }
    public void SaveImage(Bitmap bitmap){
        File myDir = new File(filepath);
        if(!myDir.mkdirs())
            if(!myDir.getParentFile().exists())
                Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
        Date date = new Date();
        String fname = date.getTime() +".jpeg";
        File file = new File (myDir, fname);

        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //how to broadcast? it doesn't do anything
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        showHistory();
        SendList[GOOGLE_DRIVE_SEND]=true;
        if(SendList[GOOGLE_DRIVE_SEND]) {
            driveResourceClient = Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
            driveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .build();
            googleApiClient.connect();
            //saveFileToDrive();
            currentImage = bitmap;
            currentImageName = fname;
            CreateMyFile();
        }
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
//--------------------------------------------------------------------------------------------------------------------
    public void CreateMyFile(){
    // create new contents resource
    Drive.DriveApi.newDriveContents(googleApiClient)
            .setResultCallback(driveContentsCallback);
    }
    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    if (result.getStatus().isSuccess()) {
                            CreateFileOnGoogleDrive(result);
                    }
                }
            };
    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result){

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // Get an output stream for the contents.
                OutputStream outputStream = driveContents.getOutputStream();
                // Write the bitmap data from it.
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                currentImage.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
                try {
                    outputStream.write(bitmapStream.toByteArray());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),"upload fail",Toast.LENGTH_LONG).show();
                }

                MetadataChangeSet changeSet =
                        new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg")
                                .setTitle(currentImageName)
                                .build();
                // create a file in root folder
                Drive.DriveApi.getRootFolder(googleApiClient)
                        .createFile(googleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
            }
        }.start();
    }
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {

                        Toast.makeText(getApplicationContext(), "file created: "+
                                result.getDriveFile().getDriveId(), Toast.LENGTH_LONG).show();

                    }

                    return;

                }
            };

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public class History_gallery_Adapter extends BaseAdapter {
        ArrayList<History_gallery> items = new ArrayList<History_gallery>();
        ArrayList<View> item_views = new ArrayList<View>();
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
            if(item_views.contains(view))
                item_views.remove(view);
            item_views.add(view);
            return view;
        }
        @Override
        public boolean isEnabled(int i){
            return true;
        }
    }

}
