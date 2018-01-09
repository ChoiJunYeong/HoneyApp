package com.example.junyeong.rocketcopy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by junyeong on 18. 1. 7.
 */

public class FolderItemView extends LinearLayout{
        TextView folderName,fileNum;
        ImageView folderType;
        Utils utils = new Utils();
        public FolderItemView(Context context){
            super(context);
            init(context);
        }
        public FolderItemView(Context context, AttributeSet attrs){
            super(context,attrs);
            init(context);
        }
        public void init(Context context){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.folder_item,this,true);
            folderName = (TextView) findViewById(R.id.folder_name);
            fileNum = (TextView) findViewById(R.id.file_num);
            folderType = (ImageView) findViewById(R.id.folder_icon);

        }
        public void setFolderName(String text){folderName.setText(text);}
        public void setFileNum(String text){fileNum.setText(text);}
        public void setFolderType(int type){
            if(type==utils.FOLDER){
                folderType.setImageResource(R.drawable.ic_folder_24dp);
            }
            else if(type==utils.SCHEDULE){
                folderType.setImageResource(R.drawable.ic_dashboard_black_24dp);
            }
        }
}
