package com.example.me.updatable_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
    final MainActivity thiis = this;
    public float lastAppVersion = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TAG", "Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new UpdateChecker().execute(this);
    }

    public void requestUpdate(final Float lastAppVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thiis.lastAppVersion = lastAppVersion;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Доступно обновление приложения Updatable_upp до версии " +
                        lastAppVersion + " - желаете обновиться?")
                        .setCancelable(true)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new UpdateDownloader().execute(thiis);
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
}
