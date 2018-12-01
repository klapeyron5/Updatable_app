package com.example.me.updatable_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity implements UpdatesCheckListener {
    final MainActivity mainContext = this;
    private static final int as = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTextOnTextView(1,"current version: "+getResources().getString(R.string.version_name));

        Log.d("TAG", "Install---->>>> "+getPackageManager().canRequestPackageInstalls());

        deleteDownloadedApk();

        new UpdatesChecker().execute(mainContext);
    }

    public void setTextOnTextView(final int textViewNumber, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = null;
                switch(textViewNumber) {
                    case 1:
                        textView = findViewById(R.id.textView1CurrentVersion);
                        break;
                    case 2:
                        textView = findViewById(R.id.textView2LatestVersion);
                        break;
                    case 3:
                        textView = findViewById(R.id.textView3DownloadingProgress);
                        break;
                }
                if (textView != null)
                    textView.setText(text);
            }
        });
    }

    /**
     * Removes apk-file, if it called Updatable_app_<current version name>.apk
     */
    public void deleteDownloadedApk() {
        String pathToStoreApk = "/sdcard/Updatable_app_"+getResources().getString(R.string.version_name)+".apk";
        File f = new File(pathToStoreApk);
        f.delete();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG","onActivityResult requestCode: "+requestCode);
        if (requestCode == 1) {
        }
    }

    @Override
    public String getUpdatesInfoUrl() {
        return getResources().getString(R.string.update_info_URL);
    }

    @Override
    public void onUpdatesChecked(UpdatesChecker updatesChecker) {
        Log.d("TAG","MainActivity onUpdatesChecked");
        setTextOnTextView(2,"latest version: "+ updatesChecker.getLastVersionName()); //TODO logs
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int currentCodeVersion = pInfo.versionCode;
        if (currentCodeVersion < updatesChecker.getLastCodeVersion()) {
            requestUpdate(updatesChecker);
        } else
            if (currentCodeVersion == updatesChecker.getLastCodeVersion()) {
                //TODO update last time of updates check
            } else {
                //TODO problems on host
            }
    }

    @Override
    public void onUpdatesCouldNotCheck(UpdatesChecker updatesChecker) {
        Log.d("TAG","MainActivity onUpdatesCouldNotCheck");
        setTextOnTextView(2,"latest version: " + "can't check last updates"); //TODO logs
    }

    /**
     * Asks user to update app
     */
    public void requestUpdate(final UpdatesChecker updatesChecker) {
        if (updatesChecker.isValidUpdatesInfo()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Доступно обновление приложения " + getResources().getString(R.string.app_name) + " до версии " +
                            updatesChecker.getLastVersionName() + " - желаете обновиться?")
                            .setCancelable(true)
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String urlToDownloadApk = updatesChecker.getLastAppURL();
                                    String pathToStoreApk = Environment.getExternalStorageDirectory() + "/Updatable_app_" + updatesChecker.getLastVersionName() + ".apk";
                                    new InternetDownloader().execute(urlToDownloadApk,pathToStoreApk);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }
}
