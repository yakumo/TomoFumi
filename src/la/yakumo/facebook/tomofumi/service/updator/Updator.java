package la.yakumo.facebook.tomofumi.service.updator;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.Facebook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Updator
    extends AsyncTask<Runnable,Integer,Integer>
{
    protected static final String TAG = Constants.LOG_TAG;
    protected Facebook facebook;
    private Resources resources = null;
    private OnProgress progress = null;

    public Updator(Context context)
    {
        facebook = Facebook.getInstance(context);
        resources = null;
        progress = null;
    }

    public Updator(Context context, OnProgress progress)
    {
        this.facebook = Facebook.getInstance(context);
        this.resources = context.getResources();
        this.progress = progress;
    }

    protected Integer doInBackground(Runnable... params)
    {
        return -1;
    }

    protected void onProgressUpdate(Integer... values)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onProgressUpdate");
        if (null != progress) {
            if (values.length == 3) {
                progress.onProgress(
                    values[0],
                    values[1],
                    resources.getString(values[2]));
            }
        }
    }

    protected void onPostExecute(Integer result)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onPostExecute:"+result);
    }

    protected String fqlQuery(String query)
        throws MalformedURLException, IOException
    {
        Bundle b = new Bundle();
        b.putString("method", "fql.query");
        b.putString("query", query);
        String ret = facebook.request(b);
        Log.i(TAG, "query result:"+ret);
        return ret;
    }

    protected String fqlMultiQuery(String... queries)
        throws MalformedURLException, IOException
    {
        Bundle b = new Bundle();
        b.putString("method", "fql.multiquery");
        JSONObject ql = new JSONObject();
        for (int i = 0 ; i < queries.length ; i++) {
            try {
                ql.put(String.format("query%d", i + 1), queries[i]);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }
        b.putString("queries", ql.toString());
        String ret = facebook.request(b);
        Log.i(TAG, "query result:"+ret);
        return ret;
    }

    public static interface OnProgress
    {
        public void onProgress(int now, int max, String msg);
    }
}
