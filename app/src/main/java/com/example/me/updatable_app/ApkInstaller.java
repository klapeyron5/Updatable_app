package com.example.me.updatable_app;

import android.os.AsyncTask;

public class ApkInstaller extends AsyncTask<String, Integer, Integer> {

    @Override
    protected Integer doInBackground(String... params) {
        return installApk(params);
    }

    protected Integer installApk(String... params) {
        return 0;
    }
}
