package com.scnner.macys.scanner.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.scnner.macys.scanner.compartors.DateComparator;
import com.scnner.macys.scanner.compartors.SizeComparatore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by ahmed on 4/12/2016.
 */
public class ScanningService extends IntentService {
    private LinkedList<File> fileList;
    private static final File ROOT = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath());
    private static Intent progressIntent;
    private static boolean serviceStatus;
    private static long totalFilesLenght;
    private static long averageFileLengh;
    private ArrayList<SizeComparatore> tenBiggestFiles;
    private ArrayList<DateComparator> mostRecent;

    public ScanningService() {
        super("Files Scanning");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tenBiggestFiles = new ArrayList<>();
        mostRecent = new ArrayList<>();
        fileList = new LinkedList<>();
        progressIntent = new Intent();
        totalFilesLenght = 0;
        averageFileLengh = 0;
        serviceStatus = true;
        progressIntent.putExtra("action", "progressInit");
        progressIntent.setAction("com.macys.scanner.broadcast");
        fileList = (LinkedList<File>) FileUtils.listFiles(ROOT, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        progressIntent.putExtra("progressBarValue", fileList.size());
        sendBroadcast(progressIntent);
        progressIntent.putExtra("action", "progress");
        setTotalFilesLenght();
        if (serviceStatus) {
            averageFileLengh = this.getAverageSize();
            Collections.sort(this.tenBiggestFiles);
            Collections.sort(this.mostRecent);
            progressIntent.putExtra("average", averageFileLengh);
            progressIntent.putExtra("mostRecent", this.getMostRecentExtention());
            progressIntent.putExtra("tenBiggest", this.getTenBiggestFiles());
            progressIntent.putExtra("action", "publish");
            sendBroadcast(progressIntent);
        }
    }

    private void setTotalFilesLenght() {
        for (int i = 0; i < fileList.size(); i++) {
            if (serviceStatus) {
                totalFilesLenght += fileList.get(i).length();
                tenBiggestFiles.add(new SizeComparatore(fileList.get(i)));
                mostRecent.add(new DateComparator(fileList.get(i)));
                progressIntent.putExtra("progress", i);
                sendBroadcast(progressIntent);
            } else {
                break;
            }
        }
        if(serviceStatus){
            progressIntent.putExtra("action", "done");
            sendBroadcast(progressIntent);
        }
    }

    private Long getAverageSize() {
        return totalFilesLenght / fileList.size();
    }

    private ArrayList<File> getTenBiggestFiles() {
       ArrayList<File> tenBiggestFiles  = new ArrayList<>();
        try {
            for (int i = 0; i < 10; i++) {
                tenBiggestFiles.add(this.tenBiggestFiles.get(i).getFile());
            }


        } catch (Exception ignored) {

        }
        return tenBiggestFiles;
    }
    private ArrayList<File> getMostRecentExtention() {
        ArrayList<File> mostRecent  = new ArrayList<>();
       try {
            for (int i = 0; i < 5; i++) {
               mostRecent.add(this.mostRecent.get(i).getFile());
           }

       } catch (Exception ignored) {

        }
        return mostRecent;
   }
    @Override
    public void onDestroy() {
        serviceStatus = false;
        progressIntent.putExtra("action", "stop");
        sendBroadcast(progressIntent);
    }
}
