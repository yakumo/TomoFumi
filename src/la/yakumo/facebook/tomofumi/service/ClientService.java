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
    public static final String EXTRA_LOGIN_SESSION_ID =
        "la.yakumo.facebook.tomofumi.LOGIN_SESSIONID";

    private RemoteCallbackList<IClientServiceCallback> listeners =
        new RemoteCallbackList<IClientServiceCallback>();

    private static final int MSG_LOGIN = 1;
    private static final int MSG_UPDATE_STREAM = 2;
    private static final int MSG_UPDATE_COMMENT = 3;
    private static final int MSG_UPDATE_LIKE = 4;
    private static final int MSG_ADD_STREAM = 5;
    private static final int MSG_ADD_COMMENT = 6;
    private static final int MSG_ADD_STREAM_LIKE = 7;
    private static final int MSG_ADD_COMMENT_LIKE = 8;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            case MSG_LOGIN:
                login(msg.arg1);
                break;
            case MSG_UPDATE_STREAM:
                updateStream();
                break;
            case MSG_UPDATE_COMMENT:
                updateComment((String)msg.obj);
                break;
            case MSG_ADD_STREAM:
                addStream((String)msg.obj);
                break;
            case MSG_ADD_STREAM_LIKE:
                addStreamLike((String)msg.obj);
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

            public void login(int sessionID)
            {
                Message msg = new Message();
                msg.what = MSG_LOGIN;
                msg.arg1 = sessionID;
                handler.sendMessage(msg);
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
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_UPDATE_COMMENT;
                    msg.obj = post_id;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_DISPLAY_LOGIN;
            }

            public int updateLike(String post_id)
            throws RemoteException
            {
                return RESULT_ERROR;
            }

            public int addStream(String text)
            throws RemoteException
            {
                Message msg = new Message();
                msg.what = MSG_ADD_STREAM;
                msg.obj = new String(text);
                handler.sendMessage(msg);
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
                Message msg = new Message();
                msg.what = MSG_ADD_STREAM_LIKE;
                msg.obj = new String(post_id);
                handler.sendMessage(msg);
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
                int sessionID = intent.getIntExtra(
                    EXTRA_LOGIN_SESSION_ID,
                    Constants.SESSION_UNKNOWN);
                if (ACTION_LOGIN_SUCCESS.equals(action)) {
                    sendLoggedIn(sessionID);
                }
                if (ACTION_LOGIN_FAIL.equals(action)) {
                    sendLoginFail(
                        sessionID,
                        intent.getStringExtra(EXTRA_LOGIN_REASON));
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

    private void sendLoggedIn(int sessionID)
    {
        String uid = Facebook.getInstance(this).getUserID();
        int numListener = listeners.beginBroadcast();
        for (int i = 0 ; i < numListener ; i++) {
            try {
                listeners.getBroadcastItem(i).loggedIn(sessionID, uid);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
        listeners.finishBroadcast();
    }

    private void sendLoginFail(int sessionID, String reason)
    {
        int numListener = listeners.beginBroadcast();
        for (int i = 0 ; i < numListener ; i++) {
            try {
                listeners.getBroadcastItem(i).loginFailed(sessionID, reason);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
        listeners.finishBroadcast();
    }

    public void login(int sessionID)
    {
        if (Facebook.getInstance(ClientService.this).loginCheck()) {
            sendLoggedIn(sessionID);
        }
    }

    public void updateStream()
    {
        new StreamUpdator(
            ClientService.this,
            handler,
            new Updator.OnProgress()
            {
                public void onProgress(int now, int max, String msg)
                {
                    int numListener = listeners.beginBroadcast();
                    for (int i = 0 ; i < numListener ; i++) {
                        try {
                            listeners.getBroadcastItem(i).updateProgress(
                                now, max, msg);
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException", e);
                        }
                    }
                    listeners.finishBroadcast();
                }
            }).execute(
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

    public void updateComment(final String post_id)
    {
        new CommentsUpdator(
            ClientService.this,
            post_id,
            handler,
            new Updator.OnProgress()
            {
                public void onProgress(int now, int max, String msg)
                {
                    int numListener = listeners.beginBroadcast();
                    for (int i = 0 ; i < numListener ; i++) {
                        try {
                            listeners.getBroadcastItem(i).updateProgress(
                                now, max, msg);
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException", e);
                        }
                    }
                    listeners.finishBroadcast();
                }
            }).execute(
                new Runnable()
                {
                    public void run()
                    {
                        Log.i(TAG, "finish update comment");
                        int numListener = listeners.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                listeners.getBroadcastItem(i).updatedComment(
                                    post_id, null);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        listeners.finishBroadcast();
                    }
                });
    }

    public void updateLike(String post_id)
    {
    }

    private void addedStream(String errMessage)
    {
        int numListener = listeners.beginBroadcast();
        for (int i = 0 ; i < numListener ; i++) {
            try {
                listeners.getBroadcastItem(i).addedStream(errMessage);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
        listeners.finishBroadcast();
    }

    public void addStream(String text)
    {
        Log.i(TAG, "addStream:"+text);
        new StatusRegister(this, text)
            .execute(new ItemRegister.OnSendFinish() {
                public void onSendSuccess()
                {
                    addedStream(null);
                }

                public void onSendFail(String reason)
                {
                    addedStream(reason);
                }
            });
    }

    public void addComment(String post_id, String text)
    {
    }

    public void addedStreamLike(String post_id, String errMessage)
    {
        int numListener = listeners.beginBroadcast();
        for (int i = 0 ; i < numListener ; i++) {
            try {
                listeners.getBroadcastItem(i).addedLike(post_id, errMessage);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
        listeners.finishBroadcast();
    }

    public void addStreamLike(final String post_id)
    {
        Log.i(TAG, "addStreamLike:"+post_id);
        new StatusLikeRegister(this, post_id)
            .execute(new ItemRegister.OnSendFinish() {
                public void onSendSuccess()
                {
                    addedStreamLike(post_id, null);
                }

                public void onSendFail(String reason)
                {
                    addedStreamLike(post_id, reason);
                }
            });
    }

    public void addCommentLike(String post_id)
    {
    }
}
