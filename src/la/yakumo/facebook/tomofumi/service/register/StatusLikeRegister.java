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

public class StatusLikeRegister
    extends ItemRegister
{
    private String post_id;
    private Context context;

    public StatusLikeRegister(
        Context context,
        String post_id)
    {
        super(context);
        this.post_id = post_id;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(OnSendFinish... params)
    {
        String errStr = null;
        Bundle info = new Bundle();

        int like_count = 0;
        int like_posted = 0;
        int can_like = 0;
        Database db = new Database(context);
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.query(
            "stream",
            new String[] {
                "like_count",
                "like_posted",
                "can_like"
            },
            "_id=?",
            new String[] {
                post_id,
            },
            null, null, null);
        if (c.moveToFirst()) {
            like_count = c.getInt(0);
            like_posted = c.getInt(1);
            can_like = c.getInt(2);
        }
        info.putInt("like_posted", like_posted == 0? 1: 0);
        for (OnSendFinish f : params) {
            f.onStartSend(info);
        }

        try {
            Bundle b = new Bundle();
            if (like_posted == 0) {
                b.putString("method", "stream.addLike");
            }
            else {
                b.putString("method", "stream.removeLike");
            }
            b.putString("post_id", post_id);
            String ret = facebook.request(b, "POST");
            Log.i(TAG, "regist result:"+ret);

            if ("true".equals(ret)) {
                SQLiteDatabase wdb = db.getWritableDatabase();
                ContentValues val = new ContentValues();
                if (like_posted == 0) {
                    val.put("like_count", like_count + 1);
                    val.put("like_posted", 1);
                }
                else {
                    val.put("like_count", like_count - 1);
                    val.put("like_posted", 0);
                }
                wdb.update(
                    "stream",
                    val,
                    "_id=?",
                    new String[] {
                        post_id,
                    });
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
