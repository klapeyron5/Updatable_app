package com.example.me.mylibrary;

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

public class InternetDataDownloader extends AsyncTask<InternetDataDownloadListener, Integer, Void>  {
    private InternetDataDownloadListener internetDataDownloadListener = null;

    private boolean isExecuted = false;

    @Override
    protected Void doInBackground(InternetDataDownloadListener... internetDataDownloadListeners) { //params[0] - url to download, params[1] - path to store
        this.internetDataDownloadListener = internetDataDownloadListeners[0];
        downloadData();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (isExecuted)
            internetDataDownloadListener.onInternetDataDownloaded(this);
        else
            internetDataDownloadListener.onInternetDataCouldNotDownload(this);
    }

    protected void downloadData() {
        if (internetDataDownloadListener == null) throw new NullPointerException();

        String dataUrl = internetDataDownloadListener.getInternetDataUrl();
        String dataStorePath = internetDataDownloadListener.getInternetDataPathToStore();
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
            isExecuted = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            internetDataDownloadListener.onInternetDataCouldNotDownload(this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            internetDataDownloadListener.onInternetDataCouldNotDownload(this);
        } catch (IOException e) {
            e.printStackTrace();
            internetDataDownloadListener.onInternetDataCouldNotDownload(this);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        internetDataDownloadListener.onInternetDataDownloadingProgressUpdate(values[0]);
    }
}
