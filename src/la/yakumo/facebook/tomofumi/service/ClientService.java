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
            }

            public void addLink(String text, String linkUrl, String imageUrl)
            {
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

    private void reloadStream(boolean isClear)
    {
        new StreamUpdator(this, handler, isClear)
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

    /*
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

      public void shareStream(String post_id)
      {
      Log.i(TAG, "shareStream:"+post_id);
      new GetURL(
      this,
      post_id,
      new Command.OnResult()
      {
      public void onResult(Bundle info)
      {
      if (info.containsKey("url")) {
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setClass(
      ClientService.this,
      TextPostActivity.class);
      intent.putExtra(
      Intent.EXTRA_TEXT,
      (String) info.get("url"));
      ClientService.this.startActivity(intent);
      }
      }
      }).execute();
      }
    */
}
