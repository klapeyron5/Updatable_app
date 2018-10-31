package com.example.me.updatable_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {
    final MainActivity mainContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTextOnTextView(1,"current version: "+getResources().getString(R.string.version_name));

        deleteDownloadedApk();

        new UpdateChecker().execute(this);
    }

    /**
     * Asks user to update app
     */
    public void requestUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Доступно обновление приложения "+getResources().getString(R.string.app_name)+" до версии " +
                        UpdateChecker.getLastVersionName() + " - желаете обновиться?")
                        .setCancelable(true)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new UpdateDownloader().execute(mainContext);
                                dialog.dismiss();
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

    public void setTextOnTextView(final int textViewNumber, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = null;
                switch(textViewNumber) {
                    case 1:
                        textView = findViewById(R.id.textView1CurrentVersion);
                        break;
                    case 2:
                        textView = findViewById(R.id.textView2LatestVersion);
                        break;
                    case 3:
                        textView = findViewById(R.id.textView3DownloadingProgress);
                        break;
                }
                if (textView != null)
                    textView.setText(text);
            }
        });
    }

    /**
     * Removes apk-file, if it called Updatable_app_<current version name>.apk
     */
    public void deleteDownloadedApk() {
        String pathToStoreApk = "/sdcard/Updatable_app_"+getResources().getString(R.string.version_name)+".apk";
        File f = new File(pathToStoreApk);
        f.delete();
    }
}
