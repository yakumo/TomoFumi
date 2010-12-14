package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.IClientServiceCallback;

public class TextPostActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private static final int MODE_COMPOSE = 0;
    private static final int MODE_IMAGE = 1;
    private static final int MODE_MYPLACES = 2;
    private static final int MODE_ADDLINK = 3;
    private static final int[] allModeViews = new int[] {
        R.id.compose_view,
        R.id.image_view,
        R.id.myplaces_view,
        R.id.addlink_view,
    };
    private int postMode = MODE_COMPOSE;

    private Handler handler = new Handler();
    private IClientService service = null;

    private IClientServiceCallback listener = new IClientServiceCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);

            if (null == service) {
                return;
            }
            handler.post(new Runnable() {
                public void run()
                {
                    TextView inputView =
                        (TextView)findViewById(R.id.stream_text);
                    String text = inputView.getText().toString();

                    try {
                        switch (postMode) {
                        case MODE_COMPOSE:
                            service.addStream(text);
                            break;
                        case MODE_IMAGE:
                            break;
                        case MODE_MYPLACES:
                            break;
                        case MODE_ADDLINK:
                            break;
                        default:
                            break;
                        }
                    } catch (RemoteException e) {
                        Log.i(TAG, "RemoteException", e);
                    }
                }
            });
        }

        public void loginFailed(String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
        }

        public void updatedStream(String errorMessage)
        {
        }

        public void updatedComment(String post_id, String errorMessage)
        {
        }

        public void updatedLike(String post_id, String errorMessage)
        {
        }

        public void updateProgress(int now, int max, String msg)
        {
        }

        public void addedStream(String errorMessage)
        {
            handler.post(new Runnable() {
                public void run()
                {
                    try {
                        if (null != service) {
                            service.unregisterCallback(listener);
                            unbindService(conn);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException", e);
                    }
                    finish();
                }
            });
        }

        public void addedComment(String post_id, String errorMessage)
        {
        }

        public void addedLike(String post_id, String errorMessage)
        {
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerCallback(listener);
                service.login();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

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
            while (true) {
                if (null != urlText) {
                    try {
                        URL url = new URL(text);
                        updateMode(MODE_ADDLINK);
                        urlText.setText(text);
                        break;
                    } catch (MalformedURLException e) {
                    }
                }
                // TODO: read image from intent

                if (null != inputView && null != text) {
                    inputView.setText(text);
                    break;
                }

                break;
            }
        }
    }

    public void onClickSend(View v)
    {
        Log.i(TAG, "onClickSend");
        switch (postMode) {
        case MODE_COMPOSE:
            sendCompose();
            break;
        case MODE_IMAGE:
            sendImage();
            break;
        case MODE_MYPLACES:
            sendPlace();
            break;
        case MODE_ADDLINK:
            sendLink();
            break;
        default:
            break;
        }
    }

    public void onClickCompose(View v)
    {
        updateMode(MODE_COMPOSE);
    }

    public void onClickCamera(View v)
    {
        updateMode(MODE_IMAGE);
    }

    public void onClickGallery(View v)
    {
        updateMode(MODE_IMAGE);
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
        case MODE_IMAGE:
        case MODE_MYPLACES:
        case MODE_ADDLINK:
            v = findViewById(allModeViews[mode]);
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
            postMode = MODE_COMPOSE;
            break;
        }
    }

    private void sendCompose()
    {
        Intent intent = new Intent(this, ClientService.class);
        if (bindService(intent, conn, BIND_AUTO_CREATE)) {
        }
    }

    private void sendImage()
    {
    }

    private void sendPlace()
    {
    }

    private void sendLink()
    {
    }
}
