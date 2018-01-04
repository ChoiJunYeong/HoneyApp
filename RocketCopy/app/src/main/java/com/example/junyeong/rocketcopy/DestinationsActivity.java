package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.JsonReader;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.json.JSONObject;

import java.io.File;
import java.util.zip.Inflater;

public class DestinationsActivity extends AppCompatActivity {
    RelativeLayout currentLayout;
    String destination,address;
    String filepath = Environment.getExternalStorageDirectory().toString() + "/app/rocket";
    File myDir = new File(filepath);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);
        //floating button setting
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to add add work
                RelativeLayout rootLayout =(RelativeLayout) findViewById(R.id.destinations);
                int layoutNumber = rootLayout.getChildCount();
                //define new layout
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                RelativeLayout newlayout = (RelativeLayout)inflater.inflate(R.layout.destination_item,null);
                //new layout setting
                TextView textView1 = (TextView) newlayout.getChildAt(2);
                textView1.setText("Destination" + (layoutNumber+1));
                TextView textView2 = (TextView) newlayout.getChildAt(3);
                textView2.setText("Address" + (layoutNumber+1));
                rootLayout.addView(newlayout);
                //layout move to below
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newlayout.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.addRule(RelativeLayout.BELOW,rootLayout.getChildAt(layoutNumber-1).getId());
                newlayout.setLayoutParams(params);
                setNewViewID(newlayout);

                newlayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        redefineDestination((RelativeLayout)view);
                    }
                });

            }
        });
        //make buttons
        RelativeLayout relay1 = (RelativeLayout) findViewById(R.id.relay1);
        relay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redefineDestination((RelativeLayout)view);
            }
        });



        //move to under status-bar
        relay1.setPadding(0,getStatusBarSize(),0,0);

    }
    public void setNewViewID(RelativeLayout layout){
        int id= layout.getId();
        int oldid = 0;
        while(findViewById(id)!=null)
            id++;
        layout.setId(id);
        int childNum=layout.getChildCount();
        for(int i=0;i<childNum;i++){
            //set new id
            View view = layout.getChildAt(i);
            while(findViewById(id)!=null)
                id++;
            view.setId(id);
            //position redefine
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            switch (i){
                case 1:
                    params.addRule(RelativeLayout.END_OF,oldid);
                case 2:
                    params.addRule(RelativeLayout.END_OF,oldid);
                case 3:
                    params.addRule(RelativeLayout.BELOW,oldid);
            }
            view.setLayoutParams(params);
            oldid=id;
        }
    }

    public void redefineDestination(RelativeLayout relativeLayout){
        currentLayout = relativeLayout;
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
            }

        });
        alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }

        });
        alert.show();
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

    public void goHome(View view){
        finish();
    }
}
