package la.yakumo.facebook.tomofumi.service;

import android.content.Context;

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

        for (OnSendFinish f : params) {
            if (null == errStr) {
                f.onSendSuccess();
            }
            else {
                f.onSendFail(errStr);
            }
        }
        return 0;
    }
}
