package la.yakumo.facebook.tomofumi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.data.Database;

public class ImageDownloader
{
    private static final String TAG = Constants.LOG_TAG;

    private static ImageDownloader downloader = null;
    private Database db;
    private SQLiteDatabase wdb;
    private Thread downloadThread;
    private Handler waiterHandler = null;
    private ArrayList<String> requestUrls = new ArrayList<String>();
    private ArrayList<String> gettingUrls = new ArrayList<String>();

    private ImageDownloader(Context context)
    {
        this.db = new Database(context);
        wdb = db.getWritableDatabase();
        Log.i(TAG, "ImageDownloader#<<constructor>>");
        downloadThread = new Thread(new Runnable() {
            public void run()
            {
                Log.i(TAG, "ImageDownloader$downloadThread");
                Looper.prepare();
                waiterHandler = new Handler() {
                    public void handleMessage(Message msg)
                    {
                        Log.i(TAG, "!!! handleMessage !!!");
                        switch (msg.what) {
                        case 1:
                            Looper.myLooper().quit();
                            break;
                        default:
                            break;
                        }

                    }
                };
                Log.i(TAG, "!!! start loop... !!!");
                while (!Thread.currentThread().isInterrupted()) {
                    Log.i(TAG, "!!! waiting... !!!");
                    Looper.loop();
                    Log.i(TAG, "!!! start download... !!!");
                    download();
                }
            }
        });
        downloadThread.start();
    }

    public static ImageDownloader getInstance(Context context)
    {
        if (null == downloader) {
            downloader = new ImageDownloader(context);
        }
        return downloader;
    }

    public void registUrl(String url)
    {
        synchronized (requestUrls) {
            requestUrls.add(url);
        }
    }

    public void request()
    {
        Log.i(TAG, "gettingUrls:"+gettingUrls);
        synchronized (gettingUrls) {
            Log.i(TAG, "requestUrls:"+requestUrls);
            synchronized (requestUrls) {
                gettingUrls.addAll(requestUrls);
                requestUrls.clear();
            }
        }
        Log.i(TAG, "waiterLooper:"+waiterHandler);
        waiterHandler.sendEmptyMessage(1);
    }

    private void download()
    {
        ContentValues val = new ContentValues();
        while (gettingUrls.size() > 0) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            String imgUri = gettingUrls.remove(0);
            Log.i(TAG, "ImageDownloader:request download:"+imgUri);

            try {
                URL url = new URL(imgUri);
                HttpURLConnection conn =
                    (HttpURLConnection)url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setDoInput(true);
                conn.connect();
                int length = conn.getContentLength();

                DataInputStream src =
                    new DataInputStream(conn.getInputStream());
                ByteArrayOutputStream dst =
                    new ByteArrayOutputStream();
                int ch;
                while ((ch = src.read()) != -1) {
                    dst.write(ch);
                }
                byte[] imageData = dst.toByteArray();

                val.clear();
                val.put("image_url", imgUri);
                val.put("image_data", imageData);
                wdb.beginTransaction();
                wdb.insertWithOnConflict(
                    "images",
                    null,
                    val,
                    SQLiteDatabase.CONFLICT_REPLACE);
                wdb.setTransactionSuccessful();
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } finally {
                wdb.endTransaction();
            }
        }
    }
}
