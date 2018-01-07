package com.example.junyeong.rocketcopy;

/**
 * Created by junyeong on 18. 1. 7.
 */

public class FolderItem {
    String filenum, foldername;
    int foldertype;

    public FolderItem() {
        return;
    }

    public String getFilenum() {
        return filenum;
    }

    public String getFoldername() {
        return foldername;
    }

    public int getFoldertype() {
        return foldertype;
    }

    public void setFilenum(String filenum) {
        this.filenum = filenum + " files";
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    public void setFoldertype(int foldertype) {
        this.foldertype = foldertype;
    }

    ;
}
