package com.example.me.updatable_app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity implements UpdatesCheckListener, InternetDataDownloadListener {
    final MainActivity mainContext = this;
    private UpdatesChecker updatesChecker;

    private static final Integer REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE = 21;
    private static final Integer REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTextOnTextView(1,"current version: "+getResources().getString(R.string.version_name));

        Log.d("TAG", "Install---->>>> "+getPackageManager().canRequestPackageInstalls());

        deleteDownloadedApk();

        updatesChecker = new UpdatesChecker();
        updatesChecker.execute(mainContext);
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

    @Override
    public String getInternetDataUrl() {
        return updatesChecker.getLastAppURL();
    }

    @Override
    public String getInternetDataPathToStore() {
        return Environment.getExternalStorageDirectory()+"/"+
                getResources().getString(R.string.app_name)+"_"+updatesChecker.getLastVersionName()+".apk";
    }

    @Override
    public void onInternetDataDownloaded(InternetDataDownloader internetDataDownloader) {
        installApk(getInternetDataPathToStore());
    }

    @Override
    public void onInternetDataCouldNotDownload(InternetDataDownloader internetDataDownloader) {

    }

    @Override
    public void onInternetDataDownloadingProgressUpdate(Integer p) {

    }

    /**
     * Asks user to update app
     */
    private void requestUpdate(final UpdatesChecker updatesChecker) {
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
                                    dialog.dismiss();
                                    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                    if (hasPermissions(mainContext,PERMISSIONS))
                                        new InternetDataDownloader().execute(mainContext);
                                    else {
                                        ActivityCompat.requestPermissions(mainContext, PERMISSIONS, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
                                    }
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

    private void installApk(String apkUrl) {
        Log.d("TAG","MainActivity installApk 1");
        File file = new File(apkUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d("TAG","MainActivity installApk 2");
            if (getPackageManager().canRequestPackageInstalls()) {
                Log.d("TAG","MainActivity installApk 3");
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Uri contentUri = FileProvider.getUriForFile(mainContext, "com.example.me.fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mainContext.startActivity(intent);
            } else {
                Log.d("TAG","MainActivity installApk 4");
                startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:com.example.me.updatable_app")),
                        REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK);
            }
        } else {
         //   Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
         //   intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        }
    }

    /*Check if app has permissions*/
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("TAG","onRequestPermissionsResult requestCode: "+requestCode);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (hasPermissions(mainContext,PERMISSIONS)) {
                requestUpdate(updatesChecker);
            }
        }
    }

    /*Only ACTION_INSTALL_PACKAGE permission goes here*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK) {
            installApk(getInternetDataPathToStore());
        }
    }
}
