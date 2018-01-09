package com.example.junyeong.rocketcopy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    File myDir = new File(Environment.getExternalStorageDirectory(),"honeyA");
    FolderAdapter adapter;
    FolderIconAdapter adapter2;
    Utils utils = new Utils();
    ActionBarDrawerToggle toggle;
    Menu menu;
    ArrayList<String> selectedFoldernames;
    static final int REQUEST_ADD_FOLDER=0,REQUEST_MODIFY_FOLDER=REQUEST_ADD_FOLDER, MODE_FOLDER = 0,MODE_FOLDERICON=1;
    int selectedColor = 0x222222,state = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_file);
        setSupportActionBar(toolbar);
        makeToggle(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadFolders();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case REQUEST_ADD_FOLDER:
                loadFolders();
                break;
        }
    }
    public void makeToggle(Toolbar toolbar){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.file_home);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        toggle.syncState();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file, menu);
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
                Intent intent = new Intent(getApplicationContext(),AddFolderActivity.class);
                startActivityForResult(intent,REQUEST_ADD_FOLDER);
                break;
            case R.id.action_delete:
                setDeletemode();
                break;
            case R.id.action_modify:
                setModifyMode();
                break;
            case R.id.action_loadscheduler:
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
                break;
            case R.id.nav_scheduler:
                break;
            case R.id.nav_destinations:
                break;
            case R.id.nav_settings:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.file_home);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void swapViewMode(View view){
        if(state==MODE_FOLDER) {
            ImageButton imageButton = findViewById(R.id.stateImageButton);
            imageButton.setImageDrawable(getDrawable(R.drawable.ic_dashboard_black_24dp));
            loadFolderIconMode();
            state=MODE_FOLDERICON;
        }
        else{
            ImageButton imageButton = findViewById(R.id.stateImageButton);
            imageButton.setImageDrawable(getDrawable(R.drawable.ic_folder_24dp));
            loadFolders();
            state=MODE_FOLDER;
        }
    }
    public void loadFolders(){
        //set filefilter for find json and image
        FilenameFilter jsonFilenameFilter = utils.jsonFilenameFilter;
        FilenameFilter imageFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(",jpeg");
            }
        };
        //set adapter and root layout
        adapter = new FolderAdapter();
        GridView rootLayout = findViewById(R.id.file_parent);
        rootLayout.setNumColumns(1);
        rootLayout.setPadding(0,getStatusBarSize(),0,0);
        for(File folder : myDir.listFiles()) {
            if (folder.isDirectory()) {
                FolderItem folderItem = new FolderItem();
                File imageFolder = new File(folder,"images");
                if(imageFolder.listFiles()==null)
                    continue;
                folderItem.setFilenum(String.valueOf(imageFolder.listFiles(imageFilenameFilter).length));
                folderItem.setFoldername(folder.getName());
                //is item of scheduler
                if (folder.listFiles(jsonFilenameFilter).length!=0) {
                    folderItem.setFoldertype(utils.SCHEDULE);
                }
                //isn't item of scheduler
                else{
                    folderItem.setFoldertype(utils.FOLDER);
                }
                adapter.addItem(folderItem);
            }
        }
        rootLayout.setAdapter(adapter);
        //set all view clickable
        rootLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                RelativeLayout relativeLayout = (RelativeLayout) ((ViewGroup)view).getChildAt(0);
                TextView textView = (TextView)  relativeLayout.getChildAt(1);
                File file = new File(myDir,textView.getText().toString());
                intent.putExtra("Directory",file.toString());
                startActivity(intent);
            }
        });
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
    public void setDeletemode(){
        //swap visible button and invisible button
        TextView textView1 = findViewById(R.id.CancleDelete);
        TextView textView2 = findViewById(R.id.confirm);
        textView1.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
        ImageButton imageButton = findViewById(R.id.stateImageButton);
        imageButton.setVisibility(View.INVISIBLE);
        menu.setGroupVisible(R.id.default_group,false);
        toggle.setDrawerIndicatorEnabled(false);
        //set selectmode
        GridView rootLayout = findViewById(R.id.file_parent);
        selectedFoldernames = new ArrayList<String>();
        rootLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout relativeLayout = (RelativeLayout) ((ViewGroup)view).getChildAt(0);
                TextView textView = (TextView)  relativeLayout.getChildAt(1);
                String foldername = textView.getText().toString();
                if(!selectedFoldernames.contains(foldername)) {
                    selectedFoldernames.add(foldername);
                    view.setBackgroundColor(getColor(R.color.select));
                }
                else {
                    selectedFoldernames.remove(foldername);
                    view.setBackgroundColor(getColor(R.color.pure));
                }
            }
        });
    }
    public void setModifyMode(){
        //swap visible button and invisible button
        ImageButton imageButton = findViewById(R.id.stateImageButton);
        imageButton.setVisibility(View.INVISIBLE);
        menu.setGroupVisible(R.id.default_group,false);
        toggle.setDrawerIndicatorEnabled(false);
        TextView confirmText = findViewById(R.id.confirm);
        confirmText.setVisibility(View.VISIBLE);
        //set selectmode
        GridView rootLayout = findViewById(R.id.file_parent);
        selectedFoldernames = new ArrayList<String>();
        rootLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout relativeLayout = (RelativeLayout) ((ViewGroup)view).getChildAt(0);
                TextView textView = (TextView)  relativeLayout.getChildAt(1);
                String foldername = textView.getText().toString();
                Intent intent = new Intent(getApplicationContext(),AddFolderActivity.class);
                intent.putExtra("Directory",foldername);
                startActivityForResult(intent,REQUEST_MODIFY_FOLDER);
            }
        });

    }
    public void setNormalMode(View view){
        //swap visible button and invisible button
        TextView textView1 = findViewById(R.id.CancleDelete);
        TextView textView2 = findViewById(R.id.confirm);
        textView1.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);
        ImageButton imageButton = findViewById(R.id.stateImageButton);
        imageButton.setVisibility(View.VISIBLE);
        menu.setGroupVisible(R.id.default_group,true);
        toggle.setDrawerIndicatorEnabled(true);
        selectedFoldernames.clear();
        if(state==MODE_FOLDER)
            loadFolders();
        else
            loadFolderIconMode();
    }
    public void loadFolderIconMode(){
        //set views excpets adapter
        GridView rootLayout = findViewById(R.id.file_parent);
        rootLayout.setNumColumns(4);
        rootLayout.setPadding(0,getStatusBarSize(),0,0);
        //set adapter
        adapter2 = new FolderIconAdapter();
        for(File folder : myDir.listFiles()) {
            if (folder.isDirectory()) {
                FolderIconItem folderIconItem = new FolderIconItem();
                folderIconItem.setTag(folder.getName());
                adapter2.addItem(folderIconItem);
            }
        }
        rootLayout.setAdapter(adapter2);
        //set all view clickable
        rootLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                RelativeLayout relativeLayout = (RelativeLayout) ((ViewGroup)view).getChildAt(0);
                TextView textView = (TextView)  relativeLayout.getChildAt(1);
                File file = new File(myDir,textView.getText().toString());
                intent.putExtra("Directory",file.toString());
                startActivity(intent);
            }
        });
    }
    public void confirmDelete(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Schedule");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onConfirmDeleteResult();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setNormalMode(null);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void onClickConfirm(View view){
        if(selectedFoldernames.isEmpty())
            setNormalMode(view);
        else
            confirmDelete(view);
    }
    public void onConfirmDeleteResult(){
        for(String foldername : selectedFoldernames){
            File folder = new File(myDir,foldername);
            if (folder.isDirectory()){
                deleteCildren(folder);
            }
            folder.delete();
        }
        setNormalMode(null);
    }
    public void deleteCildren(File folder){
        String[] children = folder.list();
        for (int i = 0; i < children.length; i++) {
            File file = new File(folder, children[i]);
            if (file.isDirectory()){
                deleteCildren(file);
            }
            file.delete();
        }
    }


    public class FolderAdapter extends BaseAdapter {
        ArrayList<FolderItem> items = new ArrayList<FolderItem>();
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
        public void addItem(FolderItem item){
            items.add(item);
        }
        @Override
        public View getView(int position, View oldView, ViewGroup parent){

            FolderItemView view = new FolderItemView(getApplicationContext());
            FolderItem item = items.get(position);
            view.setFileNum(item.getFilenum());
            view.setFolderName(item.getFoldername());
            view.setFolderType(item.getFoldertype());
            return view;
        }
        @Override
        public boolean isEnabled(int i){
            return true;
        }
    }
    public class FolderIconAdapter extends BaseAdapter {
        ArrayList<FolderIconItem> items = new ArrayList<FolderIconItem>();
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
        public void addItem(FolderIconItem item){
            items.add(item);
        }
        @Override
        public View getView(int position, View oldView, ViewGroup parent){

            FolderIconItemView view = new FolderIconItemView(getApplicationContext());
            FolderIconItem item = items.get(position);
            view.setTag(item.getTag());
            view.setImageView();
            return view;
        }
        @Override
        public boolean isEnabled(int i){
            return true;
        }
    }
}
