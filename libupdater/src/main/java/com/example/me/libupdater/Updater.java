package com.example.me.libupdater;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.io.File;

import com.example.me.libupdater.UpdatesChecker.UpdatesInfoStruct;

public class Updater extends Fragment implements UpdatesCheckListener, InternetDataDownloadListener {
    private static final Integer REQUEST__WRITE_EXTERNAL_STORAGE__TO_REQUEST_UPDATE = 21;
    private static final Integer REQUEST__REQUEST_INSTALL_PACKAGES__TO_INSTALL_LAST_VERSION_APK = 22;

    /*Must be True to be able for work. To make it True - attach this fragment to your main activity.*/
    private boolean isAttached = false;

    @Override
    /*Attaching to calling activity. Essential for work.*/
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
    }

    //TODO delete this override may be
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    /*Here permissions result will be caught (beside REQUEST_INSTALL_PACKAGES)*/
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST__WRITE_EXTERNAL_STORAGE__TO_REQUEST_UPDATE) {
            if (isUpdateNeeded()) {
                downloadInternetData(UpdatesInfoStruct.getLastAppURL(),getApkStorePath());
            }
        }
    }

    @Override
    /*Here REQUEST_INSTALL_PACKAGES will be caught.*/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST__REQUEST_INSTALL_PACKAGES__TO_INSTALL_LAST_VERSION_APK) {
            installApk(getApkStorePath());
        }
    }
//---End of activity's overrides

    public void checkSuggestUpdate() {
        if (isAttached) {
            checkUpdate();
        } else {
            //TODO
        }
    }

    /*Checks info about app updates. Results of checks will be returned in UpdatesCheckListener implementation.*/
    private void checkUpdate() {
        new UpdatesChecker(getString(R.string.update_info_URL)).execute(this);
    }

    /*Suggests user to update app.*/
    private void suggestUpdate() {
        Long currentCodeVersion = getAppVersionCode();
        if (currentCodeVersion != null) {
            if (currentCodeVersion < UpdatesInfoStruct.getLastCodeVersion()) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage("Доступно обновление приложения " + getAppLabel() + " до версии " +
                                UpdatesInfoStruct.getLastVersionName() + " - желаете обновиться?")
                                .setCancelable(true)
                                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        downloadInternetData(UpdatesInfoStruct.getLastAppURL(), getApkStorePath());
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

            } else {
                if (currentCodeVersion == UpdatesInfoStruct.getLastCodeVersion()) {
                    //TODO update last time of updates check (not needed cas UpdatesInfoStruct stores last timestamp)
                } else { //TODO current version > last version checked from host
                    //TODO problems on host
                }
            }
        } else {
            //TODO problems with PackageManager
        }
    }

    @Override
    public void onUpdatesChecked() {
        Log.d("TAG","latest version: " + UpdatesInfoStruct.getLastVersionName());
        suggestUpdate();
    }

    @Override
    public void onUpdatesCouldNotCheck(String error) {
        Log.d("TAG","latest version: " + "can't check last updates: " + error);
    }

    @Override
    public void onInternetDataDownloaded(String internetDataPathToStore) {
        Log.d("TAG","onInternetDataDownloaded");
        installApk(internetDataPathToStore);
    }

    @Override
    public void onInternetDataCouldNotDownload(String internetDataPathToStore) {
        Log.d("TAG","onInternetDataCouldNotDownload");
    }

    @Override
    public void onInternetDataDownloadingProgressUpdate(Integer p) {
        Log.d("TAG","onInternetDataDownloadingProgressUpdate: " + p);
    }

    private void clearCache() {

    }

    @Nullable
    private String getAppLabel() {
        PackageManager pm = getActivity().getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
            int stringId = ai.labelRes;
            if (stringId == 0) {
                return ai.nonLocalizedLabel.toString();
            } else {
                return getActivity().getString(stringId);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    /*Returns null if PackageManager.NameNotFoundException happened.*/
    private Long getAppVersionCode() {
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        Long returnValue = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            returnValue = Long.valueOf(pInfo.versionCode);
        } else {
            returnValue =  pInfo.getLongVersionCode();
        }
        return returnValue;
    }

    /*Checks if getAppVersionCode() not null and < last code version.*/
    private boolean isUpdateNeeded() {
        Long currentCodeVersion = getAppVersionCode();
        if ((currentCodeVersion != null)&&(currentCodeVersion < UpdatesInfoStruct.getLastCodeVersion()))
            return true;
        return false;
    }

    private String getApkStorePath() {
        return Environment.getExternalStorageDirectory()+
                "/"+getAppLabel()+"_"+UpdatesInfoStruct.getLastVersionName()+".apk";
    }

    /*Returns true if app has WRITE_EXTERNAL_STORAGE permission and starts InternetDataDownloader*/
    private void downloadInternetData(String internetDataUrl, String internetDataPathToStore) {
        Log.d("TAG","internetDataPathToStore: "+internetDataPathToStore);
        if (hasPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            new InternetDataDownloader(internetDataUrl,internetDataPathToStore).execute(this);
        } else { //TODO write permission rationale
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST__WRITE_EXTERNAL_STORAGE__TO_REQUEST_UPDATE);
        }
    }

    private void installApk(String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!tryToInstallApk(apkUrl)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("APK файл последней версии приложения " + getAppLabel() + " загрузился, " +
                                "но приложение не имеет разрешения на установку этого apk. Сейчас вы будете перенаправлены, чтобы дать это разрешение.")
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                        Uri.parse("package:"+getActivity().getPackageName())),
                                                REQUEST__REQUEST_INSTALL_PACKAGES__TO_INSTALL_LAST_VERSION_APK);

                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //minimum 14 API level
                tryToInstallApk(apkUrl);
            }
        }
    }

    /*Use this only from installApk() method.*/
    private boolean tryToInstallApk(String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //>= 26 API level
            if (getActivity().getPackageManager().canRequestPackageInstalls()) {
                File file = new File(apkUrl);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Uri contentUri = FileProvider.getUriForFile(getActivity(), "com.example.me.fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getActivity().startActivity(intent);
                return true;
            }
        } else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //minimum 14 API level
            File file = new File(apkUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri contentUri = Uri.fromFile(file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            getActivity().startActivity(intent);
            return true;
        }
        return false;
    }

    /*Check if app has permissions*/
    private boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
