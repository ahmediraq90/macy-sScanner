package com.scnner.macys.scanner;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;



import com.scnner.macys.scanner.Services.ScanningService;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ahmed on 4/12/2016.
 */

public class MainActivity extends AppCompatActivity {

    private static final SimpleDateFormat DATE = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private LinearLayout view;
    private Intent openServiceIntent;
    private ProgressBar scanningBar;
    private boolean shareBtnState;
    private StringBuilder shareDataBuild;
    private TextView dataResultes;
    private AlertDialog serviceStatusAlert;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    long startTime;
    long elapsedTime = 0L;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getStringExtra("action");
            switch (action) {
                case "stop":
                    break;
                case "progress":
                    int level = intent.getIntExtra("progress", 0);
                    scanningBar.setProgress(level + 1);
                    if (elapsedTime > 10) {
                        mBuilder.setProgress(intent.getIntExtra("progressBarValue", 100), level + 1, false);
                        mNotifyManager.notify(1, mBuilder.build());
                        startTime = System.currentTimeMillis();
                        elapsedTime = 0;
                    } else {
                        elapsedTime = new Date().getTime() - startTime;
                    }
                    dataResultes.setText("Scanning: " + (level + 1) + "/" + scanningBar.getMax());
                    break;
                case "progressInit":
                    view.removeAllViews();
                    cleanUpAllResources();
                    mBuilder.setProgress(intent.getIntExtra("progressBarValue", 100), 0, false);
                    mNotifyManager.notify(1, mBuilder.build());
                    scanningBar.setMax(intent.getIntExtra("progressBarValue", 100));
                    shareBtnState = false;
                    invalidateOptionsMenu();
                    break;
                case "done":
                    dataResultes.setText("Please wait we are returning all the results");
                    break;
                default:
                    dataResultes.setText("");
                    ArrayList<File> tenBiggest = (ArrayList<File>) intent.getSerializableExtra("tenBiggest");
                    long averageSize = intent.getLongExtra("average", 0);
                    ArrayList<File> mostRecent = (ArrayList<File>) intent.getSerializableExtra("mostRecent");
                    TextView textView = new TextView(context);
                    textView.setText("                                  Ten Biggest Files");
                    shareDataBuild.append("                                  Ten Biggest Files \n");
                    textView.setTextColor(Color.BLACK);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    view.addView(textView);
                    for (int i = 0; i < tenBiggest.size(); i++) {
                        textView = new TextView(context);
                        textView.setTextColor(Color.GRAY);
                        textView.setTypeface(Typeface.DEFAULT);
                        textView.setText(tenBiggest.get(i).getName() + "   " + getSizeByKb(tenBiggest.get(i).length()) + "KB");
                        shareDataBuild.append(tenBiggest.get(i).getName()).append("    ").append(getSizeByKb(tenBiggest.get(i).length())).append("KB \n");
                        textView.setPadding(5, 5, 5, 5);
                        view.addView(textView);
                    }
                    textView = new TextView(context);
                    textView.setText("                                  Average File Size");
                    shareDataBuild.append("                                  Average File Size \n");
                    textView.setTextColor(Color.BLACK);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    view.addView(textView);
                    textView = new TextView(context);
                    textView.setTextColor(Color.GRAY);
                    textView.setTypeface(Typeface.DEFAULT);
                    textView.setText(getSizeByKb(averageSize) + "KB");
                    shareDataBuild.append(getSizeByKb(averageSize)).append("KB \n");
                    textView.setPadding(5, 5, 5, 5);
                    view.addView(textView);
                    textView = new TextView(context);
                    textView.setText("                                  Frequent Extensions");
                    shareDataBuild.append("                                  Average File Size \n");
                    textView.setTextColor(Color.BLACK);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    view.addView(textView);
                    for (int i = 0; i < mostRecent.size(); i++) {
                        textView = new TextView(context);
                        textView.setTextColor(Color.GRAY);
                        textView.setTypeface(Typeface.DEFAULT);
                        textView.setText(FilenameUtils.getExtension(mostRecent.get(i).getName()) + " modify date : " + DATE.format(mostRecent.get(i).lastModified()));
                        shareDataBuild.append(FilenameUtils.getExtension(mostRecent.get(i).getName())).append("    ").append(" modify date : ").append(DATE.format(mostRecent.get(i).lastModified())).append(" \n");
                        textView.setPadding(5, 5, 5, 5);
                        view.addView(textView);
                    }
                    shareBtnState = true;
                    invalidateOptionsMenu();
                    break;
            }


        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (LinearLayout) findViewById(R.id.resltView);
        shareDataBuild = new StringBuilder();
        view.removeAllViews();
        dataResultes = (TextView) findViewById(R.id.textView2);
        scanningBar = (ProgressBar) findViewById(R.id.scanningProgress);
        registerReceiver(broadcastReceiver, new IntentFilter("com.macys.scanner.broadcast"));
        openServiceIntent = new Intent(MainActivity.this, ScanningService.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_NO_CREATE);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(MainActivity.this);
        mBuilder.setContentIntent(contentIntent)
                .setContentTitle("Scanning")
                .setContentText("Scanning in progress")
                .setSmallIcon(R.drawable.filescanner);

    }

    private void cleanUpAllResources() {
        scanningBar.setProgress(0);
        dataResultes.setText("");
        shareBtnState = false;
        invalidateOptionsMenu();
        shareDataBuild = new StringBuilder();
        mNotifyManager.cancel(1);
    }

    private long getSizeByKb(long size) {
        return size / 1024;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menu.getItem(0).setVisible(shareBtnState);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "File Scanner Details");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareDataBuild.toString());
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        stopService(openServiceIntent);

    }

    public void stopScanning(View view) {
        if (isMyServiceRunning(ScanningService.class)) {
            stopService(openServiceIntent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Scanning is already Stopped!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            serviceStatusAlert.hide();
                        }
                    });
            serviceStatusAlert = builder.create();
            serviceStatusAlert.show();
        }
    }

    public void startScanning(View view) {
        if (!isMyServiceRunning(ScanningService.class)) {
            startService(openServiceIntent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Scanning is already started!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            serviceStatusAlert.hide();
                        }
                    });
            serviceStatusAlert = builder.create();
            serviceStatusAlert.show();
        }

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}