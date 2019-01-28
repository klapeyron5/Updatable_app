package com.example.me.libupdater;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class InternetDataDownloader extends AsyncTask<InternetDataDownloadListener, Integer, Void> {

    private InternetDataDownloadListener internetDataDownloadListener;
    private String internetDataUrl;
    private String internetDataPathToStore;

    private boolean hasExecuted = false;

    public InternetDataDownloader(String internetDataUrl, String internetDataPathToStore) {
        this.internetDataUrl = internetDataUrl;
        this.internetDataPathToStore = internetDataPathToStore;
    }

    @Override
    protected Void doInBackground(InternetDataDownloadListener... internetDataDownloadListeners) {
        this.internetDataDownloadListener = internetDataDownloadListeners[0];
        downloadData();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (hasExecuted)
            internetDataDownloadListener.onInternetDataDownloaded(internetDataPathToStore);
        else
            internetDataDownloadListener.onInternetDataCouldNotDownload(internetDataPathToStore);
    }

    protected void downloadData() {
        if (internetDataDownloadListener == null) throw new NullPointerException();

        String dataUrl = internetDataUrl;
        String dataStorePath = internetDataPathToStore;
        try {
            URL url = new URL(dataUrl); //MalformedURLException
            URLConnection connection = url.openConnection(); //IOException
            connection.connect(); //IOException

            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream()); //IOException
            OutputStream output = new FileOutputStream(dataStorePath); //FileNotFoundException

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) { //IOException
                total += count;
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count); //IOException
            }
            output.flush(); //IOException
            output.close(); //IOException
            input.close(); //IOException
            hasExecuted = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            internetDataDownloadListener.onInternetDataCouldNotDownload(internetDataPathToStore);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            internetDataDownloadListener.onInternetDataCouldNotDownload(internetDataPathToStore);
        } catch (IOException e) {
            e.printStackTrace();
            internetDataDownloadListener.onInternetDataCouldNotDownload(internetDataPathToStore);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        internetDataDownloadListener.onInternetDataDownloadingProgressUpdate(values[0]);
    }
}
