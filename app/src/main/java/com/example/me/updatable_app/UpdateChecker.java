package com.example.me.updatable_app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateChecker extends AsyncTask<MainActivity, Void, Void> {
    private MainActivity mainActivity = null;

    /**Actual version of code (the main number of update history)*/
    private static int lastCodeVersion = -1;

    /**Name of actual version, shown to user*/
    private static String lastVersionName = "null";

    /**URL of actual app's apk storing*/
    private static String lastAppURL = "null";

    /**@param mainActivities - [0]th element is the context of call*/
    @Override
    protected Void doInBackground(MainActivity... mainActivities) {
        this.mainActivity = mainActivities[0];
        checkUpdates(this.mainActivity);
        return null;
    }

    /**
     * Checks if there is a new code version and suggests user to update app
     * @param mainActivity
     */
    void checkUpdates(final MainActivity mainActivity) {
        try {
            if (mainActivity == null) throw new NullPointerException();
            PackageInfo pInfo = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0);
            int currentCodeVersion = pInfo.versionCode;
            findoutLastAppVersion();
            mainActivity.setTextOnTextView(2,"latest version: "+ lastVersionName); //TODO logs
            if (currentCodeVersion < lastCodeVersion)
                mainActivity.requestUpdate();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find out (from resources/update_info_URL) last code version, last app version, URL of a last app's apk
     * and updates lastCodeVersion, lastVersionName, lastAppURL params respectively
     */
    void findoutLastAppVersion() {
        try {
            URL url = new URL(mainActivity.getResources().getString(R.string.update_info_URL));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000); // timing out in a minute
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            int idx;
            while((str = in.readLine()) != null) {
                idx = str.indexOf("code_version");
                if (idx != -1) {
                    str = str.substring(idx + ("code_version").length()).trim();
                    lastCodeVersion = Integer.parseInt(str);
                }
                idx = str.indexOf("version_name");
                if (idx != -1) {
                    str = str.substring(idx + ("version_name").length()).trim();
                    lastVersionName = str;
                }
                idx = str.indexOf("apk_URL");
                if (idx != -1) {
                    str = str.substring(idx + ("apk_URL").length()).trim();
                    lastAppURL = str;
                }
            }
            in.close();
        } catch (MalformedURLException e) { //from new URL
            e.printStackTrace();
        } catch (IOException e) { //from url.openStream()
            e.printStackTrace();
        }
    }

    /**Actual version of code (the main number of update history)*/
    public static int getLastCodeVersion() {
        return lastCodeVersion;
    }

    /**Name of actual version, shown to user*/
    public static String getLastVersionName() {
        return lastVersionName;
    }

    /**URL of actual app's apk storing*/
    public static String getLastAppURL() {
        return lastAppURL;
    }
}
