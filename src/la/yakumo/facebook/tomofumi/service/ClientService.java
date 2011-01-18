package la.yakumo.facebook.tomofumi.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.callback.*;
import la.yakumo.facebook.tomofumi.service.updator.*;

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

    private Handler handler = new Handler();

    private RemoteCallbackList<ICommandCallback> callbacks =
        new RemoteCallbackList<ICommandCallback>();

    private final IClientService.Stub stub =
        new IClientService.Stub()
        {
            public void registerCallback(ICommandCallback callback)
            {
                Log.i(TAG, "registerLoginCallback:"+callback);
                synchronized (callbacks) {
                    callbacks.register(callback);
                }
            }

            public void unregisterCallback(ICommandCallback callback)
            {
                Log.i(TAG, "unregisterLoginCallback:"+callback);
                synchronized (callbacks) {
                    callbacks.unregister(callback);
                }
            }

            public void reloadStream(boolean isClear)
            {
                ClientService.this.reloadStream(isClear);
            }

            public void reloadComment(String post_id)
            {
            }

            public void reloadLike(String post_id)
            {
            }

            public void addStreamMessage(String text)
            {
                ClientService.this.addStreamMessage(text);
            }

            public void addLink(String text, String linkUrl, String imageUrl)
            {
            }

            public void setStreamLike(String post_id)
            {
                ClientService.this.setStreamLike(post_id);
            }

            public void resetStreamLike(String post_id)
            {
                ClientService.this.resetStreamLike(post_id);
            }

            public void setCommentLike(String post_id)
            {
                ClientService.this.setCommentLike(post_id);
            }

            public void resetCommentLike(String post_id)
            {
                ClientService.this.resetCommentLike(post_id);
            }
        };

    public void onCreate()
    {
        ImageDownloader.createInstance(this);
    }

    public void onDestroy()
    {
        //unregisterReceiver(loginStatusReceiver);
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand:"+intent);
        if (null != intent) {
            String action = intent.getAction();
            if (ImageDownloader.IMAGE_DOWNLOADED.equals(action)) {
                String url = intent.getStringExtra("url");
                Log.i(TAG, "image downloaded:"+url);
                synchronized (callbacks) {
                    int numListener = callbacks.beginBroadcast();
                    for (int i = 0 ; i < numListener ; i++) {
                        try {
                            callbacks.getBroadcastItem(i).readedImage(url);
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException", e);
                        }
                    }
                    callbacks.finishBroadcast();
                }
            }
            if (ImageDownloader.IMAGE_REQUEST.equals(action)) {
                String url = intent.getStringExtra("url");
                Log.i(TAG, "image request:"+url);
                ImageDownloader.getInstance().requestUrl(url);
            }
        }
        return START_STICKY;
    }

    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "ClientService#onBind");
        Facebook.getInstance(this).loginCheck();
        return stub;
    }

    void reloadStream(boolean isClear)
    {
        new StreamUpdator(this, isClear)
            .execute(new Updator.OnStatusChange() {
                public void onStart()
                {
                    synchronized (callbacks) {
                        int numListener = callbacks.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                callbacks.getBroadcastItem(i)
                                    .reloadStreamStart();
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        callbacks.finishBroadcast();
                    }
                }

                public void onFinish(Bundle info)
                {
                    String errMsg = null;
                    if (info.containsKey("error")) {
                        errMsg = info.getString("error");
                    }
                    synchronized (callbacks) {
                        int numListener = callbacks.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                callbacks.getBroadcastItem(i)
                                    .reloadStreamFinish(errMsg);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        callbacks.finishBroadcast();
                    }
                }
            });
    }

    void addStreamMessage(String text)
    {
        new AddStreamMessageUpdator(this, text)
            .execute(new Updator.OnStatusChange() {
                public void onStart()
                {
                }

                public void onFinish(Bundle info)
                {
                    String errMsg = null;
                    if (info.containsKey("error")) {
                        errMsg = info.getString("error");
                    }
                    String post_id = null;
                    if (info.containsKey("post_id")) {
                        post_id = info.getString("post_id");
                    }
                    synchronized (callbacks) {
                        int numListener = callbacks.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                callbacks.getBroadcastItem(i)
                                    .registedStreamMessage(post_id, errMsg);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        callbacks.finishBroadcast();
                    }
                }
            });
    }

    void changeStreamLike(final String post_id, boolean flag)
    {
        new StreamLikeUpdator(this, post_id, flag)
            .execute(new Updator.OnStatusChange() {
                public void onStart()
                {
                    synchronized (callbacks) {
                        int numListener = callbacks.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                callbacks.getBroadcastItem(i)
                                    .reloadedStreamLikeStart(post_id);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        callbacks.finishBroadcast();
                    }
                }

                public void onFinish(Bundle info)
                {
                    String errMsg = null;
                    if (info.containsKey("error")) {
                        errMsg = info.getString("error");
                    }
                    synchronized (callbacks) {
                        int numListener = callbacks.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            try {
                                callbacks.getBroadcastItem(i)
                                    .reloadedStreamLikeFinish(post_id, errMsg);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        callbacks.finishBroadcast();
                    }
                }
            });
    }

    void setStreamLike(String post_id)
    {
        changeStreamLike(post_id, true);
    }

    void resetStreamLike(String post_id)
    {
        changeStreamLike(post_id, false);
    }

    void setCommentLike(String post_id)
    {
    }

    void resetCommentLike(String post_id)
    {
    }
}
