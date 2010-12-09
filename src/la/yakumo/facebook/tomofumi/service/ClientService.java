package la.yakumo.facebook.tomofumi.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;

public class ClientService
    extends Service
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = -1;
    public static final int RESULT_DISPLAY_LOGIN = -2;

    public static final String ACTION_LOGIN_SUCCESS =
        "la.yakumo.facebook.tomofumi.LOGIN_SUCCESS";
    public static final String ACTION_LOGIN_FAIL =
        "la.yakumo.facebook.tomofumi.LOGIN_FAIL";
    public static final String EXTRA_LOGIN_REASON =
        "la.yakumo.facebook.tomofumi.LOGIN_REASON";

    private RemoteCallbackList<IClientServiceCallback> listeners =
        new RemoteCallbackList<IClientServiceCallback>();

    private static final int MSG_LOGIN = 1;
    private static final int MSG_UPDATE_STREAM = 2;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            case MSG_LOGIN:
                login();
                break;
            case MSG_UPDATE_STREAM:
                updateStream();
                break;
            default:
                break;
            }
        }
    };

    private final IClientService.Stub stub =
        new IClientService.Stub()
        {
            public void registerCallback(IClientServiceCallback callback)
            throws RemoteException
            {
                Log.i(TAG, "registerCallback:"+callback);
                listeners.register(callback);
            }

            public void unregisterCallback(IClientServiceCallback callback)
            throws RemoteException
            {
                Log.i(TAG, "unregisterCallback:"+callback);
                listeners.unregister(callback);
            }

            public void login()
            {
                handler.sendEmptyMessage(MSG_LOGIN);
            }

            public int updateStream()
            throws RemoteException
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    handler.sendEmptyMessage(MSG_UPDATE_STREAM);
                    return RESULT_OK;
                }
                return RESULT_DISPLAY_LOGIN;
            }

            public int updateComment(String post_id)
            throws RemoteException
            {
                return RESULT_ERROR;
            }

            public int updateLike(String post_id)
            throws RemoteException
            {
                return RESULT_ERROR;
            }

            public int addStream(String text)
            throws RemoteException
            {
                return RESULT_ERROR;
            }

            public int addComment(String post_id, String text)
            throws RemoteException
            {
                return RESULT_ERROR;
            }

            public int addStreamLike(String post_id)
            throws RemoteException
            {
                return RESULT_ERROR;
            }

            public int addCommentLike(String post_id)
            throws RemoteException
            {
                return RESULT_ERROR;
            }
        };

    private BroadcastReceiver loginStatusReceiver =
        new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if (ACTION_LOGIN_SUCCESS.equals(action)) {
                    sendLoggedIn();
                }
                if (ACTION_LOGIN_FAIL.equals(action)) {
                    sendLoginFail(intent.getStringExtra(EXTRA_LOGIN_REASON));
                }
            }
        };

    public void onCreate()
    {
        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_LOGIN_SUCCESS);
        f.addAction(ACTION_LOGIN_FAIL);
        registerReceiver(loginStatusReceiver, f);
    }

    public void onDestroy()
    {
        unregisterReceiver(loginStatusReceiver);
    }

    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "ClientService#onBind");
        return stub;
    }

    private void sendLoggedIn()
    {
        String uid = Facebook.getInstance(this).getUserID();
        int numListener = listeners.beginBroadcast();
        for (int i = 0 ; i < numListener ; i++) {
            try {
                listeners.getBroadcastItem(i).loggedIn(uid);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
        listeners.finishBroadcast();
    }

    private void sendLoginFail(String reason)
    {
        int numListener = listeners.beginBroadcast();
        for (int i = 0 ; i < numListener ; i++) {
            try {
                listeners.getBroadcastItem(i).loginFailed(reason);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
        listeners.finishBroadcast();
    }

    public void login()
    {
        if (Facebook.getInstance(ClientService.this).loginCheck()) {
            sendLoggedIn();
        }
    }

    public void updateStream()
    {
        new StreamUpdator(
            ClientService.this,
            handler).execute(
                new Runnable()
                {
                    public void run()
                    {
                        Log.i(TAG, "finish update stream");
                        int numListener = listeners.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                listeners.getBroadcastItem(i).updatedStream(null);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        listeners.finishBroadcast();
                    }
                });
    }

    public void updateComment(String post_id)
    {
    }

    public void updateLike(String post_id)
    {
    }

    public void addStream(String text)
    {
    }

    public void addComment(String post_id, String text)
    {
    }

    public void addStreamLike(String post_id)
    {
    }

    public void addCommentLike(String post_id)
    {
    }
}
