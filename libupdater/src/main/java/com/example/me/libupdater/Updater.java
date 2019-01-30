package com.example.me.libupdater;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.os.Build;

import java.io.File;
import java.util.Date;

import com.example.me.libupdater.UpdatesChecker.UpdatesInfoStruct;

public class Updater extends Fragment implements UpdatesCheckListener, InternetDataDownloadListener {
    private static final Integer REQUEST__WRITE_EXTERNAL_STORAGE__PERMISSION_TO_REQUEST_UPDATE = 21;
    private static final Integer REQUEST__REQUEST_INSTALL_PACKAGES__PERMISSION_TO_INSTALL_LAST_VERSION_APK = 22;
    private static final Integer REQUEST__INSTALL_PACKAGE__INTENT_TO_INSTALL_LAST_VERSION_APK = 23;

    /*Must be True to be able for work. To make it True - attach this fragment to your main activity.*/
    private boolean isAttached = false;

    @Override
    /*Attaching to calling activity. Essential for work.*/
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST__WRITE_EXTERNAL_STORAGE__PERMISSION_TO_REQUEST_UPDATE) {
            processREQUEST__WRITE_EXTERNAL_STORAGE__TO_REQUEST_UPDATE();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG","onActivityResult: "+requestCode+"|"+resultCode);
        if (requestCode == REQUEST__REQUEST_INSTALL_PACKAGES__PERMISSION_TO_INSTALL_LAST_VERSION_APK) {
            installApk(getApkStorePath());
        }
        if (requestCode == REQUEST__WRITE_EXTERNAL_STORAGE__PERMISSION_TO_REQUEST_UPDATE) {
            processREQUEST__WRITE_EXTERNAL_STORAGE__TO_REQUEST_UPDATE();
        }
        if (requestCode == REQUEST__INSTALL_PACKAGE__INTENT_TO_INSTALL_LAST_VERSION_APK) {
            Log.d("TAG","REQUEST__INSTALL_PACKAGE__INTENT_TO_INSTALL_LAST_VERSION_APK");
        }
    }
//---End of activity's overrides

    /*Check last version and suggest to update if needed.*/
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
                } else {
                    //TODO problems on host: current version > last version checked from host
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
        showNotification("HIHI","HIYA",new Intent(),p);
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

    /*Return path and file name to sore in External app's private directory (but user has access to it).*/
    private String getApkStorePath() {
        return getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/"+getAppLabel()+"_"+UpdatesInfoStruct.getLastVersionName()+".apk";
    }

    /*Returns true if app has WRITE_EXTERNAL_STORAGE permission and starts InternetDataDownloader*/
    private void downloadInternetData(String internetDataUrl, String internetDataPathToStore) {
        Log.d("TAG","internetDataPathToStore: "+internetDataPathToStore);
        if (hasPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            new InternetDataDownloader(internetDataUrl,internetDataPathToStore).execute(this);
        } else {
            Log.d("TAG","requestPermissions before");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST__WRITE_EXTERNAL_STORAGE__PERMISSION_TO_REQUEST_UPDATE);
        }
    }

    /*Install apk from given url.*/
    private void installApk(String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!tryToInstallApk(apkUrl)) {
                showDialogWithOkButton("APK файл последней версии приложения " + getAppLabel() + " загрузился, " +
                                "но приложение не имеет разрешения на установку этого apk. Сейчас вы будете перенаправлены, чтобы дать это разрешение.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                         //       dialog.dismiss(); //TODO is needed or not? seems not
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                Uri.parse("package:"+getActivity().getPackageName())),
                                        REQUEST__REQUEST_INSTALL_PACKAGES__PERMISSION_TO_INSTALL_LAST_VERSION_APK);
                            }
                        },
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                Uri.parse("package:"+getActivity().getPackageName())),
                                        REQUEST__REQUEST_INSTALL_PACKAGES__PERMISSION_TO_INSTALL_LAST_VERSION_APK);
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
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //minimum 14 API level
                File file = new File(apkUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW); //TODO ACTION_INSTALL_PACKAGE
                Uri contentUri = Uri.fromFile(file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                getActivity().startActivity(intent);
                return true;
            }
        }
        return false;
    }

    //TODO
    private boolean newTryToInstallApk(String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //>= 26 API level
            if (getActivity().getPackageManager().canRequestPackageInstalls()) {
                File file = new File(apkUrl);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                Uri contentUri = Uri.fromFile(new File(apkUrl));
                intent.setData(contentUri);
                intent.putExtra(Intent.EXTRA_RETURN_RESULT,true);
                getActivity().startActivityForResult(intent,REQUEST__INSTALL_PACKAGE__INTENT_TO_INSTALL_LAST_VERSION_APK);
                return true;
            }
        }
        return false;
    }

    /*Check if app has permissions.*/
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

    private void showDialogWithOkButton(final String message, final DialogInterface.OnClickListener okListener, final DialogInterface.OnCancelListener cancelListener) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(message)
                        .setPositiveButton("Ок", okListener)
                        .setOnCancelListener(cancelListener);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void processREQUEST__WRITE_EXTERNAL_STORAGE__TO_REQUEST_UPDATE() {
        if (isUpdateNeeded()) {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (hasPermissions(getActivity(),permission)) {
                downloadInternetData(UpdatesInfoStruct.getLastAppURL(),getApkStorePath()); //double permission check. Think it's ok.
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //user pressed only "DENY"
                        showDialogWithOkButton("Разрешение на доступ к файлам нужно для загрузки .apk-файла последней " +
                                        "версии приложения "+ getAppLabel(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("TAG", "shouldShowRequestPermissionRationale OK pressed");
                                        downloadInternetData(UpdatesInfoStruct.getLastAppURL(),getApkStorePath());
                                    }
                                },
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        Log.d("TAG", "shouldShowRequestPermissionRationale CANCELED");
                                        downloadInternetData(UpdatesInfoStruct.getLastAppURL(),getApkStorePath());
                                    }
                                });
                    } else {
                        //user pressed "Don't ask again" and "DENY" or "ALLOW", but last is excluded by first condition
                        showDialogWithOkButton("Приложение "+getAppLabel()+" требует срочного обновления. "+
                                        "Пожалуйста, дайте разрешение на доступ к файлам, оно нужно для загрузки последней версии. " +
                                        "Сейчас вы будете перенаправлены на экран настроек приложения, откуда можно дать это разрешение.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                        Uri.parse("package:"+getActivity().getPackageName())),
                                                REQUEST__WRITE_EXTERNAL_STORAGE__PERMISSION_TO_REQUEST_UPDATE);
                                    }
                                },
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                        Uri.parse("package:"+getActivity().getPackageName())),
                                                REQUEST__WRITE_EXTERNAL_STORAGE__PERMISSION_TO_REQUEST_UPDATE);
                                    }
                                });
                    }
                } else {
                    //We can not be here because lower Android versions do not allow to turn off permissions after installation
                }
            }
        }
    }

    private void initNotification() {

    }
    public void showNotification(String heading, String description, Intent intent,int progress){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        createChannel();
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity(),"channelID")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle(heading)
                .setContentText(description)
                .setAutoCancel(true)
                .setSound(null)
                .setProgress(100,progress,false)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void createChannel(){
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("channelID","name", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Description");
        notificationManager.createNotificationChannel(channel);
    }
}
