package la.yakumo.facebook.tomofumi.service.updator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;

public class StreamLikeUpdator
    extends Updator
{
    private String post_id;
    private boolean isRegist = false;

    public StreamLikeUpdator(Context context, String post_id, boolean isRegist)
    {
        super(context);
        this.post_id = post_id;
        this.isRegist = isRegist;
    }

    @Override
    protected void updateCommand(Bundle info)
    {
        Database db = new Database(context);

        info.putString("post_id", post_id);

        int like_count = 0;
        int like_posted = 0;
        int can_like = 0;
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
            if (can_like == 0) {
                info.putString(
                    "error",
                    resources.getString(R.string.error_can_not_like));
                return;
            }
        }

        String errStr = null;
        try {
            Bundle b = new Bundle();
            if (isRegist) {
                b.putString("method", "stream.addLike");
            }
            else {
                b.putString("method", "stream.removeLike");
            }
            b.putString("post_id", post_id);
            String ret = facebook.request(b, "POST");
            Log.i(TAG, "regist result:"+ret);

            if ("true".equals(ret)) {
                ContentValues val = new ContentValues();
                if (isRegist) {
                    val.put("like_count", like_count + 1);
                    val.put("like_posted", 1);
                    info.putInt("like_count", like_count + 1);
                }
                else {
                    val.put("like_count", like_count - 1);
                    val.put("like_posted", 0);
                    info.putInt("like_count", like_count - 1);
                }

                SQLiteDatabase wdb = db.getWritableDatabase();
                try {
                    wdb.beginTransaction();
                    wdb.update(
                        "stream",
                        val,
                        "_id=?",
                        new String[] {
                            post_id,
                        });
                    wdb.setTransactionSuccessful();
                } finally {
                    wdb.endTransaction();
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
            errStr = e.getMessage();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            errStr = e.getMessage();
        }
        if (null != errStr) {
            info.putString("error", errStr);
        }
    }
}
