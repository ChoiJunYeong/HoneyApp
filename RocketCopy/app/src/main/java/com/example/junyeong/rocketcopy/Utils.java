package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by junyeong on 18. 1. 5.
 */

public class Utils {
    FilenameFilter jsonFilenameFilter;
    final static int FOLDER=0,SCHEDULE=1;
    final static int MODE_ADD=0,MODE_MODIFY = 1,CAMERA_ACTIVITY = 2, REQUEST_IMAGE_CAPTURE = 2,MODE_FOLDER_ACTIVITY=3,MODE_FOLDER_ADD=4;
    final static int IMAGE_FOCUS=1,REQUEST_CODE_SIGN_IN=3, REQUEST_MOVE_IMAGE=0;
    final static int REQUEST_ADD_FOLDER=0,REQUEST_MODIFY_FOLDER=REQUEST_ADD_FOLDER, MODE_FOLDER = 0,MODE_FOLDERICON=1;
    final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    Utils(){
         jsonFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.equals("timesheet.json"))
                    return true;
                else
                    return false;
            }
        };
    }
    //change json file to string 'simply'
    public String readJSON(File filename){
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //save destination information as json format
    public void writejson(File myDir,String num,String name,String address){
        File jsonfile = new File(myDir,"destInfo.json");
        JSONObject jsonObject = new JSONObject();
        if(!jsonfile.exists()){
            //make json file
            for(int i=1;i<=6;i++){
                JSONObject jsonUnit = new JSONObject();
                try {
                    jsonUnit.put("name", "destination"+i);
                    jsonUnit.put("address","address"+i);
                    jsonObject.put(String.valueOf(i),jsonUnit);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        try {
            if(!jsonObject.has("1")){
                jsonObject = new JSONObject(readJSON(jsonfile));
            }
            //replace jsonobject to new address
            jsonObject.remove(num);
            JSONObject jsonUnit = new JSONObject();
            jsonUnit.put("name",name);
            jsonUnit.put("address",address);
            jsonObject.put(num,jsonUnit);
            FileWriter fileWriter = new FileWriter(jsonfile);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public int getStatusBarSize(Context context) {
        TypedValue tv = new TypedValue();
        int TitleBarHeight=0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            TitleBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
        }
        return TitleBarHeight;
    }
    public File[] sortByname(File[] list){
        Arrays.sort(list, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                String fileName1 = file1.getName();
                String fileName2 = file2.getName();
                return fileName1.compareTo(fileName2);
            }
        });
        return list;
    }
    public int string2int(String str){
        int value=0;
        for(byte unitValue : str.getBytes()){
            value += unitValue;
        }
        return value;
    }
    public void setDestinationPreference(SharedPreferences preference,String[] destInfo){
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("name",destInfo[0]);
        editor.putString("address",destInfo[1]);
        editor.putString("type",destInfo[2]);
        editor.commit();
    }
}
