package la.yakumo.facebook.tomofumi.service.register;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import la.yakumo.facebook.tomofumi.data.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentLikeRegister
    extends ItemRegister
{
    private String post_id;
    private int add_mode;
    private String user_id;
    private Database db;

    public CommentLikeRegister(
        Context context,
        String post_id,
        int add_mode,
        String user_id)
    {
        super(context);
        this.post_id = post_id;
        this.add_mode = add_mode;
        this.user_id = user_id;
        db = new Database(context);
    }

    protected Integer doInBackground(OnSendFinish... params)
    {
        String errStr = null;
        Bundle info = new Bundle();

        if (add_mode == -1) {
            add_mode = 1;
            SQLiteDatabase rdb = db.getReadableDatabase();
            Cursor c = rdb.query(
                "comments",
                new String[] {"likes"},
                "item_id=?",
                new String[] {
                    post_id,
                },
                null, null, null);
            long lastStream = 0;
            if (c.moveToFirst()) {
                try {
                    JSONArray a = new JSONArray(c.getString(0));
                    Log.i(TAG, "likes:"+a+","+user_id);
                    add_mode = 1;
                    for (int i = 0 ; i < a.length() ; i++) {
                        Log.i(TAG, "check:"+user_id+"="+a.getString(i));
                        if (user_id.equals(a.getString(i))) {
                            Log.i(TAG, "find !!!");
                            add_mode = 0;
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                    add_mode = 1;
                }
            }
        }

        info.putInt("like_posted", add_mode);
        for (OnSendFinish f : params) {
            f.onStartSend(info);
        }

        try {
            Bundle b = new Bundle();
            String path = "/"+post_id+"/likes";
            String ret = facebook.request(
                path,
                b,
                (add_mode == 0)? "DELETE": "POST");
            Log.i(TAG, "regist result:"+ret);

            b = new Bundle();
            String resp = facebook.request(
                "/"+post_id+"/likes",
                b,
                "GET");
            Log.i(TAG, "likes result:"+resp);

            SQLiteDatabase wdb = db.getWritableDatabase();
            try {
                wdb.beginTransaction();
                boolean liked_flag = false;
                JSONArray ids = new JSONArray();
                try {
                    JSONObject respObj = new JSONObject(resp);
                    JSONArray datas = respObj.getJSONArray("data");
                    for (int i = 0 ; i < datas.length() ; i++) {
                        try {
                            JSONObject user = datas.getJSONObject(i);
                            String uid = user.getString("id");
                            if (user_id.equals(uid)) {
                                liked_flag = true;
                            }
                            ids.put(uid);
                        } catch (JSONException e) {
                        }
                    }
                } catch (JSONException e) {
                }
                Log.i(TAG, "id list:"+ids.toString());
                info.putInt("likes", ids.length());
                info.putBoolean("liked", liked_flag);
                ContentValues val = new ContentValues();
                val.put("likes", ids.toString());
                val.put("like_count", ids.length());
                val.put("like_posted", liked_flag);
                wdb.update(
                    "comments",
                    val,
                    "item_id=?",
                    new String[] {
                        post_id,
                    });
                wdb.setTransactionSuccessful();
            } finally {
                wdb.endTransaction();
            }
        } catch (MalformedURLException e) {
            Log.i(TAG, "MalformedURLException", e);
            errStr = e.getMessage();
        } catch (IOException e) {
            Log.i(TAG, "IOException", e);
            errStr = e.getMessage();
        }

        for (OnSendFinish f : params) {
            f.onSended(errStr, info);
        }
        return 0;
    }
}
