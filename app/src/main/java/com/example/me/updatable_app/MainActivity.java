package com.example.me.updatable_app;

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
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity implements UpdatesCheckListener, InternetDataDownloadListener {
    private final MainActivity mainContext = this;
    private UpdatesChecker updatesChecker;

    private static final Integer REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE = 21;
    private static final Integer REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTextOnTextView(1,"current version: "+getResources().getString(R.string.version_name));

        deleteDownloadedApk();

        updatesChecker = new UpdatesChecker();
        updatesChecker.execute(mainContext);
    }

    @Override
    public String getUpdatesInfoUrl() {
        return getResources().getString(R.string.update_info_URL);
    }

    @Override
    public void onUpdatesChecked(UpdatesChecker updatesChecker) {
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
                                    if (!tryToDownloadInternetData(mainContext)) {
                                        ActivityCompat.requestPermissions(mainContext,
                                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!tryToInstallApk(apkUrl)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mainContext);
                        builder.setMessage("APK файл последней версии приложения " + getResources().getString(R.string.app_name) + " загрузился, " +
                                "но приложение не имеет разрешения на установку этого apk. Сейчас вы будете перенаправлены, чтобы дать это разрешение.")
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                        Uri.parse("package:com.example.me.updatable_app")),
                                                REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK);

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        } else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                tryToInstallApk(apkUrl);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            tryToDownloadInternetData(mainContext);
        }
    }

    /*Only ACTION_INSTALL_PACKAGE/ACTION_MANAGE_UNKNOWN_APP_SOURCES permission goes here*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK) {
            tryToInstallApk(getInternetDataPathToStore());
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

    /*Returns true if app has WRITE_EXTERNAL_STORAGE permission and starts InternetDataDownloader*/
    private boolean tryToDownloadInternetData(InternetDataDownloadListener internetDataDownloadListener) {
        if (hasPermissions(mainContext,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            new InternetDataDownloader().execute(internetDataDownloadListener);
            return true;
        }
        return false;
    }

    private boolean tryToInstallApk(String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //>= 26 API level
            if (getPackageManager().canRequestPackageInstalls()) {
                File file = new File(apkUrl);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Uri contentUri = FileProvider.getUriForFile(mainContext, "com.example.me.fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mainContext.startActivity(intent);
                return true;
            }
        } else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //minimum 14 API level
                File file = new File(apkUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri contentUri = Uri.fromFile(file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                mainContext.startActivity(intent);
                return true;
            }
        return false;
    }

    /**
     * Removes apk-file, if it called Updatable_app_<current version name>.apk
     */
    public void deleteDownloadedApk() {
        File f = new File(Environment.getExternalStorageDirectory()+"/"+
                getResources().getString(R.string.app_name)+"_"+getResources().getString(R.string.version_name)+".apk");
        f.delete();
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
}
