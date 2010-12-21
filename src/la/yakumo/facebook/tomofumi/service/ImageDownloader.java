package la.yakumo.facebook.tomofumi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.SynchronousQueue;
import la.yakumo.facebook.tomofumi.data.Database;

public class ImageDownloader
{
    private static final int TASK_NUM = 5;

    private static ImageDownloader instance = null;
    private OnDownloadCallback callback = null;
    private SynchronousQueue<String> registedUrl = new SynchronousQueue<String>();
    private DownloadTask[] tasks = new DownloadTask[TASK_NUM];
    private Context context;
    private Database database;

    protected ImageDownloader()
    {
    }

    protected ImageDownloader(Context context, OnDownloadCallback callback)
    {
        this.context = context;
        this.callback = callback;

        this.database = new Database(context);

        for (int i = 0 ; i < TASK_NUM ; i++) {
            tasks[i] = null;
        }
    }

    public static final ImageDownloader startDownloader(
        Context context,
        OnDownloadCallback callback)
    {
        if (null == instance) {
            instance = new ImageDownloader(context, callback);
        }
        return instance;
    }

    public static final void registDownload(String url)
    {
        instance.regist(url);
    }

    private final void regist(String url)
    {
        registedUrl.offer(url);

        for (int i = 0 ; i < TASK_NUM ; i++) {
            if (tasks[i] == null) {
                tasks[i] = new DownloadTask();
                tasks[i].execute();
            }
        }
    }

    public static interface OnDownloadCallback
    {
        public void downloaded(String url);
    }

    public class DownloadTask
        extends AsyncTask<String,Integer,Integer>
    {
        protected Integer doInBackground(String... urls)
        {
            String dataUrl = registedUrl.poll();
            if (dataUrl == null) {
                return 0;
            }

            SQLiteDatabase wdb = database.getWritableDatabase();
            try {
                URL url = new URL(dataUrl);
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

                ContentValues val = new ContentValues();
                val.put("image_url", dataUrl);
                val.put("image_data", imageData);
                wdb.beginTransaction();
                wdb.insertWithOnConflict(
                    "images",
                    null,
                    val,
                    SQLiteDatabase.CONFLICT_REPLACE);
                wdb.setTransactionSuccessful();
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            } finally {
                wdb.endTransaction();
            }
            return 0;
        }

        protected void onProgressUpdate(Integer... progress)
        {
        }

        protected void onPostExecute(Long result)
        {
        }
    }
}
