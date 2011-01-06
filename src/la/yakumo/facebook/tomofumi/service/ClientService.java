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
import la.yakumo.facebook.tomofumi.service.callback.*;
import la.yakumo.facebook.tomofumi.service.register.*;
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

    private RemoteCallbackList<ILoginCallback> loginListeners =
        new RemoteCallbackList<ILoginCallback>();
    private RemoteCallbackList<IStreamCallback> streamListeners =
        new RemoteCallbackList<IStreamCallback>();
    private RemoteCallbackList<ICommentCallback> commentListeners =
        new RemoteCallbackList<ICommentCallback>();
    private RemoteCallbackList<ILikeCallback> likeListeners =
        new RemoteCallbackList<ILikeCallback>();

    private static final int MSG_LOGIN = 1;
    private static final int MSG_UPDATE_STREAM = 2;
    private static final int MSG_UPDATE_COMMENT = 3;
    private static final int MSG_UPDATE_LIKE = 4;
    private static final int MSG_ADD_STREAM = 5;
    private static final int MSG_ADD_COMMENT = 6;
    private static final int MSG_ADD_STREAM_LIKE = 7;
    private static final int MSG_ADD_COMMENT_LIKE = 8;
    private static final int MSG_ADD_LINK = 9;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            String[] strs;
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
            case MSG_ADD_COMMENT:
                strs = (String[])msg.obj;
                addComment(strs[0], strs[1]);
                break;
            case MSG_ADD_STREAM_LIKE:
                addStreamLike((String)msg.obj);
                break;
            case MSG_ADD_COMMENT_LIKE:
                addCommentLike((String)msg.obj, msg.arg1);
                break;
            case MSG_ADD_LINK:
                strs = (String[])msg.obj;
                addLink(strs[0], strs[1], strs[2]);
                break;
            default:
                break;
            }
        }
    };

    private final IClientService.Stub stub =
        new IClientService.Stub()
        {
            public void registerLoginCallback(ILoginCallback callback)
            {
                Log.i(TAG, "registerLoginCallback:"+callback);
                loginListeners.register(callback);
            }

            public void unregisterLoginCallback(ILoginCallback callback)
            {
                Log.i(TAG, "unregisterLoginCallback:"+callback);
                loginListeners.unregister(callback);
            }

            public void registerStreamCallback(IStreamCallback callback)
            {
                Log.i(TAG, "registerStreamCallback:"+callback);
                streamListeners.register(callback);
            }

            public void unregisterStreamCallback(IStreamCallback callback)
            {
                Log.i(TAG, "unregisterStreamCallback:"+callback);
                streamListeners.unregister(callback);
            }

            public void registerCommentCallback(ICommentCallback callback)
            {
                Log.i(TAG, "registerCommentCallback:"+callback);
                commentListeners.register(callback);
            }

            public void unregisterCommentCallback(ICommentCallback callback)
            {
                Log.i(TAG, "unregisterCommentCallback:"+callback);
                commentListeners.unregister(callback);
            }

            public void registerLikeCallback(ILikeCallback callback)
            {
                Log.i(TAG, "registerLikeCallback:"+callback);
                likeListeners.register(callback);
            }

            public void unregisterLikeCallback(ILikeCallback callback)
            {
                Log.i(TAG, "unregisterLikeCallback:"+callback);
                likeListeners.unregister(callback);
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
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_STREAM;
                    msg.obj = new String(text);
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int addComment(String post_id, String text)
            throws RemoteException
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_COMMENT;
                    msg.obj = new String[] {post_id, text};
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int toggleStreamLike(String post_id)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_STREAM_LIKE;
                    msg.obj = new String(post_id);
                    msg.arg1 = -1;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int registStreamLike(String post_id)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_STREAM_LIKE;
                    msg.obj = new String(post_id);
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int unregistStreamLike(String post_id)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_STREAM_LIKE;
                    msg.obj = new String(post_id);
                    msg.arg1 = 0;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int toggleCommentLike(String post_id)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_COMMENT_LIKE;
                    msg.obj = new String(post_id);
                    msg.arg1 = -1;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int registCommentLike(String post_id)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_COMMENT_LIKE;
                    msg.obj = new String(post_id);
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int unregistCommentLike(String post_id)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_COMMENT_LIKE;
                    msg.obj = new String(post_id);
                    msg.arg1 = 0;
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
                return RESULT_ERROR;
            }

            public int addLink(String text, String linkUrl, String imageUrl)
            {
                if (Facebook.getInstance(ClientService.this).loginCheck()) {
                    Message msg = new Message();
                    msg.what = MSG_ADD_LINK;
                    msg.obj = new String[] {text, linkUrl, imageUrl};
                    handler.sendMessage(msg);
                    return RESULT_OK;
                }
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
        ImageDownloader.startDownloader(
            this,
            new ImageDownloader.OnDownloadCallback() {
                public void downloaded(String url)
                {
                }
            });
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
        synchronized (loginListeners) {
            int numListener = loginListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    loginListeners.getBroadcastItem(i).loggedIn(uid);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            loginListeners.finishBroadcast();
        }
    }

    private void sendLoginFail(int sessionID, String reason)
    {
        synchronized (loginListeners) {
            int numListener = loginListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    loginListeners.getBroadcastItem(i).loginFailed(reason);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            loginListeners.finishBroadcast();
        }
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
            new Updator.OnEventCallback()
            {
                public void onStartEvent(Bundle info)
                {
                }

                public void onProgress(Bundle info)
                {
                }

                public void onFinishEvent(Bundle info, boolean isCancel)
                {
                    Log.i(TAG, "finish update stream");
                    synchronized (streamListeners) {
                        int numListener = streamListeners.beginBroadcast();
                        for (int i = 0 ; i < numListener ; i++) {
                            IStreamCallback cb =
                                streamListeners.getBroadcastItem(i);
                            try {
                                cb.updatedStream(null);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException", e);
                            }
                        }
                        streamListeners.finishBroadcast();
                    }
                }
            }).execute();
    }

    public void updateComment(final String post_id)
    {
        new CommentsUpdator(
            ClientService.this,
            Facebook.getInstance(this).getUserID(),
            post_id,
            new Updator.OnEventCallback()
            {
                public void onStartEvent(Bundle info)
                {
                }

                public void onProgress(Bundle info)
                {
                }

                public void onFinishEvent(Bundle info, boolean isCancel)
                {
                    Log.i(TAG, "finish update stream");
                    if (info.containsKey("post_id")) {
                        synchronized (commentListeners) {
                            int numListener = commentListeners.beginBroadcast();
                            for (int i = 0 ; i < numListener ; i++) {
                                ICommentCallback cb =
                                    commentListeners.getBroadcastItem(i);
                                try {
                                    cb.updatedComment(
                                        info.getString("post_id"),
                                        null);
                                } catch (RemoteException e) {
                                    Log.e(TAG, "RemoteException", e);
                                }
                            }
                            commentListeners.finishBroadcast();
                        }
                    }
                    else {
                        synchronized (likeListeners) {
                            int numListener = likeListeners.beginBroadcast();
                            for (int i = 0 ; i < numListener ; i++) {
                                ILikeCallback cb =
                                    likeListeners.getBroadcastItem(i);
                                try {
                                    cb.likeDataUpdated(
                                        info.getString("comment_post_id"),
                                        info.getInt("likes"),
                                        info.getBoolean("liked"));
                                } catch (RemoteException e) {
                                    Log.e(TAG, "RemoteException", e);
                                }
                            }
                            likeListeners.finishBroadcast();
                        }
                    }
                }
            }).execute();
    }

    public void updateLike(String post_id)
    {
    }

    private void addedStream(String errMessage)
    {
        synchronized (streamListeners) {
            int numListener = streamListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    streamListeners.getBroadcastItem(i).addedStream(errMessage);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            streamListeners.finishBroadcast();
        }
    }

    public void addStream(String text)
    {
        Log.i(TAG, "addStream:"+text);
        new StatusRegister(this, text)
            .execute(new ItemRegister.OnSendFinish() {
                public void onStartSend(Bundle info)
                {
                }

                public void onSended(String reason, Bundle info)
                {
                    addedStream(reason);
                }
            });
    }

    public void startAddComment(String post_id)
    {
        synchronized (commentListeners) {
            int numListener = commentListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    commentListeners.getBroadcastItem(i).registerComment(post_id);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            commentListeners.finishBroadcast();
        }
    }

    public void addedComment(
        String post_id,
        String comment_post_id,
        String errMessage)
    {
        synchronized (commentListeners) {
            int numListener = commentListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    commentListeners.getBroadcastItem(i).registedComment(
                        post_id, comment_post_id, errMessage);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            commentListeners.finishBroadcast();
        }
    }

    public void addComment(final String post_id, final String text)
    {
        Log.i(TAG, "addComment:"+post_id+", "+text);
        new CommentRegister(this, post_id, text)
            .execute(new ItemRegister.OnSendFinish() {
                public void onStartSend(Bundle info)
                {
                    startAddComment(post_id);
                }

                public void onSended(String reason, Bundle info)
                {
                    addedComment(
                        post_id,
                        info.getString("comment_post_id"),
                        reason);
                }
            });
    }

    private void startAddStreamLike(String post_id, int mode)
    {
        synchronized (likeListeners) {
            int numListener = likeListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    if (0 == mode) {
                        likeListeners.getBroadcastItem(i).unregisterLike(post_id);
                    }
                    else {
                        likeListeners.getBroadcastItem(i).registerLike(post_id);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            likeListeners.finishBroadcast();
        }
    }

    private void addedStreamLike(String post_id, String errMessage, int mode)
    {
        synchronized (likeListeners) {
            int numListener = likeListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    if (0 == mode) {
                        likeListeners.getBroadcastItem(i).unregistedLike(post_id);
                    }
                    else {
                        likeListeners.getBroadcastItem(i).registedLike(post_id);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            likeListeners.finishBroadcast();
        }
    }

    private void reloadedLike(String post_id, int likes, boolean liked)
    {
        synchronized (likeListeners) {
            int numListener = likeListeners.beginBroadcast();
            for (int i = 0 ; i < numListener ; i++) {
                try {
                    likeListeners.getBroadcastItem(i).likeDataUpdated(
                        post_id, likes, liked);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
            likeListeners.finishBroadcast();
        }
    }

    public void addStreamLike(final String post_id)
    {
        Log.i(TAG, "addStreamLike:"+post_id);
        new StatusLikeRegister(this, post_id)
            .execute(new ItemRegister.OnSendFinish() {
                public void onStartSend(Bundle info)
                {
                    startAddStreamLike(post_id, info.getInt("like_posted"));
                }

                public void onSended(String reason, Bundle info)
                {
                    addedStreamLike(
                        post_id,
                        reason,
                        info.getInt("like_posted"));
                    reloadedLike(
                        post_id,
                        info.getInt("like_count"),
                        (info.getInt("like_posted") != 0)? true: false);
                }
            });
    }

    public void addCommentLike(final String post_id, final int add_mode)
    {
        Log.i(TAG, "addCommentLike:"+post_id);
        new CommentLikeRegister(
            this,
            post_id,
            add_mode,
            Facebook.getInstance(this).getUserID())
            .execute(new ItemRegister.OnSendFinish() {
                public void onStartSend(Bundle info)
                {
                    startAddStreamLike(post_id, info.getInt("like_posted"));
                }

                public void onSended(String reason, Bundle info)
                {
                    addedStreamLike(
                        post_id,
                        reason,
                        info.getInt("like_posted"));
                    reloadedLike(
                        post_id,
                        info.getInt("likes"),
                        info.getBoolean("liked"));
                }
            });
    }

    public void addLink(final String text, final String link, final String img)
    {
        Log.i(TAG, "addLink:"+text+", "+link+", "+img);
        new LinkRegister(
            this,
            text,
            link,
            img)
            .execute(new ItemRegister.OnSendFinish() {
                public void onStartSend(Bundle info)
                {
                }

                public void onSended(String reason, Bundle info)
                {
                    addedStream(reason);
                }
            });
    }
}
