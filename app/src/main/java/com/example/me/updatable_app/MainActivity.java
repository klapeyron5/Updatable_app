package com.example.me.updatable_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Updater().execute(this);
    }

    public void Update(final Float lastAppVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Доступно обновление приложения Updatable_upp до версии " +
                        lastAppVersion + " - желаете обновиться? " +
                        "Если вы согласны - вы будете перенаправлены к скачиванию APK файла,"
                        +" который затем нужно будет открыть.")
                        .setCancelable(true)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String apkUrl = "https://yadi.sk/d/yMEIx0XVw1f9Tw";
                                intent.setData(Uri.parse(apkUrl));

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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

    public void setTextTextView2(final Float lastAppVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView2 = findViewById(R.id.textView2);
                textView2.setText("latest version "+lastAppVersion);
            }
        });
    }
}
