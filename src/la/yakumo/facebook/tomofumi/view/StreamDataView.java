package la.yakumo.facebook.tomofumi.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.StreamItemActivity;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.callback.*;

public class StreamDataView
    extends LinearLayout
{
    private static final String TAG = Constants.LOG_TAG;

    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private MovementMethod movementmethod = LinkMovementMethod.getInstance();
    private Resources resources;
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;
    private TextAppearanceSpan summarySpan;
    private TextView messageView;
    private TextView summaryView;
    private TextView descriptionView;
    private NetImageView streamIconView;
    private NetImageView summaryIconView;
    private NetImageView appIconView;
    private View summaryBaseView;
    private PostItemView commentView;
    private PostItemView likeView;

    private Database.StreamListItem listItem;
    private IClientService service = null;
    private Handler handler = new Handler();

    private ILoginCallback loginListener = new ILoginCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);

            try {
                service.unregisterLoginCallback(loginListener);
                service.registerLikeCallback(likeListener);
                if (/* listItem.like_posted */ listItem.like.enable_item) {
                    service.unregistStreamLike(listItem.post_id);
                }
                else {
                    service.registStreamLike(listItem.post_id);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void loginFailed(String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
            try {
                service.unregisterLoginCallback(loginListener);
                getContext().unbindService(conn);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
    };

    private ILikeCallback likeListener = new ILikeCallback.Stub() {
        public void registerLike(final String post_id)
        {
            Log.i(TAG, "(1)registerLike:"+post_id+","+listItem.post_id);
            if (post_id.equals(listItem.post_id)) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        listItem.like.state_changing = true;
                        updateData();
                    }
                });
            }
        }

        public void registedLike(final String post_id)
        {
            Log.i(TAG, "(1)registedLike:"+post_id+","+listItem.post_id);
            if (post_id.equals(listItem.post_id)) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        listItem.like.state_changing = false;
                        listItem.like.count++;
                        listItem.like.enable_item = true;
                        updateData();
                    }
                });
                try {
                    service.unregisterLikeCallback(likeListener);
                    getContext().unbindService(conn);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
        }

        public void unregisterLike(final String post_id)
        {
            Log.i(TAG, "(1)unregisterLike:"+post_id+","+listItem.post_id);
            if (post_id.equals(listItem.post_id)) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        listItem.like.state_changing = true;
                        updateData();
                    }
                });
            }
        }

        public void unregistedLike(final String post_id)
        {
            Log.i(TAG, "(1)unregistedLike:"+post_id+","+listItem.post_id);
            if (post_id.equals(listItem.post_id)) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        listItem.like.state_changing = false;
                        listItem.like.count--;
                        listItem.like.enable_item = false;
                        updateData();
                    }
                });
                try {
                    service.unregisterLikeCallback(likeListener);
                    getContext().unbindService(conn);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
        }

        public void commentLikeDataReaded(
            String comment_post_id,
            int likes,
            boolean liked)
        {
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerLoginCallback(loginListener);
                service.login(Constants.SESSION_STREAM_LIST);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    private void privateInit(Context context)
    {
        resources = context.getResources();

        messageSpan = new TextAppearanceSpan(context, R.style.StreamMessage);
        usernameSpan = new TextAppearanceSpan(context, R.style.StreamMessageUser);
        summarySpan = new TextAppearanceSpan(context, R.style.StreamSummary);

        addView(
            View.inflate(context, R.layout.stream_data, null),
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        messageView = (TextView) findViewById(R.id.message);
        summaryView = (TextView) findViewById(R.id.summary);
        descriptionView = (TextView) findViewById(R.id.description);
        streamIconView = (NetImageView) findViewById(R.id.stream_icon);
        summaryIconView = (NetImageView) findViewById(R.id.summary_icon);
        appIconView = (NetImageView) findViewById(R.id.app_icon);
        summaryBaseView = findViewById(R.id.summary_base);
        commentView = (PostItemView) findViewById(R.id.comment_item);
        likeView = (PostItemView) findViewById(R.id.like_item);

        if (!Constants.IS_FREE) {
            if (null != messageView) {
                messageView.setMovementMethod(movementmethod);
            }
            if (null != summaryView) {
                summaryView.setMovementMethod(movementmethod);
            }
        }

        if (null != likeView) {
            likeView.setOnClickListener(new OnClickListener() {
                public void onClick (View v)
                {
                    Context context = getContext();
                    listItem.like.state_changing = true;
                    updateData();
                    Intent intent = new Intent(context, ClientService.class);
                    if (context.bindService(
                            intent,
                            conn,
                            Context.BIND_AUTO_CREATE)) {
                    }
                }
            });
        }
        if (null != commentView) {
            commentView.setOnClickListener(new OnClickListener() {
                public void onClick (View v)
                {
                    Context context = getContext();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClass(context, StreamItemActivity.class);
                    intent.putExtra("post_id", listItem.post_id);
                    context.startActivity(intent);
                }
            });
        }
    }

    public StreamDataView(Context context)
    {
        super(context);
        privateInit(context);
    }

    public StreamDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        privateInit(context);
    }

    public void hideComments()
    {
        if (null != commentView) {
            commentView.setVisibility(View.GONE);
        }
    }

    public void put(Database.StreamListItem li)
    {
        listItem = li;
        updateData();
    }

    private void updateData()
    {
        if (null != streamIconView) {
            streamIconView.setImageResource(R.drawable.clear_image);
        }
        if (null != summaryIconView) {
            summaryIconView.setImageResource(R.drawable.clear_image);
        }
        if (null != appIconView) {
            appIconView.setVisibility(View.GONE);
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
        if (null != summaryView) {
            String name = listItem.attachment_name;
            String caption = listItem.attachment_caption;
            String link = listItem.attachment_link;
            String icon = listItem.attachment_icon;
            String image = listItem.attachment_image;
            String msg = "";
            String sep = "";
            if (null != name && name.length() > 0) {
                msg = msg + sep + name;
                sep = "\n";
            }
            if (null != caption && caption.length() > 0) {
                msg = msg + sep + caption;
                sep = "\n";
            }
            if (msg.length() > 0) {
                Spannable spannable = factory.newSpannable(msg);
                spannable.setSpan(
                    summarySpan, 0, msg.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (null != name && name.length() > 0 &&
                    null != link && link.length() > 0) {
                    try {
                        URL url = new URL(link);
                        URLSpan s = new URLSpan(link);
                        spannable.setSpan(
                            s, 0, name.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (MalformedURLException e) {
                        Log.i(TAG, "MalformedURLException", e);
                    }
                }
                if (null != icon && null != appIconView) {
                    appIconView.setVisibility(View.VISIBLE);
                    appIconView.setImageURI(Uri.parse(icon));
                }
                if (null != summaryIconView) {
                    if (null != image && image.length() > 0){
                        summaryIconView.setVisibility(View.VISIBLE);
                        summaryIconView.setImageURI(Uri.parse(image));
                    }
                    else {
                        summaryIconView.setVisibility(View.GONE);
                    }
                }
                summaryBaseView.setVisibility(View.VISIBLE);
                summaryView.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            else {
                summaryBaseView.setVisibility(View.GONE);
            }
        }
        if (null != descriptionView) {
            if (null != listItem.description &&
                listItem.description.length() > 0) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(listItem.description);
            }
            else {
                descriptionView.setVisibility(View.GONE);
            }
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
        if (null != commentView) {
            commentView.setPostItem(listItem.comment);
        }
        if (null != likeView) {
            likeView.setPostItem(listItem.like);
        }

        if (listItem.updated) {
            setBackgroundResource(
                R.color.stream_updated_background_color);
        }
        else {
            setBackgroundResource(
                R.color.stream_no_updated_background_color);
        }
    }
}
