package com.example.me.libupdater;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdatesChecker extends AsyncTask<UpdatesCheckListener, Void, Void> {

    private UpdatesCheckListener updatesCheckListener;
    private String updateInfoUrl;

    private boolean hasExecuted = false;

    public UpdatesChecker(String updateInfoUrl) {
        this.updateInfoUrl = updateInfoUrl;
    }

    /**@param updatesCheckListeners - [0]th element is the context of call*/
    @Override
    protected Void doInBackground(UpdatesCheckListener... updatesCheckListeners) {
        this.updatesCheckListener = updatesCheckListeners[0];
        checkUpdates();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (hasExecuted && UpdatesInfoStruct.isValidUpdatesInfo()) {
            UpdatesInfoStruct.isUpdatesCheckedSuccessfully = true;
            updatesCheckListener.onUpdatesChecked();
        }
        else {
            updatesCheckListener.onUpdatesCouldNotCheck("onPostExecute"); //TODO problems with thread synchronization OR updates info is not valid
        }
    }

    /**
     * Checks last code version, last version name, last app url
     * and store them in RAM. These can be obtained through getters of this class UpdatesChecker
     */
    private void checkUpdates() {
        try {
            if (updatesCheckListener == null) throw new NullPointerException();

            URL url = new URL(updateInfoUrl); //MalformedURLException
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //IOException
            conn.setConnectTimeout(60000); // timing out in a minute
//            url.openStream();
 //           conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            int idx;
            while((str = in.readLine()) != null) {
                idx = str.indexOf("code_version");
                if (idx != -1) {
                    str = str.substring(idx + ("code_version").length()).trim();
                    UpdatesInfoStruct.lastCodeVersion = Integer.parseInt(str);
                }
                idx = str.indexOf("version_name");
                if (idx != -1) {
                    str = str.substring(idx + ("version_name").length()).trim();
                    UpdatesInfoStruct.lastVersionName = str;
                }
                idx = str.indexOf("apk_URL");
                if (idx != -1) {
                    str = str.substring(idx + ("apk_URL").length()).trim();
                    UpdatesInfoStruct.lastAppURL = str;
                }
            }
            Log.d("TAG","checkUpdates 4");
            in.close();
            conn.disconnect();
            Log.d("TAG","checkUpdates 5");
            hasExecuted = true;
        } catch (NullPointerException e) {
            e.printStackTrace(); //TODO where is updatesCheckListener ?
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            updatesCheckListener.onUpdatesCouldNotCheck("IOException");
        }
    }

    private void newCheckUpdates() {
        try {
            Log.d("TAG","newCheckUpdates 1");
            URL url = new URL(updateInfoUrl); //MalformedURLException
            Log.d("TAG","newCheckUpdates 2");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //IOException
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            Log.d("TAG","newCheckUpdates 3");
            connection.getInputStream();
//            InputStream in = new BufferedInputStream(connection.getInputStream());
            Log.d("TAG","newCheckUpdates 4");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class UpdatesInfoStruct { //TODO add timestamp of updates checking (or number of updates check request) to avoid dissynchronization of processing updates check requests in UpdatesCheckListener
        /**Default lastCodeVersion value (when updates have not checked successfully).*/
        public final static int LastCodeVersionDefault = -1;
        /**Default lastAppURL value (when updates have not checked successfully).*/
        public final static String LastAppURLDefault = null;
        /**Default lastVersionName value (when updates have not checked successfully).*/
        public final static String LastVersionNameDefault = null;

        private static boolean isUpdatesCheckedSuccessfully = false;


        /**Actual version of code (the main number of update history)*/
        private static int lastCodeVersion = LastCodeVersionDefault;
        /**Name of actual version, shown to user*/
        private static String lastVersionName = LastVersionNameDefault;
        /**URL of actual app's apk storing*/
        private static String lastAppURL = LastAppURLDefault;

        /**Actual version of code (the main number of update history) or {@LastCodeVersionDefault}
         * if updates is not checked successfully.*/
        public static int getLastCodeVersion() {
            if (isUpdatesCheckedSuccessfully)
                return lastCodeVersion;
            return LastCodeVersionDefault;
        }

        /**Name of actual version, shown to user or {@LastVersionNameDefault}
         * if updates is not checked successfully.*/
        public static String getLastVersionName() {
            if (isUpdatesCheckedSuccessfully)
                return lastVersionName;
            return LastVersionNameDefault;
        }

        /**URL of actual app's apk storing or {@LastAppURLDefault}
         * if updates is not checked successfully.*/
        public static String getLastAppURL() {
            if (isUpdatesCheckedSuccessfully)
                return lastAppURL;
            return LastAppURLDefault;
        }

        /*Is info about updates in proper format?*/
        private static boolean isValidUpdatesInfo() {
            if ((lastCodeVersion >= 0)&&
                    (lastVersionName != null)&&
                    (lastAppURL != null))
                return true;
            else
                return false;
        }
    }
}
