package la.yakumo.facebook.tomofumi.service.updator;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.activity.ProgressActivity;
import la.yakumo.facebook.tomofumi.data.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamUpdator
    extends Updator
{
    private boolean isClear = false;
    private ProgressDialog progress;

    public StreamUpdator(Context context, Handler handler, boolean isClear)
    {
        super(context);
        this.isClear = isClear;
    }

    @Override
    protected void updateCommand(Bundle info)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, ProgressActivity.class);
        intent.putExtra("is_finish", false);
        context.startActivity(intent);

        Database db = new Database(context);

        if (isClear) {
        }

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

        String q1 =
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
        String q2 =
            "SELECT"+
            " id"+
            ",name"+
            ",pic_square"+
            ",username"+
            " FROM profile"+
            " WHERE id IN "+
            "(SELECT"+
            " actor_id"+
            " FROM #query1)"+
            "";
        String q3 =
            "SELECT"+
            " uid"+
            ",profile_url"+
            " FROM user"+
            " WHERE uid IN "+
            "(SELECT"+
            " actor_id"+
            " FROM #query1)"+
            "";
        String resp = "{}";
        try {
            resp = facebook.fqlMultiQuery(q1, q2, q3);
            Log.i(TAG, "resp:"+resp);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        if (resp.startsWith("[")) {
            SQLiteDatabase wdb = db.getWritableDatabase();
            try {
                wdb.beginTransaction();
                ContentValues val = new ContentValues();
                val.put("updated", 0);
                wdb.update("stream", val, "updated=1", null);

                try {
                    JSONArray resps = new JSONArray(resp);
                    parseStream(
                        wdb,
                        resps.getJSONObject(0).getJSONArray("fql_result_set"));
                    parseUsers(
                        wdb,
                        resps.getJSONObject(1).getJSONArray("fql_result_set"),
                        resps.getJSONObject(2).getJSONArray("fql_result_set"));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
        }

        intent.putExtra("is_finish", true);
        context.startActivity(intent);
    }

    private void setVariable(
        ContentValues val,
        String dkey,
        JSONObject obj,
        String skey)
    {
        String tmp = "";
        try {
            tmp = obj.getString(skey);
        } catch (JSONException e) {
        }
        if (null == tmp) {
            tmp = "";
        }
        val.put(dkey, tmp);
    }

    private void parseStream(
        SQLiteDatabase wdb,
        JSONArray stream)
    {
        ContentValues val = new ContentValues();
        try {
            for (int i = 0 ; i < stream.length() ; i++) {
                JSONObject obj = stream.getJSONObject(i);
                Log.i(TAG, "msg:"+obj);

                val.clear();
                try {
                    val.put("_id", obj.getString("post_id"));
                    String app_id = obj.getString("app_id");
                    try {
                        val.put("app_id", Long.parseLong(app_id));
                    } catch (NumberFormatException e) {
                    }
                    val.put("actor_id", obj.getString("actor_id"));
                    val.put("target_id", obj.getString("target_id"));
                    val.put("created_time", obj.getLong("created_time"));
                    val.put("updated_time", obj.getLong("updated_time"));
                    val.put("message", obj.getString("message"));
                    JSONObject comments = obj.getJSONObject("comments");
                    val.put("comment_count", comments.getInt("count"));
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
                        setVariable(val, "description",
                                    att, "description");
                        setVariable(val, "attachment_name",
                                    att, "name");
                        setVariable(val, "attachment_caption",
                                    att, "caption");
                        setVariable(val, "attachment_link",
                                    att, "href");
                        setVariable(val, "attachment_icon",
                                    att, "icon");

                        try {
                            JSONArray media = att.getJSONArray("media");
                            if (null != media) {
                                JSONObject fmo = media.getJSONObject(0);
                                setVariable(val, "attachment_image",
                                            fmo, "src");
                            }
                        } catch (JSONException e) {
                        }
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
}
