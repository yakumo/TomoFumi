package la.yakumo.facebook.tomofumi.service.register;

import android.content.Context;
import android.os.Bundle;

public class CommentLikeRegister
    extends ItemRegister
{
    private String post_id;
    private int add_mode;

    public CommentLikeRegister(Context context, String post_id, int add_mode)
    {
        super(context);
        this.post_id = post_id;
        this.add_mode = add_mode;
    }

    protected Integer doInBackground(OnSendFinish... params)
    {
        String errStr = null;
        Bundle info = new Bundle();

        for (OnSendFinish f : params) {
            f.onStartSend(info);
        }

        for (OnSendFinish f : params) {
            f.onSended(errStr, info);
        }
        return 0;
    }
}
