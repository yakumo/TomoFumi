package la.yakumo.facebook.tomofumi.service.command;

import android.content.Context;
import android.os.AsyncTask;
import java.net.MalformedURLException;
import java.io.IOException;
import android.os.Bundle;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.Facebook;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class Command
    extends AsyncTask<Integer,Integer,Integer>
{
    protected static final String TAG = Constants.LOG_TAG;

    protected Context context;
    protected Facebook facebook;
    protected OnResult result;

    public Command(Context context, OnResult result)
    {
        this.context = context;
        this.facebook = Facebook.getInstance(context);
        this.result = result;
    }

    protected Integer doInBackground(Integer... params)
    {
        return -1;
    }

    protected void onProgressUpdate(Integer... values)
    {
    }

    protected void onPostExecute(Integer result)
    {
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

    public static interface OnResult
    {
        public void onResult(Bundle info);
    }
}
