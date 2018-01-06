package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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

/**
 * Created by junyeong on 18. 1. 5.
 */

public class Utils {
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

}
