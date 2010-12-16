package la.yakumo.facebook.tomofumi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentsUpdator
    extends Updator
{
    private Context context;
    private Handler handler;
    private String post_id;

    public CommentsUpdator(
        Context context,
        String post_id,
        Handler handler)
    {
        super(context);
        this.context = context;
        this.handler = handler;
        this.post_id = post_id;
    }

    public CommentsUpdator(
        Context context,
        String post_id,
        Handler handler,
        OnProgress progress)
    {
        super(context, progress);
        this.context = context;
        this.handler = handler;
        this.post_id = post_id;
    }

    protected Integer doInBackground(Runnable... params)
    {
        if (!facebook.loginCheck()) {
            return -1;
        }

        publishProgress(0, 3, R.string.progress_wall_reading_message);

        Database db = new Database(context);
        ContentValues val = new ContentValues();
        ArrayList<Integer> userIds = new ArrayList<Integer>();

        String query =
            "SELECT"+
            " object_id"+
            ",fromid"+
            ",time"+
            ",text"+
            " FROM comment"+
            " WHERE post_id=\""+post_id+"\""+
            "";
        String resp = "{}";
        try {
            resp = fqlQuery(query);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        if (resp.startsWith("[")) {
            SQLiteDatabase wdb = db.getWritableDatabase();
            try {
                wdb.beginTransaction();
                try {
                    JSONArray mlist = new JSONArray(resp);
                    for (int i = 0 ; i < mlist.length() ; i++) {
                        JSONObject obj = mlist.getJSONObject(i);
                        Log.i(TAG, "msg:"+obj);

                        val.clear();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
            wdb.close();
        }

        publishProgress(1, 3, R.string.progress_wall_reading_message);

        query =
            "SELECT"+
            " user_id"+
            " FROM like"+
            " WHERE post_id=\""+post_id+"\""+
            "";
        resp = "{}";
        try {
            resp = fqlQuery(query);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        if (resp.startsWith("[")) {
            SQLiteDatabase wdb = db.getWritableDatabase();
            try {
                wdb.beginTransaction();
                try {
                    JSONArray mlist = new JSONArray(resp);
                    for (int i = 0 ; i < mlist.length() ; i++) {
                        JSONObject obj = mlist.getJSONObject(i);
                        Log.i(TAG, "msg:"+obj);

                        val.clear();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
            wdb.close();
        }

        publishProgress(2, 3, R.string.progress_wall_reading_message);

        db.close();

        if (null != handler) {
            for (Runnable r : params) {
                handler.post(r);
            }
        }

        return 0;
    }

    protected void onPostExecute(Integer result)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onPostExecute:"+result);
    }
}
