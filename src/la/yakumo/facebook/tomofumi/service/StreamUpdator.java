package la.yakumo.facebook.tomofumi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    public StreamUpdator(Context context, Handler handler, OnProgress progress)
    {
        super(context, progress);
        this.context = context;
        this.handler = handler;
    }

    @Override
    protected Integer doInBackground(Runnable... params)
    {
        if (!facebook.loginCheck()) {
            return -1;
        }

        Database db = new Database(context);
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.query(
            "stream",
            new String[] {"updated_time"},
            null, null, null, null,
            "updated_time desc",
            "1");
        long lastStream = 0;
        if (c.moveToFirst()) {
            lastStream = c.getLong(0);
        }
        rdb.close();

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
            ",action_links"+
            ",attachment"+
            " FROM stream"+
            " WHERE source_id"+
            " IN"+
            "(SELECT"+
            " target_id"+
            " FROM connection"+
            " WHERE source_id="+facebook.getUserID()+")"+
            " AND is_hidden = 0"+
            ((lastStream == 0)?
             " ORDER BY created_time DESC"+
             " LIMIT 20":
             " AND updated_time>"+lastStream+
             " ORDER BY created_time DESC")+
            "";

        SQLiteDatabase wdb = db.getWritableDatabase();
        try {
            ContentValues val = new ContentValues();
            val.put("updated", 0);
            wdb.beginTransaction();
            wdb.update("stream", val, "updated=1", null);
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }

        publishProgress(0, 3, R.string.progress_wall_reading_message);

        ArrayList<Long> appList = new ArrayList<Long>();
        ArrayList<String> uidList = new ArrayList<String>();
        String resp = "{}";
        try {
            resp = fqlQuery(query);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        if (resp.startsWith("[")) {
            try {
                wdb.beginTransaction();
                ContentValues val = new ContentValues();
                try {
                    JSONArray mlist = new JSONArray(resp);
                    for (int i = 0 ; i < mlist.length() ; i++) {
                        JSONObject obj = mlist.getJSONObject(i);
                        Log.i(TAG, "msg:"+obj);

                        val.clear();
                        try {
                            val.put("_id", obj.getString("post_id"));
                            String app_id = obj.getString("app_id");
                            try {
                                long appId = Long.parseLong(app_id);
                                val.put("app_id", appId);
                                if (!appList.contains(appId)) {
                                    appList.add(appId);
                                }
                            } catch (NumberFormatException e) {
                            }
                            String uid = obj.getString("actor_id");
                            if (!uidList.contains(uid)) {
                                uidList.add(uid);
                            }
                            val.put("actor_id", uid);
                            val.put("target_id", obj.getString("target_id"));
                            val.put("created_time", obj.getLong("created_time"));
                            val.put("updated_time", obj.getLong("updated_time"));
                            val.put("message", obj.getString("message"));
                            JSONObject comments = obj.getJSONObject("comments");
                            val.put("comments", comments.getInt("count"));
                            val.put("comment_can_post",
                                    (comments.getBoolean("can_post")? 1: 0));
                            JSONObject likes = obj.getJSONObject("likes");
                            val.put("like_count", likes.getInt("count"));
                            val.put("like_posted",
                                    (likes.getBoolean("user_likes")? 1: 0));
                            val.put("can_like",
                                    (likes.getBoolean("can_like")? 1: 0));
                            val.put("updated", 1);

                            JSONObject att = obj.getJSONObject("attachment");
                            if (null != att) {
                                String desc = att.getString("description");
                                if (null == desc) {
                                    desc = "";
                                }
                                val.put("description", desc);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException", e);
                        }

                        wdb.insertWithOnConflict(
                            "stream",
                            null,
                            val,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
        }

        publishProgress(1, 3, R.string.progress_user_reading_message);

        if (uidList.size() > 0) {
            String users = "(";
            String sep = "";
            for (String u : uidList) {
                users = users + sep + u;
                sep = ",";
            }
            users = users + ")";

            resp = "{}";
            query =
                "SELECT"+
                " uid"+
                ",name"+
                ",pic_square"+
                ",username"+
                ",profile_url"+
                " FROM user"+
                " WHERE uid IN "+users+
                "";
            try {
                resp = fqlQuery(query);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }
            if (resp.startsWith("[")) {
                try {
                    wdb.beginTransaction();
                    ContentValues val = new ContentValues();
                    try {
                        JSONArray mlist = new JSONArray(resp);
                        for (int i = 0 ; i < mlist.length() ; i++) {
                            JSONObject obj = mlist.getJSONObject(i);
                            Log.i(TAG, "msg:"+obj);

                            val.clear();
                            try {
                                val.put("_id", obj.getLong("uid"));
                                val.put("name", obj.getString("name"));
                                val.put("username", obj.getString("username"));
                                val.put("pic_square",
                                        obj.getString("pic_square"));
                                val.put("profile_url",
                                        obj.getString("profile_url"));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSONException", e);
                            }

                            wdb.insertWithOnConflict(
                                "user",
                                null,
                                val,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException", e);
                    }
                    wdb.setTransactionSuccessful();
                } finally {
                    wdb.endTransaction();
                }
            }
        }

        publishProgress(2, 3, R.string.progress_app_reading_message);

        wdb.close();
        db.close();

        publishProgress(3, 3, R.string.progress_finish_message);

        if (null != handler) {
            for (Runnable r : params) {
                handler.post(r);
            }
        }

        return 0;
    }
}
