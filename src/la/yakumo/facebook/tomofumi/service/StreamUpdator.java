package la.yakumo.facebook.tomofumi.service;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamUpdator
    extends Updator
{
    public StreamUpdator(Context context)
    {
        super(context);
    }

    @Override
    protected Integer doInBackground (String... params)
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
             " ORDER BY created_time"+
             " DESC LIMIT 20":
             " AND created_time>"+lastStream+
             " ORDER BY created_time DESC")+
            "";
        try {
            String resp = fqlQuery(query);
            JSONArray mlist = new JSONArray(resp);
            for (int i = 0 ; i < mlist.length() ; i++) {
                JSONObject obj = mlist.getJSONObject(i);
                Log.i(TAG, "msg:"+obj);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
            return -1;
        }

        return 0;
    }
}
