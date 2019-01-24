package com.example.me.someapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.example.me.libupdater.Updater;

public class MainActivity extends FragmentActivity { //обязательно FragmentActivity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Updater updater = new Updater();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(updater,"updater");
        fragmentTransaction.commitNow(); //синхронная регистрация фрагмента

        updater.checkSuggestUpdate(); //проверка и предложение обновлений
    }
}
