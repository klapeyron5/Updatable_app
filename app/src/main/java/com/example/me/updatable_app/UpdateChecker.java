package com.example.me.updatable_app;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker extends AsyncTask<MainActivity, Void, Void> {
    MainActivity mainActivity = null;

    @Override
    protected Void doInBackground(MainActivity... mainActivities) {
        this.mainActivity = mainActivities[0];
        checkUpdates(this.mainActivity);
        return null;
    }

    void checkUpdates(final MainActivity mainActivity) {
        final Float currentAppVersion = 0.1f;
        final Float lastAppVersion = getLastAppVersion();

        mainActivity.setTextOnTextView(2,"latest version: "+lastAppVersion);

        if (currentAppVersion < lastAppVersion)
            mainActivity.requestUpdate(lastAppVersion);
    }

    Float getLastAppVersion() {
        try {
            URL url = new URL("http://f0230501.xsph.ru//Updatable_app.txt ");//"https://raw.githubusercontent.com/nikl5/Updatable_app/master/Updatable_app.txt");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000); // timing out in a minute
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while((str = in.readLine()) != null) {

                int idx = str.indexOf("version");
                if (idx != -1) {
                    str = str.substring(idx + ("version").length()).trim();
                    return Float.parseFloat(str);
                }
            }
            in.close();
        } catch (MalformedURLException e) { //from new URL
            e.printStackTrace();
        } catch (IOException e) { //from url.openStream()
            e.printStackTrace();
        }
        return null;
    }
}
