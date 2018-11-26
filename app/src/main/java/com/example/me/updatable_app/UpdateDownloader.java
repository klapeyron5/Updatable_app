package com.example.me.updatable_app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateDownloader extends AsyncTask<MainActivity, Void, Integer>  {
    private MainActivity mainActivity = null;
    private String pathToStoreApk = "";

    @Override
    protected Integer doInBackground(MainActivity... mainActivities) {
        Log.d("TAG","UpdateDownloader 1");
        this.mainActivity = mainActivities[0];
        pathToStoreApk = "/sdcard/Updatable_app_"+UpdateChecker.getLastVersionName()+".apk";
        return downloadAPK(UpdateChecker.getLastAppURL());
    }

    protected Integer downloadAPK(String... sUrl) {
        Log.d("TAG","UpdateDownloader downloadAPK 1");
        try {
            URL url = new URL(sUrl[0]);
            Log.d("TAG","UpdateDownloader downloadAPK 2");
            URLConnection connection = url.openConnection();
            Log.d("TAG","UpdateDownloader downloadAPK 3");
            connection.connect();
            Log.d("TAG","UpdateDownloader downloadAPK 4");

            int fileLength = connection.getContentLength();
            Log.d("TAG","UpdateDownloader downloadAPK 5");

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            Log.d("TAG","UpdateDownloader downloadAPK 6");
            OutputStream output = new FileOutputStream(pathToStoreApk);
            Log.d("TAG","UpdateDownloader downloadAPK 7");

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
            Log.d("TAG","UpdateDownloader downloadAPK 8");

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG","UpdateDownloader downloadAPK exception");
            return 0;
        }
        return 1;
    }

    public void publishProgress(int progress) {
        mainActivity.setTextOnTextView(3, "downloading apk: " + progress + "%"); //TODO make this in notifications
    }

    /**
     * Begins the installation of downloaded APK. User should confirm.
     * @param result
     */
    @Override
    protected void onPostExecute(Integer result) {
        if (result == 1) {
            Log.d("TAG","UpdateDownloader onPostExecute 1");
           // this.mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));//, Uri.fromFile(new File(pathToStoreApk))));


            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            Log.d("TAG","Path to apk 1: "+Uri.fromFile(new File(pathToStoreApk)));
            Log.d("TAG","Path to apk 2: "+FileProvider.getUriForFile(this.mainActivity, "com.example.me.fileprovider", new File(pathToStoreApk)));
          //  intent.setDataAndType(FileProvider.getUriForFile(this.mainActivity, "com.example.me.fileprovider",
                    //new File(pathToStoreApk)), "application/vnd.android.package-archive");
            intent.setDataAndType(Uri.fromFile(new File(pathToStoreApk)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            this.mainActivity.startActivity(intent);

           /* Intent i = new Intent();
            i.setAction(Intent.ACTION_INSTALL_PACKAGE);
            i.setDataAndType(Uri.fromFile(new File(pathToStoreApk)), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.mainActivity.startActivity(i);*/
        }
    }
}
