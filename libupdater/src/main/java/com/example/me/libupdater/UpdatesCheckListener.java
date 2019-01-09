package com.example.me.libupdater;

public interface UpdatesCheckListener {
    /*Updates checked successfully*/
    public void onUpdatesChecked(UpdatesChecker updatesChecker);

    /*Mistakes have occurred during updates checking*/
    public void onUpdatesCouldNotCheck(UpdatesChecker updatesChecker);
}
