package la.yakumo.facebook.tomofumi.service.register;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;

public class StatusRegister
    extends ItemRegister
{
    private String statusText;

    public StatusRegister(Context context, String text)
    {
        super(context);
        statusText = text;
    }

    @Override
    protected Integer doInBackground(OnSendFinish... params)
    {
        String errStr = null;

        try {
            Bundle b = new Bundle();
            b.putString("method", "stream.publish");
            b.putString("message", statusText);
            String ret = facebook.request(b, "POST");
            Log.i(TAG, "regist result:"+ret);
        } catch (MalformedURLException e) {
            Log.i(TAG, "MalformedURLException", e);
            errStr = e.getMessage();
        } catch (IOException e) {
            Log.i(TAG, "IOException", e);
            errStr = e.getMessage();
        }

        for (OnSendFinish f : params) {
            f.onSended(errStr);
        }
        return 0;
    }
}
