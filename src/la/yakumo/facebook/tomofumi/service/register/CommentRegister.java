package la.yakumo.facebook.tomofumi.service.register;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;

public class CommentRegister
    extends ItemRegister
{
    private Context context;
    private String post_id;
    private String text;

    public CommentRegister(Context context, String post_id, String text)
    {
        super(context);
        this.context = context;
        this.post_id = post_id;
        this.text = text;
    }

    protected Integer doInBackground(OnSendFinish... params)
    {
        String errStr = null;
        Bundle info = new Bundle();

        for (OnSendFinish f : params) {
            f.onStartSend(info);
        }

        try {
            Bundle b = new Bundle();
            b.putString("method", "stream.addComment");
            b.putString("comment", text);
            b.putString("post_id", post_id);
            b.putString("uid", facebook.getUserID());
            String ret = facebook.request(b, "POST");
            Log.i(TAG, "regist result:"+ret);
            if (ret.startsWith("\"") && ret.endsWith("\"")) {
                info.putString(
                    "comment_post_id",
                    ret.substring(1, ret.length() - 2));
            }
        } catch (MalformedURLException e) {
            Log.i(TAG, "MalformedURLException", e);
            errStr = e.getMessage();
        } catch (IOException e) {
            Log.i(TAG, "IOException", e);
            errStr = e.getMessage();
        }

        for (OnSendFinish f : params) {
            f.onSended(errStr, info);
        }
        return 0;
    }
}
