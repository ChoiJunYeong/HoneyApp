package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.JsonReader;
import android.util.Log;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Inflater;

public class DestinationsActivity extends AppCompatActivity {
    RelativeLayout currentLayout;
    String destination,address;
    String filepath = Environment.getExternalStorageDirectory().toString() + "/honeyA";
    File myDir = new File(filepath);
    DialogInterface dialogInterface;
    Utils utils = new Utils();

    GoogleSignInClient googleSignInClient;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static final int REQUEST_CODE_SIGN_IN = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);
        setDestinationLayout();
        sharedPreferences = getSharedPreferences("honeyA", MODE_PRIVATE);
        editor = sharedPreferences.edit();


    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    //save login state id_token
                    if (result.isSuccess()) {
                        GoogleSignInAccount acct = result.getSignInAccount();
                        String id_token = acct.getIdToken();
                        editor.putString("GD_LOGIN", id_token);
                        editor.commit();
                    }

                }
                break;

        }
    }
    public void setDestinationLayout(){
        RelativeLayout rootLayout = findViewById(R.id.destinations);
        //move to under status-bar
        rootLayout.setPadding(0,getStatusBarSize(),0,0);
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
                signIn();
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
    // Build a Google SignIn client.
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }
    //return status-bar size for getting layout offset
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

    //sign in googledrive
    private void signIn() {
        googleSignInClient = buildGoogleSignInClient();
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

}
