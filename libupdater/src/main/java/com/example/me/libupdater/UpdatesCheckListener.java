package com.example.me.libupdater;

public interface UpdatesCheckListener {
    /*Updates checked successfully (valid updates info is read from cloud storage).*/
    public void onUpdatesChecked();

    /*Mistakes have occurred during updates checking*/
    public void onUpdatesCouldNotCheck(String error);
}
