package la.yakumo.facebook.tomofumi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
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

    private ArrayList<String> parseComment(
        SQLiteDatabase wdb,
        JSONArray comments,
        JSONArray likes)
    {
        ArrayList<String> ret = new ArrayList<String>();
        ContentValues val = new ContentValues();
        try {
            for (int i = 0 ; i < comments.length() ; i++) {
                JSONObject obj = comments.getJSONObject(i);
                Log.i(TAG, "comment:"+obj);

                val.clear();
                try {
                    String itemId = obj.getString("id");
                    ret.add(itemId);
                    val.put("post_id", post_id);
                    val.put("data_mode", Constants.COMMENTMODE_COMMENT);
                    val.put("item_id", itemId);
                    val.put("user_id", obj.getString("fromid"));
                    val.put("like_count", 0);
                    val.put("time", obj.getLong("time"));
                    val.put("message", obj.getString("text"));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.insertWithOnConflict(
                    "comments",
                    null,
                    val,
                    SQLiteDatabase.CONFLICT_REPLACE);
            }
            for (int i = 0 ; i < likes.length() ; i++) {
                JSONObject obj = likes.getJSONObject(i);
                Log.i(TAG, "like:"+obj);

                val.clear();
                try {
                    val.put("post_id", post_id);
                    val.put("data_mode", Constants.COMMENTMODE_LIKE);
                    val.put("item_id", (String)null);
                    val.put("user_id", obj.getString("user_id"));
                    val.put("like_count", 0);
                    val.put("time", 0);
                    val.put("message", (String)null);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.insertWithOnConflict(
                    "comments",
                    null,
                    val,
                    SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }
        return ret;
    }

    private void parseUsers(
        SQLiteDatabase wdb,
        JSONArray profiles,
        JSONArray users)
    {
        HashMap<String,ContentValues> userMap =
            new HashMap<String,ContentValues>();
        ContentValues val;
        try {
            for (int i = 0 ; i < profiles.length() ; i++) {
                JSONObject obj = profiles.getJSONObject(i);
                Log.i(TAG, "profile:"+obj);

                val = new ContentValues();
                String uid = null;
                try {
                    uid = obj.getString("id");
                    val.put("_id", uid);
                    val.put("name", obj.getString("name"));
                    val.put("username", obj.getString("username"));
                    val.put("pic_square", obj.getString("pic_square"));
                } catch (JSONException e) {
                }
                if (null != uid) {
                    userMap.put(uid, val);
                }
            }
            for (int i = 0 ; i < users.length() ; i++) {
                JSONObject obj = users.getJSONObject(i);
                Log.i(TAG, "users:"+obj);

                String uid = null;
                try {
                    uid = obj.getString("uid");
                    val = userMap.get(uid);
                    val.put("profile_url", obj.getString("profile_url"));
                } catch (JSONException e) {
                }
            }

            for (ContentValues v : userMap.values()) {
                wdb.insertWithOnConflict(
                    "user",
                    null,
                    v,
                    SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }
    }

    private JSONArray getResult(JSONArray res, int idx)
    {
        JSONArray ret = null;
        try {
            ret = res.getJSONObject(idx).getJSONArray("fql_result_set");
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
            ret = new JSONArray();
        }
        return ret;
    }

    protected Integer doInBackground(Runnable... params)
    {
        if (!facebook.loginCheck()) {
            return -1;
        }

        publishProgress(0, 3, R.string.progress_wall_reading_message);

        Database db = new Database(context);

        ArrayList<String> commentIds = null;
        String query1 =
            "SELECT"+
            " id"+
            ",fromid"+
            ",time"+
            ",text"+
            " FROM comment"+
            " WHERE post_id=\""+post_id+"\""+
            "";
        String query2 =
            "SELECT"+
            " user_id"+
            " FROM like"+
            " WHERE post_id=\""+post_id+"\""+
            "";
        String query3 =
            "SELECT"+
            " id"+
            ",name"+
            ",pic_square"+
            ",username"+
            " FROM profile"+
            " WHERE"+
            " id IN "+
            "(SELECT"+
            " fromid"+
            " FROM #query1)"+
            " OR"+
            " id IN "+
            "(SELECT"+
            " user_id"+
            " FROM #query2)"+
            "";
        String query4 =
            "SELECT"+
            " uid"+
            ",profile_url"+
            " FROM user"+
            " WHERE"+
            " uid IN "+
            "(SELECT"+
            " fromid"+
            " FROM #query1)"+
            " OR"+
            " uid IN "+
            "(SELECT"+
            " user_id"+
            " FROM #query2)"+
            "";
        String resp = "{}";
        try {
            resp = fqlMultiQuery(query1, query2, query3, query4);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        SQLiteDatabase wdb = db.getWritableDatabase();
        if (resp.startsWith("[")) {
            wdb.delete("comments", "post_id=?", new String[] {post_id});
            try {
                wdb.beginTransaction();
                try {
                    JSONArray resps = new JSONArray(resp);
                    JSONArray q0 = getResult(resps, 0);
                    JSONArray q1 = getResult(resps, 1);
                    JSONArray q2 = getResult(resps, 2);
                    JSONArray q3 = getResult(resps, 3);
                    commentIds = parseComment(wdb, q0, q1);
                    parseUsers(wdb, q2, q3);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
        }

        HashMap<String,String> commentLikes = new HashMap<String,String>();
        for (String id : commentIds) {
            try {
                String path = String.format("/%s/likes", id);
                resp = facebook.request(path, "GET");
                JSONObject likeTmp = new JSONObject();
                JSONObject likes = new JSONObject(resp);
                JSONArray data = likes.getJSONArray("data");
                for (int i = 0 ; i < data.length() ; i++) {
                    JSONObject obj = data.getJSONObject(i);
                    try {
                        likeTmp.put(
                            obj.getString("id"),
                            obj.getString("name"));
                    } catch (JSONException e) {
                    }
                }
                commentLikes.put(id, likeTmp.toString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }
        try {
            ContentValues val = new ContentValues();
            wdb.beginTransaction();
            for (String key : commentLikes.keySet()) {
                val.clear();
                val.put("likes", commentLikes.get(key));
                wdb.update("comments", val, "item_id=?", new String[] {key});
            }
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }

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
