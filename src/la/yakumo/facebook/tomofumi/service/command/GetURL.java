package la.yakumo.facebook.tomofumi.service.command;

import android.content.Context;
import java.net.MalformedURLException;
import java.io.IOException;
import la.yakumo.facebook.tomofumi.Constants;
import android.util.Log;
import android.os.Bundle;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class GetURL
    extends Command
{
    private String post_id;

    public GetURL(Context context, String post_id, OnResult result)
    {
        super(context, result);
        this.post_id = post_id;
    }

    protected Integer doInBackground(Integer... params)
    {
        Bundle info = new Bundle();
        String url = null;
        String sql =
            "SELECT permalink"+
            " FROM stream"+
            " WHERE post_id=\""+post_id+"\""+
            "";

        try {
            String resp = fqlQuery(sql);
            Log.i(TAG, "resp:"+resp);

            JSONArray res = new JSONArray(resp);
            if (res.length() > 0) {
                JSONObject obj = res.getJSONObject(0);
                if (obj.has("permalink")) {
                    url = obj.getString("permalink");
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }


        if (null != url) {
            info.putString("url", url);
        }
        result.onResult(info);

        return 0;
    }
}
