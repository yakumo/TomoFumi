package la.yakumo.facebook.tomofumi.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.callback.*;

public class CommentDataView
    extends ItemDataView
{
    private static final String TAG = Constants.LOG_TAG;

    private IClientService service = null;
    private Handler handler = new Handler();
    private Database.CommentListItem listItem;

    private ILoginCallback loginListener = new ILoginCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);
            try {
                service.unregisterLoginCallback(loginListener);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
            registerCommentLike();
        }

        public void loginFailed(String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
        }
    };

    private ILikeCallback likeListener = new ILikeCallback.Stub() {
        public void registerLike(final String post_id)
        {
            Log.i(TAG, "(3)registerLike:"+post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    listItem.like.state_changing = true;
                    updateData();
                }
            });
        }

        public void registedLike(final String post_id)
        {
            Log.i(TAG, "(3)registedLike:"+post_id);
        }

        public void unregisterLike(final String post_id)
        {
            Log.i(TAG, "(3)unregisterLike:"+post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    listItem.like.state_changing = true;
                    updateData();
                }
            });
        }

        public void unregistedLike(final String post_id)
        {
            Log.i(TAG, "(3)unregistedLike:"+post_id);
        }

        public void likeDataUpdated(
            final String post_id,
            final int likes,
            final boolean liked)
        {
            Log.i(TAG, "(3)likeDataUpdated:"+post_id+","+likes+","+liked);
            handler.post(new Runnable() {
                public void run()
                {
                    if (post_id.equals(listItem.post_id)) {
                        listItem.like.state_changing = false;
                        listItem.like.enable_item = liked;
                        listItem.like.count = likes;
                        updateData();
                    }
                }
            });
            try {
                service.unregisterLikeCallback(likeListener);
                getContext().unbindService(conn);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerLoginCallback(loginListener);
                service.registerLikeCallback(likeListener);
                service.login(Constants.SESSION_UPDATE_COMMENTS);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    public CommentDataView(Context context)
    {
        super(context);
    }

    public CommentDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    protected void onClickCommentView()
    {
        Log.i(TAG, "CommentDataView#onClickCommentView");
    }

    protected void onClickLikeView()
    {
        Log.i(TAG, "CommentDataView#onClickLikeView");

        Context context = getContext();
        Intent intent = new Intent(context, ClientService.class);
        if (context.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
        }
    }

    public void put(Object obj)
    {
        listItem = (Database.CommentListItem) obj;
        updateData();
    }

    void updateData()
    {
        if (null != streamIconView) {
            streamIconView.setImageResource(R.drawable.clear_image);
        }
        if (null != summaryView) {
            summaryView.setVisibility(View.GONE);
        }
        if (null != summaryIconView) {
            summaryIconView.setVisibility(View.GONE);
        }
        if (null != appIconView) {
            appIconView.setVisibility(View.GONE);
        }
        if (null != descriptionView) {
            descriptionView.setVisibility(View.GONE);
        }
        if (null != commentView) {
            commentView.setVisibility(View.GONE);
        }
        if (null != messageView) {
            String usr = listItem.name;
            String msg = listItem.message;
            String userUrl = listItem.profile_url;
            if (null == usr) {
                usr = "???";
            }
            String allMsg = usr + " " + msg;
            Spannable spannable = factory.newSpannable(allMsg);
            spannable.setSpan(
                messageSpan, 0, allMsg.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(
                usernameSpan, 0, usr.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (null != userUrl) {
                try {
                    URL url = new URL(userUrl);
                    URLSpan s = new URLSpan(userUrl);
                    spannable.setSpan(
                        s, 0, usr.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (MalformedURLException e) {
                }
            }
            messageView.setText(spannable, TextView.BufferType.SPANNABLE);
        }
        if (null != streamIconView) {
            if (null != listItem.pic_square) {
                streamIconView.setImageURI(Uri.parse(listItem.pic_square));
                streamIconView.setVisibility(View.VISIBLE);
            }
            else {
                streamIconView.setVisibility(View.GONE);
            }
        }
        if (null != likeView) {
            likeView.setPostItem(listItem.like);
        }
    }

    private void registerCommentLike()
    {
        try {
            if (null != service) {
                if (listItem.like.enable_item) {
                    service.unregistCommentLike(listItem.post_id);
                }
                else {
                    service.registCommentLike(listItem.post_id);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }
}
