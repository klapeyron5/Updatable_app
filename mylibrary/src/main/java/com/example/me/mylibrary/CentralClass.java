package com.example.me.mylibrary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

public class CentralClass extends Fragment implements UpdatesCheckListener, InternetDataDownloadListener {
    private static final Integer REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE = 21;
    private static final Integer REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK = 22;

    private boolean isAttached = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    //    isAttached = true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
        return null;
    }

    private boolean clearCache() {
        File f = new File(Environment.getExternalStorageDirectory()+"/"+
                getResources().getString(R.string.lib_name)+"_"+getResources().getString(R.string.lib_version_name)+".apk");
        f.delete();

        return true;
    }

    public boolean checkUpdate() {
        if (isAttached) {
            UpdatesChecker updatesChecker = new UpdatesChecker();
            updatesChecker.execute(this);
            return true;
        } else
            return false;
    }

    public boolean suggestUpdate(UpdatesChecker updatesChecker) {
        if (isAttached) {
            int currentCodeVersion = getAppVersionCode();
            if (currentCodeVersion < updatesChecker.getLastCodeVersion()) {
                requestUpdate(updatesChecker);
            } else
                if (currentCodeVersion == updatesChecker.getLastCodeVersion()) {
                    //TODO update last time of updates check
                } else {
                    //TODO problems on host
                }
            return true;
        } else
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

    @Override
    public String getUpdatesInfoUrl() {
        if (isAttached)
            return getResources().getString(R.string.update_info_URL);
        else
            return null;
    }

    @Override
    public void onUpdatesChecked(UpdatesChecker updatesChecker) {
        Log.d("TAG","latest version: " + updatesChecker.getLastVersionName());
        suggestUpdate(updatesChecker);
    }

    @Override
    public void onUpdatesCouldNotCheck(UpdatesChecker updatesChecker) {
        Log.d("TAG","latest version: " + "can't check last updates");
    }

    @Override
    public String getInternetDataUrl() {
        return "";//updatesChecker.getLastAppURL();
    }

    @Override
    public String getInternetDataPathToStore() {
        return "";//Environment.getExternalStorageDirectory()+"/"+
                //getActivity().getResources().getString(R.string.app_name)+"_"+updatesChecker.getLastVersionName()+".apk";
    }

    @Override
    public void onInternetDataDownloaded(InternetDataDownloader internetDataDownloader) {
        //installApk(getInternetDataPathToStore());
    }

    @Override
    public void onInternetDataCouldNotDownload(InternetDataDownloader internetDataDownloader) {

    }

    @Override
    public void onInternetDataDownloadingProgressUpdate(Integer p) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE) {
            Log.d("TAG","LIBLIBLIB onRequestPermissionsResult");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG","LIBRARY onActivityResult, code: "+requestCode);
    }

    /**
     * Asks user to update app
     */
    private void requestUpdate(final UpdatesChecker updatesChecker) {
        if (updatesChecker.isValidUpdatesInfo()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setMessage("Доступно обновление приложения " + getAppLabel() + " до версии " +
                            updatesChecker.getLastVersionName() + " - желаете обновиться?")
                            .setCancelable(true)
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    if (!tryToDownloadInternetData()) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
                                     //   ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                     //           REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
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

    private int getAppVersionCode() {
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionCode;
    }

    /*Returns true if app has WRITE_EXTERNAL_STORAGE permission and starts InternetDataDownloader*/
    private boolean tryToDownloadInternetData() {
        if (hasPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            new InternetDataDownloader().execute(this);
            return true;
        }
        return false;
    }
}
