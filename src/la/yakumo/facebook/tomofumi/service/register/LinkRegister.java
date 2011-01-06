package la.yakumo.facebook.tomofumi.service.register;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;

public class LinkRegister
    extends ItemRegister
{
    private String text = "";
    private String link = "";
    private String image = "";

    public LinkRegister(Context context, String text, String link, String image)
    {
        super(context);
        this.text = text;
        this.link = link;
        this.image = image;
    }

    @Override
    protected Integer doInBackground(OnSendFinish... params)
    {
        String errStr = null;
        Bundle info = new Bundle();

        try {
            Bundle b = new Bundle();
            b.putString("method", "links.post");
            b.putString("url", link);
            b.putString("comment", text);
            if (null != image) {
                b.putString("image", image);
            }
            String ret = facebook.request(b, "POST");
            Log.i(TAG, "regist result:"+ret);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
            errStr = e.getMessage();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            errStr = e.getMessage();
        }

        for (OnSendFinish f : params) {
            f.onSended(errStr, info);
        }
        return 0;
    }
}
