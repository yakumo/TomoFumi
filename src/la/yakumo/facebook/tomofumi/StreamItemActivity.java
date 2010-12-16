package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.IClientServiceCallback;

public class StreamItemActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private static final int ERROR_TOAST_DISPLAY_DURATION = 1500;

    private IClientService service = null;

    private String postId;
    private ProgressDialog progress = null;
    private Handler handler = new Handler();
    private Resources resources;

    private IClientServiceCallback listener = new IClientServiceCallback.Stub() {
        public void loggedIn(int sessionID, String userID)
        {
            Log.i(TAG, "loggedIn:"+sessionID+","+userID);

            if (null != service &&
                Constants.SESSION_UPDATE_COMMENTS == sessionID) {
                requestUpdateComments(postId);
            }
        }

        public void loginFailed(int sessionID, String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
        }

        public void updatedStream(String errorMessage)
        {
        }

        public void updatedComment(String post_id, String errorMessage)
        {
            if (null != progress) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        progress.dismiss();
                        progress = null;
                    }
                });
            }
        }

        public void updatedLike(String post_id, String errorMessage)
        {
        }

        public void updateProgress(int now, int max, String msg)
        {
            if (null != progress) {
                final int fNow = now;
                final int fMax = max;
                final String fMsg = msg;
                handler.post(new Runnable() {
                    public void run()
                    {
                        progress.setMessage(fMsg);
                        progress.setMax(fMax);
                        progress.setProgress(fNow);
                    }
                });
            }
        }

        public void addedStream(String errorMessage)
        {
        }

        public void addedComment(String post_id, String errorMessage)
        {
        }

        public void addedLike(final String post_id, String errorMessage)
        {
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerCallback(listener);
                service.login(Constants.SESSION_UPDATE_COMMENTS);
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

        resources = getResources();

        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");
        if (null == postId) {
            Toast.makeText(
                this,
                R.string.error_unknown_postid,
                ERROR_TOAST_DISPLAY_DURATION);
            finish();
            return;
        }

        intent = new Intent(this, ClientService.class);
        if (bindService(intent, conn, BIND_AUTO_CREATE)) {
        }
    }

    private void requestUpdateComments(final String postId)
    {
        Log.i(TAG, "requestUpdateComments");
        handler.post(new Runnable() {
            public void run()
            {
                progress =
                    ProgressDialog.show(
                        StreamItemActivity.this,
                        resources.getString(
                            R.string.progress_stream_update_title),
                        resources.getString(
                            R.string.progress_updating_comment_message),
                        false,
                        false);
            }
        });
        try {
            service.updateComment(postId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }
}
