package com.example.me.updatable_app;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateDownloader extends AsyncTask<MainActivity, Void, Void>  {
    MainActivity mainActivity = null;

    @Override
    protected Void doInBackground(MainActivity... mainActivities) {
     //   this.mainActivity = mainActivities[0];
        downloadAPK("https://yadi.sk/d/yMEIx0XVw1f9Tw");
        return null;
    }

    protected String downloadAPK(String... sUrl) {
        String path = "/sdcard/YourApp.apk";
        try {
            URL url = new URL(sUrl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
         //       publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e("YourApp", "Well that didn't work out so well...");
            Log.e("YourApp", e.getMessage());
        }
        return path;
    }

    public void publishProgress(int progress) {
        mainActivity.setTextOnTextView(3, "downloading apk: " + progress);
    }
}
