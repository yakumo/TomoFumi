package la.yakumo.facebook.tomofumi.service.updator;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import la.yakumo.facebook.tomofumi.data.Database;

public class AddStreamMessageUpdator
    extends Updator
{
    private String message;

    public AddStreamMessageUpdator(Context context, String message)
    {
        super(context);
        this.message = message;
    }

    @Override
    protected void updateCommand(Bundle info)
    {
        String errStr = null;

        try {
            Bundle b = new Bundle();
            b.putString("method", "stream.publish");
            b.putString("message", message);
            String ret = facebook.request(b, "POST");
            Log.i(TAG, "regist result:"+ret);

            if ("false".equals(ret) ||
                "0".equals(ret)) {
                return;
            }

            info.putString("post_id", ret);
            ContentValues val = new ContentValues();
            long t = Calendar.getInstance().getTimeInMillis() / 1000;
            val.put("_id", ret);
            val.put("app_id", (String)null);
            val.put("actor_id", facebook.getUserID());
            val.put("target_id", (String)null);
            val.put("created_time", t);
            val.put("updated_time", t);
            val.put("message", message);
            val.put("comment_count", 0);
            val.put("comment_can_post", 1);
            val.put("like_count", 0);
            val.put("like_posted", 0);
            val.put("can_like", 1);
            val.put("updated", 1);
            val.put("description", (String)null);
            val.put("attachment_name", (String)null);
            val.put("attachment_caption", (String)null);
            val.put("attachment_link", (String)null);
            val.put("attachment_icon", (String)null);
            val.put("attachment_image", (String)null);

            Database db = new Database(context);
            SQLiteDatabase wdb = db.getWritableDatabase();
            try {
                wdb.beginTransaction();
                wdb.insertWithOnConflict(
                    "stream",
                    null,
                    val,
                    SQLiteDatabase.CONFLICT_REPLACE);
                wdb.setTransactionSuccessful();
            } catch (SQLiteException e) {
            } finally {
                if (wdb.inTransaction()) {
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
    }
}
