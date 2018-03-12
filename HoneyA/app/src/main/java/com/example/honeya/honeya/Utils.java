package com.example.honeya.honeya;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
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
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by junyeong on 18. 1. 5.
 */

public class Utils {

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);


    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }


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
    public static void detectNote(Bitmap image,String filePath){
        Mat BGRImage = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC3);
        Mat grayImage = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);
        org.opencv.android.Utils.bitmapToMat(image,BGRImage);
        Mat copyImage = BGRImage;
        Mat copyImage2 = BGRImage;
        Imgproc.cvtColor(BGRImage,grayImage,Imgproc.COLOR_BGR2GRAY);
        FeatureDetector sift = FeatureDetector.create(FeatureDetector.FAST);
        MatOfKeyPoint kp = new MatOfKeyPoint();
        sift.detect(grayImage,kp);
        Features2d.drawKeypoints(grayImage,kp,copyImage);
        Imgcodecs.imwrite(filePath+"/no_flag.jpg",copyImage);
        Features2d.drawKeypoints(grayImage,kp,copyImage2, Scalar.all(-1),Features2d.DRAW_RICH_KEYPOINTS);
        Imgcodecs.imwrite(filePath+"/sift_keypoints.jpg",copyImage2);
    }
    public static void matchNote(Bitmap image){
        Mat Img1 = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC3);
        Mat Img2 = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC3);
        org.opencv.android.Utils.bitmapToMat(image,Img1);
        FeatureDetector sift = FeatureDetector.create(FeatureDetector.FAST);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);

        MatOfKeyPoint kp1 = new MatOfKeyPoint();
        sift.detect(Img1,kp1);
        Mat des1 = new Mat();
        descriptorExtractor.compute(Img1,kp1,des1);

        MatOfKeyPoint kp2 = new MatOfKeyPoint();
        sift.detect(Img2,kp2);
        Mat des2 = new Mat();
        descriptorExtractor.compute(Img2,kp2,des2);
        BFMatcher bfMatcher = new BFMatcher();
        List<MatOfDMatch> matches = new List<MatOfDMatch>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<MatOfDMatch> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(MatOfDMatch matOfDMatch) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends MatOfDMatch> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, @NonNull Collection<? extends MatOfDMatch> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public MatOfDMatch get(int index) {
                return null;
            }

            @Override
            public MatOfDMatch set(int index, MatOfDMatch element) {
                return null;
            }

            @Override
            public void add(int index, MatOfDMatch element) {

            }

            @Override
            public MatOfDMatch remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<MatOfDMatch> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<MatOfDMatch> listIterator(int index) {
                return null;
            }

            @NonNull
            @Override
            public List<MatOfDMatch> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
        bfMatcher.knnMatch(des1,des2,matches,2);
        for(MatOfDMatch match : matches){

        }

    }
}
