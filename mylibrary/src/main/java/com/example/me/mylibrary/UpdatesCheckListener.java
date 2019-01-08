package com.example.me.mylibrary;

public interface UpdatesCheckListener {
    /*Returns URL where updates info is stored*/
    public String getUpdatesInfoUrl();

    /*Updates checked successfully*/
    public void onUpdatesChecked(UpdatesChecker updatesChecker);

    /*Mistakes have occurred during updates checking*/
    public void onUpdatesCouldNotCheck(UpdatesChecker updatesChecker);
}
