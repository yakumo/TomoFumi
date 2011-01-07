package la.yakumo.facebook.tomofumi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;

public class ProgressActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    @Override
    public void onCreate(Bundle bndl)
    {
        super.onCreate(bndl);

        Intent intent = getIntent();
        Log.i(TAG, "intent:"+intent);
        if (intent.hasExtra("is_finish")) {
            Log.i(TAG, "has is_finish, "+intent.getBooleanExtra("is_finish", false));
            if (intent.getBooleanExtra("is_finish", false)) {
                finish();
            }
        }

        setContentView(R.layout.progress_dialog);
    }

    protected void onNewIntent(Intent intent)
    {
        Log.i(TAG, "onNewIntent:"+intent);
        if (intent.hasExtra("is_finish")) {
            Log.i(TAG, "has is_finish, "+intent.getBooleanExtra("is_finish", false));
            if (intent.getBooleanExtra("is_finish", false)) {
                finish();
            }
        }
    }

    public void onPause()
    {
        super.onPause();
        Log.i(TAG, "finish progress");
    }
}
