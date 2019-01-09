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

public class Updater extends Fragment implements UpdatesCheckListener, InternetDataDownloadListener {
    private static final Integer REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE = 21;
    private static final Integer REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK = 22;

    private boolean isAttached = false;

    private String costylPrototypeString;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("TAG","onCreateView");
    //    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
    /*    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //>= 26 API level
            if (!getActivity().getPackageManager().canRequestPackageInstalls()) {
                Log.d("TAG","LIBLIBLIB canNotRequestPackageInstalls");
                startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:"+getActivity().getPackageName())),
                        REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK);
            }
        }*/
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE) {
            Log.d("TAG","LIBLIBLIB onRequestPermissionsResult");
            checkUpdate();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG","LIBLIBLIB onActivityResult, code: "+requestCode);
        if (requestCode == REQUEST_REQUEST_INSTALL_PACKAGES_PERMISSION_TO_INSTALL_LAST_VERSION_APK) {
            tryToInstallApk(costylPrototypeString);
        }
    }

    private void checkUpdate() {
        new UpdatesChecker(getString(R.string.update_info_URL)).execute(this);
    }

    private void suggestUpdate(UpdatesChecker updatesChecker) {
        int currentCodeVersion = getAppVersionCode();
        if (currentCodeVersion < updatesChecker.getLastCodeVersion()) {
            requestUpdate(updatesChecker);
        } else
        if (currentCodeVersion == updatesChecker.getLastCodeVersion()) {
            //TODO update last time of updates check
        } else {
            //TODO problems on host
        }
    }

    public void checkSuggestUpdate() {
        if (isAttached) {
            checkUpdate();
        } else {

        }
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
    public void onInternetDataDownloaded(InternetDataDownloader internetDataDownloader) {
        Log.d("TAG","onInternetDataDownloaded");
        installApk(internetDataDownloader.getInternetDataPathToStore());
    }

    @Override
    public void onInternetDataCouldNotDownload(InternetDataDownloader internetDataDownloader) {
        Log.d("TAG","onInternetDataCouldNotDownload");
    }

    @Override
    public void onInternetDataDownloadingProgressUpdate(Integer p) {
        Log.d("TAG","onInternetDataDownloadingProgressUpdate: " + p);
    }

    private void clearCache() {

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
                                    costylPrototypeString = Environment.getExternalStorageDirectory()+
                                            "/"+getAppLabel()+"_"+updatesChecker.getLastVersionName()+".apk";
                                    downloadInternetData(updatesChecker.getLastAppURL(),
                                            Environment.getExternalStorageDirectory()+
                                                    "/"+getAppLabel()+"_"+updatesChecker.getLastVersionName()+".apk");
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
    private void downloadInternetData(String internetDataUrl, String internetDataPathToStore) {
        Log.d("TAG","internetDataPathToStore: "+internetDataPathToStore);
        if (hasPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            new InternetDataDownloader(internetDataUrl,internetDataPathToStore).execute(this);
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_TO_REQUEST_UPDATE);
        }
    }

    private void installApk(String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!tryToInstallApk(apkUrl)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("APK файл последней версии приложения " + getResources().getString(R.string.app_name) + " загрузился, " +
                                "но приложение не имеет разрешения на установку этого apk. Сейчас вы будете перенаправлены, чтобы дать это разрешение.")
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                        Uri.parse("package:"+getActivity().getPackageName())),
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
