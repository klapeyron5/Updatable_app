package com.example.me.updatable_app;

import android.os.AsyncTask;
import android.util.Log;

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
        Log.d("TAG", "InternetDataDownloader 1");
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
        Log.d("TAG","InternetDataDownloader downloadAPK 1");
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
            Log.d("TAG", "InternetDataDownloader downloadAPK 8");
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

    /**
     * Begins the installation of downloaded APK. User should confirm.
     */
  //  @Override
  //  protected void onPostExecute(Void aVoid) {

    //    if (result == 1) {
      //      Log.d("TAG","InternetDataDownloader onPostExecute 1");
           // this.mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));//, Uri.fromFile(new File(pathToStoreApk))));


      //      Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
          //  Log.d("TAG","Path to apk 1: "+Uri.fromFile(new File(pathToStoreApk)));
         //   Log.d("TAG","Path to apk 2: "+FileProvider.getUriForFile(this.mainActivity, "com.example.me.fileprovider", new File(pathToStoreApk)));
          //  intent.setDataAndType(FileProvider.getUriForFile(this.mainActivity, "com.example.me.fileprovider",
                    //new File(pathToStoreApk)), "application/vnd.android.package-archive");
         //   intent.setDataAndType(Uri.fromFile(new File(pathToStoreApk)), "application/vnd.android.package-archive");
     //       intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

          //  this.mainActivity.startActivity(intent);

           /* Intent i = new Intent();
            i.setAction(Intent.ACTION_INSTALL_PACKAGE);
            i.setDataAndType(Uri.fromFile(new File(pathToStoreApk)), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.mainActivity.startActivity(i);*/
     //  }
   // }
}
