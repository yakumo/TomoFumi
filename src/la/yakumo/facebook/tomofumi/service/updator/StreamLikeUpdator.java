package la.yakumo.facebook.tomofumi.service.updator;

import android.content.Context;
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

            /*
            if ("true".equals(ret)) {
                SQLiteDatabase wdb = db.getWritableDatabase();
                ContentValues val = new ContentValues();
                if (like_posted == 0) {
                    val.put("like_count", like_count + 1);
                    val.put("like_posted", 1);
                    info.putInt("like_count", like_count + 1);
                }
                else {
                    val.put("like_count", like_count - 1);
                    val.put("like_posted", 0);
                    info.putInt("like_count", like_count - 1);
                }
                wdb.update(
                    "stream",
                    val,
                    "_id=?",
                    new String[] {
                        post_id,
                    });
            }
            */
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
