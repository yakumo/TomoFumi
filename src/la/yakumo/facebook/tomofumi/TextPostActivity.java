package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TextPostActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    @Override
    public void onCreate(Bundle bndl)
    {
        super.onCreate(bndl);
        setContentView(R.layout.post_wall);
    }

    public void onClickSend(View v)
    {
        Log.i(TAG, "onClickSend");
    }
}
