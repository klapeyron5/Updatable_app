package com.example.me.updatable_app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class InternetDownloader extends AsyncTask<String, Integer, Integer>  {

    @Override
    protected Integer doInBackground(String... params) { //params[0] - url to download, params[1] - path to store
        Log.d("TAG","InternetDownloader 1");
        return downloadAPK(params);
    }

    protected Integer downloadAPK(String... sUrl) {
        Log.d("TAG","InternetDownloader downloadAPK 1");
        try {
            URL url = new URL(sUrl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(sUrl[1]);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
            Log.d("TAG","InternetDownloader downloadAPK 8");

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG","InternetDownloader downloadAPK exception");
            return 0;
        }
        return 1;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    /**
     * Begins the installation of downloaded APK. User should confirm.
     * @param result
     */
    @Override
    protected void onPostExecute(Integer result) {
        if (result == 1) {
            Log.d("TAG","InternetDownloader onPostExecute 1");
           // this.mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));//, Uri.fromFile(new File(pathToStoreApk))));


            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
          //  Log.d("TAG","Path to apk 1: "+Uri.fromFile(new File(pathToStoreApk)));
         //   Log.d("TAG","Path to apk 2: "+FileProvider.getUriForFile(this.mainActivity, "com.example.me.fileprovider", new File(pathToStoreApk)));
          //  intent.setDataAndType(FileProvider.getUriForFile(this.mainActivity, "com.example.me.fileprovider",
                    //new File(pathToStoreApk)), "application/vnd.android.package-archive");
         //   intent.setDataAndType(Uri.fromFile(new File(pathToStoreApk)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

          //  this.mainActivity.startActivity(intent);

           /* Intent i = new Intent();
            i.setAction(Intent.ACTION_INSTALL_PACKAGE);
            i.setDataAndType(Uri.fromFile(new File(pathToStoreApk)), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.mainActivity.startActivity(i);*/
        }
    }
}
