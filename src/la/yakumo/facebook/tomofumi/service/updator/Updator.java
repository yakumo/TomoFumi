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
    extends AsyncTask<Void,Bundle,Integer>
{
    protected static final String TAG = Constants.LOG_TAG;
    protected Facebook facebook;
    protected Context context;
    protected Resources resources = null;
    private OnEventCallback callback = null;
    private boolean showProgress = true;

    public Updator(Context context)
    {
        facebook = Facebook.getInstance(context);
        this.context = context;
        this.resources = context.getResources();
    }

    public Updator(Context context, OnEventCallback callback)
    {
        this(context);
        this.callback = callback;
    }

    public Updator(Context context, OnEventCallback callback, boolean pShow)
    {
        this(context, callback);
        this.showProgress = pShow;
    }

    protected Integer doInBackground(Void... params)
    {
        return -1;
    }

    protected void onProgressUpdate(Bundle... values)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onProgressUpdate");
        if (null != callback && values.length > 0) {
            callback.onProgress(values[0]);
        }
    }

    protected void onPostExecute(Integer result)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onPostExecute:"+result);
    }

    protected void start(Bundle info)
    {
        if (null != callback) {
            callback.onStartEvent(info);
        }
    }

    protected void finish(Bundle info, boolean cancelled)
    {
        if (null != callback) {
            callback.onFinishEvent(info, cancelled);
        }
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

    public static interface OnEventCallback
    {
        public void onStartEvent(Bundle info);
        public void onProgress(Bundle info);
        public void onFinishEvent(Bundle info, boolean isCancel);
    }
}
