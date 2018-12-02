package com.example.me.updatable_app;

public interface InternetDataDownloadListener {
    /*Returns URL where data should be downloaded from*/
    public String getInternetDataUrl();

    /*Returns path where data should be stored on your android device*/
    public String getInternetDataPathToStore();

    /*Downloaded data successfully*/
    public void onInternetDataDownloaded(InternetDataDownloader internetDataDownloader);

    /*Mistakes have occurred during trying to download data*/
    public void onInternetDataCouldNotDownload(InternetDataDownloader internetDataDownloader);

    /*Here can be shown current progress of downloading on UI*/
    public void onInternetDataDownloadingProgressUpdate(Integer p);
}
