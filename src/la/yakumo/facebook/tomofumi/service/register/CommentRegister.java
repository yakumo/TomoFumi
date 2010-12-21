package la.yakumo.facebook.tomofumi.service.register;

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
            f.onSended(errStr);
        }
        return 0;
    }
}
