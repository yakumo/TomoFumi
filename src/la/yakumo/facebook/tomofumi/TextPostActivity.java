package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;

public class TextPostActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private static final int MODE_COMPOSE = 0;
    private static final int MODE_CAMERA = 1;
    private static final int MODE_GALLERY = 2;
    private static final int MODE_MYPLACES = 3;
    private static final int MODE_ADDLINK = 4;
    private static final int[] allModeViews = new int[] {
        R.id.compose_view,
        R.id.camera_view,
        R.id.gallery_view,
        R.id.myplaces_view,
        R.id.addlink_view,
    };
    private int postMode = MODE_COMPOSE;

    @Override
    public void onCreate(Bundle bndl)
    {
        super.onCreate(bndl);
        setContentView(R.layout.post_wall);
        updateMode(MODE_COMPOSE);

        View v = findViewById(R.id.command_list);
        if (null != v) {
            if (Constants.IS_FREE) {
                v.setVisibility(View.GONE);
            }
            else {
                v.setVisibility(View.VISIBLE);
            }
        }

        Intent intent = getIntent();
        Bundle ext = intent.getExtras();
        if (null != ext) {
            for (String key : ext.keySet()) {
                Log.i(TAG, "key:"+key);
            }
        }

        TextView inputView = (TextView) findViewById(R.id.stream_text);
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (Constants.IS_FREE) {
            if (null != inputView && null != text) {
                inputView.setText(text);
            }
        }
        else {
            TextView urlText = (TextView) findViewById(R.id.link_text);
            if (null != urlText) {
                try {
                    URL url = new URL(text);
                    updateMode(MODE_ADDLINK);
                    urlText.setText(text);
                } catch (MalformedURLException e) {
                    Log.i(TAG, "MalformedURLException", e);
                }
            }

        }
    }

    public void onClickSend(View v)
    {
        Log.i(TAG, "onClickSend");
    }

    public void onClickCompose(View v)
    {
        updateMode(MODE_COMPOSE);
    }

    public void onClickCamera(View v)
    {
        updateMode(MODE_CAMERA);
    }

    public void onClickGallery(View v)
    {
        updateMode(MODE_GALLERY);
    }

    public void onClickMyPlace(View v)
    {
        updateMode(MODE_MYPLACES);
    }

    public void onClickAddLink(View v)
    {
        updateMode(MODE_ADDLINK);
    }

    private void updateMode(int mode)
    {
        View v;
        if (postMode == mode) {
            return;
        }
        postMode = mode;
        for (int id : allModeViews) {
            v = findViewById(id);
            v.setVisibility(View.GONE);
        }
        switch (mode) {
        case MODE_CAMERA:
        case MODE_GALLERY:
        case MODE_MYPLACES:
        case MODE_ADDLINK:
            v = findViewById(allModeViews[mode]);
            Log.i(TAG, "visible view:"+v);
            if (null != v) {
                v.setVisibility(View.VISIBLE);
            }
            break;
        case MODE_COMPOSE:
        default:
            /*
             * all view is hidden
              v = findViewById(R.id.compse_view);
              v.setVisibility(View.VISIBLE);
            */
            break;
        }

    }
}
