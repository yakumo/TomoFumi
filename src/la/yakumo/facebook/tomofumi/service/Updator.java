package la.yakumo.facebook.tomofumi.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import la.yakumo.facebook.tomofumi.Constants;

public class Updator
    extends AsyncTask<String,Integer,Integer>
{
    protected static final String TAG = Constants.LOG_TAG;
    protected Facebook facebook;

    public Updator(Context context)
    {
        facebook = Facebook.getInstance(context);
    }

    protected Integer doInBackground (String... params)
    {
        return -1;
    }

    protected void onProgressUpdate (Integer... values)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onProgressUpdate");
    }

    protected void onPostExecute (Integer result)
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
}
