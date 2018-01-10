package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 Name will set to directory name
 Memo will save as memo.txt in directory "/name"
*/
public class AddFolderActivity extends AppCompatActivity {
    File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/honeyA");
    String filename;
    final String[] ReservedChars = {"|", "\\", "?", "*", "<", "\"", ":", ">","/"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_folder);
        //set action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_file);
        setSupportActionBar(toolbar);
        RelativeLayout rootLayout = findViewById(R.id.textsParentLayout);
        rootLayout.setPadding(0,getStatusBarSize(),0,0);
        //check is it modify mode
        filename = getIntent().getStringExtra("Directory");
        if(filename!=null){
            //set name space
            EditText editName = findViewById(R.id.nameEdit);
            editName.setText(filename);
            //set memo space
            EditText editMemo = findViewById(R.id.memoEdit);
            File memoFile = (new File(myDir,filename+"/memo.txt"));
            try {
                //read text file
                BufferedReader br = new BufferedReader(new FileReader(memoFile));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                String lineSparator = "";
                while (line != null) {
                    sb.append(lineSparator+line);
                    sb.append(lineSparator);
                    lineSparator=System.lineSeparator();
                    line = br.readLine();
                }
                //set memo
                String memo = sb.toString();
                editMemo.setText(memo);
            }catch (Exception e){ e.printStackTrace();}
        }
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
    public void saveItem(View view){
        EditText editName = findViewById(R.id.nameEdit);
        EditText editMemo = findViewById(R.id.memoEdit);
        File folder = new File(myDir, editName.getText().toString());
        if(!isValid(folder)){
            Toast.makeText(this,"폴더가 이미 존재하거나 유효하지 않은 폴더명입니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(filename==null || !filename.equals(editName.getText().toString())) {
            //make folder
            if (!folder.mkdir()) {
                if (!folder.exists()) {
                    Toast.makeText(getApplicationContext(), "Can't make directory. error.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            File imageFolder = new File(folder,"images");
            if(!imageFolder.mkdir()) {
                if (!imageFolder.exists()) {
                    Toast.makeText(getApplicationContext(), "Can't make directory. error.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        if(filename!=null){
            //move folder
            File oldFile = new File(myDir,filename);
            if(!move(folder,oldFile))
                Toast.makeText(getApplicationContext(),"Error occur",Toast.LENGTH_SHORT).show();
        }
        //make memo text file
        File memo = new File(folder,"memo.txt");
        try{
            if(!memo.createNewFile())
                if(!memo.exists())
                    Toast.makeText(getApplicationContext(),"Can't make memo file. error.",Toast.LENGTH_SHORT).show();
        }catch (Exception e){e.printStackTrace();}
        try {
            PrintWriter writer = new PrintWriter(memo, "UTF-8");
            writer.print(editMemo.getText());
            writer.flush();
            writer.close();
        }catch (Exception e){ e.printStackTrace();}
        finish();
    }
    public boolean isValid(File folder){
        if(folder.exists() && filename!=null){
            return false;
        }
        int size = ReservedChars.length;
        String folderName = folder.getName();
        for(int i=0;i<size;i++){
            if(folderName.contains(ReservedChars[i]))
                return false;
        }
        return true;
    }
    public boolean move(File newFile,File oldFile){
        if(oldFile.isDirectory()){
            for(File file : oldFile.listFiles()){
                if(!move(new File(newFile,file.getName()),file))
                    return false;
            }
        }
        oldFile.renameTo(newFile);
        if(oldFile.exists())
            oldFile.delete();
        return newFile.exists();
    }
}
