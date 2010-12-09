package la.yakumo.facebook.tomofumi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import la.yakumo.facebook.tomofumi.data.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamUpdator
    extends Updator
{
    private Context context;
    private Handler handler;

    public StreamUpdator(Context context, Handler handler)
    {
        super(context);
        this.context = context;
        this.handler = handler;
    }

    @Override
    protected Integer doInBackground(Runnable... params)
    {
        if (!facebook.loginCheck()) {
            return -1;
        }

        long lastStream = 0;
        String query =
            "SELECT"+
            " post_id"+
            ",app_id"+
            ",actor_id"+
            ",target_id"+
            ",created_time"+
            ",updated_time"+
            ",message"+
            ",app_data"+
            ",comments"+
            ",likes"+
            " FROM stream"+
            " WHERE source_id"+
            " IN"+
            "(SELECT"+
            " target_id"+
            " FROM connection"+
            " WHERE source_id="+facebook.getUserID()+")"+
            " AND is_hidden = 0"+
            ((lastStream == 0)?
             " ORDER BY updated_time DESC"+
             " LIMIT 20":
             " AND updated_time>"+lastStream+
             " ORDER BY created_time DESC")+
            "";

        Database db = new Database(context);
        SQLiteDatabase wdb = db.getWritableDatabase();
        try {
            ContentValues val = new ContentValues();
            String resp = fqlQuery(query);
            JSONArray mlist = new JSONArray(resp);
            wdb.beginTransaction();
            for (int i = 0 ; i < mlist.length() ; i++) {
                JSONObject obj = mlist.getJSONObject(i);
                Log.i(TAG, "msg:"+obj);

                val.clear();
                try {
                    val.put("post_id", obj.getString("post_id"));
                    String app_id = obj.getString("app_id");
                    try {
                        val.put("app_id", Integer.parseInt(app_id));
                    } catch (NumberFormatException e) {
                    }
                    val.put("actor_id", obj.getLong("actor_id"));
                    val.put("target_id", obj.getString("target_id"));
                    val.put("created_time", obj.getLong("created_time"));
                    val.put("updated_time", obj.getLong("updated_time"));
                    val.put("message", obj.getString("message"));
                    JSONObject comments = obj.getJSONObject("comments");
                    val.put("comments", comments.getInt("count"));
                    JSONObject likes = obj.getJSONObject("likes");
                    val.put("likes", likes.getInt("count"));
                    val.put("updated", 1);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }

                wdb.insertWithOnConflict(
                    "stream",
                    null,
                    val,
                    SQLiteDatabase.CONFLICT_REPLACE);
            }
            wdb.setTransactionSuccessful();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
            return -1;
        } finally {
            wdb.endTransaction();
        }
        db.close();

        if (null != handler) {
            for (Runnable r : params) {
                handler.post(r);
            }
        }

        return 0;
    }
}
