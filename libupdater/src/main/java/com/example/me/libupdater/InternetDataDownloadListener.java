package com.example.me.libupdater;

public interface InternetDataDownloadListener {
    /*Downloaded data successfully*/
    public void onInternetDataDownloaded(String internetDataPathToStore);

    /*Mistakes have occurred during trying to download data*/
    public void onInternetDataCouldNotDownload(String internetDataPathToStore);

    /*Here can be shown current progress of downloading on UI*/
    public void onInternetDataDownloadingProgressUpdate(Integer p);

    public void onInternetDataNoFreeStorageSpace();
}
