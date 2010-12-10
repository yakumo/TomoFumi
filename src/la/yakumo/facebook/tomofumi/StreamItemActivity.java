package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class StreamItemActivity
    extends Activity
{
    private static final int ERROR_TOAST_DISPLAY_DURATION = 1500;

    private String postId;

    @Override
    public void onCreate(Bundle bndl)
    {
        super.onCreate(bndl);

        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");
        if (null == postId) {
            Toast.makeText(
                this,
                R.string.error_unknown_postid,
                ERROR_TOAST_DISPLAY_DURATION);
        }
    }
}
