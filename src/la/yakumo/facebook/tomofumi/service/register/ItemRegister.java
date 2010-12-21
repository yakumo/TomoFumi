package la.yakumo.facebook.tomofumi.service.register;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.Facebook;

public class ItemRegister
    extends AsyncTask<ItemRegister.OnSendFinish,Integer,Integer>
{
    protected static final String TAG = Constants.LOG_TAG;

    private Handler handler = new Handler();
    protected Facebook facebook;

    protected ItemRegister(Context context)
    {
        facebook = Facebook.getInstance(context);
    }

    protected Integer doInBackground(OnSendFinish... params)
    {
        return -1;
    }

    protected void onProgressUpdate(Integer... values)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onProgressUpdate");
    }

    protected void onPostExecute(Integer result)
    {
        Log.i(TAG, ""+this.getClass().toString()+"#onPostExecute:"+result);
    }

    public static interface OnSendFinish
    {
        public void onStartSend();
        public void onSended(String reason);
    }
}
