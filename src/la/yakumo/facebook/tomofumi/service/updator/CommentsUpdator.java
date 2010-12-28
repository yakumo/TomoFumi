package la.yakumo.facebook.tomofumi.service.updator;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
    private String user_id;
    private String post_id;

    public CommentsUpdator(
        Context context,
        String user_id,
        String post_id,
        OnEventCallback cb)
    {
        super(context, cb);
        this.user_id = user_id;
        this.post_id = post_id;
    }

    private ArrayList<String> parseComment(
        SQLiteDatabase wdb,
        JSONArray comments,
        JSONArray likes,
        JSONArray commentLikes)
    {
        ArrayList<String> ret = new ArrayList<String>();
        ContentValues val = new ContentValues();
        try {
            HashMap<String,JSONArray> commentLikeData =
                new HashMap<String,JSONArray>();
            for (int i = 0 ; i < commentLikes.length() ; i++) {
                JSONObject obj = commentLikes.getJSONObject(i);
                Log.i(TAG, "comment like:"+obj);

                val.clear();
                try {
                    String post_id = obj.getString("post_id");
                    String user_id = obj.getString("user_id");
                    if (!commentLikeData.containsKey(post_id)) {
                        commentLikeData.put(post_id, new JSONArray());
                    }
                    JSONArray arr = commentLikeData.get(post_id);
                    arr.put(user_id);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
            }
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
                    val.put("user_id", obj.getLong("fromid"));
                    val.put("time", obj.getLong("time"));
                    val.put("message", obj.getString("text"));
                    JSONArray arr;
                    if (commentLikeData.containsKey(itemId)) {
                        arr = commentLikeData.get(itemId);
                    }
                    else {
                        arr = new JSONArray();
                    }
                    val.put("likes", arr.toString());
                    val.put("like_count", 0);
                    val.put("like_posted", 0);
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
                    val.put("time", 0);
                    val.put("message", (String)null);
                    val.put("likes", (String)null);
                    val.put("like_count", 0);
                    val.put("like_posted", 0);
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
            //Log.e(TAG, "JSONException", e);
            ret = new JSONArray();
        }
        return ret;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        if (!facebook.loginCheck()) {
            return -1;
        }

        Bundle info = new Bundle();
        info.putString("post_id", post_id);
        start(info);

        Database db = new Database(context);

        ArrayList<String> commentIds = null;
        String query1 =
            "SELECT"+
            " id"+
            ",fromid"+
            ",time"+
            ",text"+
            ",likes"+
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
            " object_id"+
            ",post_id"+
            ",user_id"+
            " FROM like"+
            " WHERE "+
            " post_id IN "+
            "(SELECT"+
            " id"+
            " FROM #query1)"+
            "";
        String query4 =
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
            " OR"+
            " id IN "+
            "(SELECT"+
            " user_id"+
            " FROM #query3)"+
            "";
        String query5 =
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
            " OR"+
            " uid IN "+
            "(SELECT"+
            " user_id"+
            " FROM #query3)"+
            "";
        String resp = "{}";
        try {
            resp = fqlMultiQuery(query1, query2, query3, query4, query5);
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
                    JSONArray q4 = getResult(resps, 4);
                    commentIds = parseComment(wdb, q0, q1, q2);
                    parseUsers(wdb, q3, q4);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
        }

        finish(info, false);
        info.remove("post_id");

        for (String cid : commentIds) {
            info.putString("comment_post_id", cid);
            info.putInt("likes", 0);
            info.putBoolean("liked", false);
            try {
                Bundle b = new Bundle();
                resp = facebook.request(
                    String.format("%s/likes", cid),
                    b,
                    "GET");
                Log.i(TAG, "resp:"+resp);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }

            finish(info, false);
        }

        return 0;
    }

    protected void onPostExecute(Integer result)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onPostExecute:"+result);
    }
}
