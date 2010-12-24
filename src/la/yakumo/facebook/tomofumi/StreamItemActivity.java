package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.method.KeyListener;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import la.yakumo.facebook.tomofumi.adapter.CommentListAdapter;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.callback.*;
import la.yakumo.facebook.tomofumi.view.NetImageView;
import la.yakumo.facebook.tomofumi.view.StreamDataView;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamItemActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private static final int ERROR_TOAST_DISPLAY_DURATION = 1500;

    private IClientService service = null;

    private String userId = null;
    private String postId;
    private ProgressDialog progress = null;
    private Handler handler = new Handler();
    private Resources resources;
    private ListView commentListView;
    private Database db;
    private HashMap<String,View> likePosting = new HashMap<String,View>();
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;
    private TextAppearanceSpan summarySpan;
    private MovementMethod movementmethod =
        LinkMovementMethod.getInstance();

    private ILoginCallback loginListener = new ILoginCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);
            StreamItemActivity.this.userId = userID;

            CommentListAdapter ca = (CommentListAdapter)
                commentListView.getAdapter();
            ca.setUserId(userID);

            try {
                service.unregisterLoginCallback(loginListener);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
            requestUpdateComments(postId);
        }

        public void loginFailed(String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
        }
    };

    private ICommentCallback commentListener = new ICommentCallback.Stub() {
        public void updatedComment(String post_id, String errorMessage)
        {
            if (null != progress) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        progress.dismiss();
                        progress = null;

                        CommentListAdapter ca = (CommentListAdapter)
                            commentListView.getAdapter();
                        ca.reloadData(postId);
                    }
                });
            }
        }

        public void registerComment(
            String post_id)
        {
            Log.i(TAG, "registerComment:"+post_id);
        }

        public void registedComment(
            String post_id,
            String comment_post_id,
            String errMsg)
        {
            Log.i(TAG, "registedComment:"+post_id+","+comment_post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    TextView tv = (TextView) findViewById(R.id.comment_text);
                    if (null != tv) {
                        tv.setText("");
                    }
                }
            });
        }
    };

    private ILikeCallback likeListener = new ILikeCallback.Stub() {
        public void registerLike(final String post_id)
        {
            Log.i(TAG, "registerLike:"+post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    final TextView tv = (TextView) likePosting.get(post_id);
                    CommentListAdapter a =
                        (CommentListAdapter)commentListView.getAdapter();
                    if (null != tv && null != tv.getTag()) {
                        a.likeUpdating(((Integer)tv.getTag()).intValue(), tv);
                    }
                }
            });
        }

        public void registedLike(final String post_id)
        {
            Log.i(TAG, "registedLike:"+post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    TextView tv = (TextView) likePosting.get(post_id);
                    CommentListAdapter a =
                        (CommentListAdapter)commentListView.getAdapter();
                    if (null != tv && null != tv.getTag()) {
                        a.likeRegisted(((Integer)tv.getTag()).intValue(), tv);
                    }
                    likePosting.remove(post_id);
                }
            });
        }

        public void unregisterLike(final String post_id)
        {
            Log.i(TAG, "unregisterLike:"+post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    TextView tv = (TextView) likePosting.get(post_id);
                    CommentListAdapter a =
                        (CommentListAdapter)commentListView.getAdapter();
                    if (null != tv && null != tv.getTag()) {
                        a.likeUpdating(((Integer)tv.getTag()).intValue(), tv);
                    }
                }
            });
        }

        public void unregistedLike(final String post_id)
        {
            Log.i(TAG, "unregistedLike:"+post_id);
            handler.post(new Runnable() {
                public void run()
                {
                    TextView tv = (TextView) likePosting.get(post_id);
                    CommentListAdapter a =
                        (CommentListAdapter)commentListView.getAdapter();
                    if (null != tv && null != tv.getTag()) {
                        a.likeUnregisted(((Integer)tv.getTag()).intValue(), tv);
                    }
                    likePosting.remove(post_id);
                }
            });
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerLoginCallback(loginListener);
                service.registerCommentCallback(commentListener);
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

    @Override
    public void onCreate(Bundle bndl)
    {
        super.onCreate(bndl);

        resources = getResources();
        setContentView(R.layout.stream_item_comment);

        TextView tv = (TextView) findViewById(R.id.comment_text);
        if (null != tv) {
            /*
            tv.setKeyListener(new KeyListener() {
                public void clearMetaKeyState(
                    View view,
                    Editable content,
                    int states)
                {
                }

                public boolean onKeyDown(
                    View view,
                    Editable text,
                    int keyCode,
                    KeyEvent event)
                {
                    return false;
                }

                public boolean onKeyOther(
                    View view,
                    Editable text,
                    KeyEvent event)
                {
                    return false;
                }

                public boolean onKeyUp(
                    View view,
                    Editable text,
                    int keyCode,
                    KeyEvent event)
                {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        onClickSendComment(view);
                        return false;
                    default:
                        break;
                    }

                    return false;
                }

                public int getInputType()
                {
                    return InputType.TYPE_CLASS_TEXT;
                }
            });
            */
        }

        messageSpan =
            new TextAppearanceSpan(
                this,
                R.style.StreamMessage);
        usernameSpan =
            new TextAppearanceSpan(
                this,
                R.style.StreamMessageUser);
        summarySpan =
            new TextAppearanceSpan(
                this,
                R.style.StreamSummary);

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

        db = new Database(this);
        Database.StreamListItem listItem = db.getStreamListItem(postId);
        StreamDataView dv = (StreamDataView) findViewById(R.id.message_base);
        listItem.comment_can_post = false;
        dv.put(listItem);

        commentListView = (ListView) findViewById(R.id.comment_list);
        commentListView.setAdapter(new CommentListAdapter(this));

        intent = new Intent(this, ClientService.class);
        if (bindService(intent, conn, BIND_AUTO_CREATE)) {
        }
    }

    private void requestUpdateComments(final String postId)
    {
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try {
            if (null != service) {
                service.unregisterLoginCallback(loginListener);
                service.unregisterCommentCallback(commentListener);
                service.unregisterLikeCallback(likeListener);
                unbindService(conn);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }

    public void onClickSendComment(View v)
    {
        TextView tv = (TextView) findViewById(R.id.comment_text);
        String text = tv.getText().toString();
        Log.i(TAG, "comment:"+text);
        try {
            if (null != service) {
                service.addComment(postId, text);
            }
        } catch (RemoteException e) {
            Log.i(TAG, "RemoteException", e);
        }
    }

    public void onClickLike(View v)
    {
        Log.i(TAG, "add like to comment:"+v.getTag());
        if (null == v.getTag()) {
            return;
        }

        CommentListAdapter a =
            (CommentListAdapter)commentListView.getAdapter();
        CommentListAdapter.CommentItem ci =
            (CommentListAdapter.CommentItem) a.getItem(
                ((Integer)v.getTag()).intValue());
        String post_id = ci.post_id;
        likePosting.put(post_id, v);
        try {
            if (null != service) {
                if (ci.like_users.contains(userId)) {
                    service.unregistCommentLike(post_id);
                }
                else {
                    service.registCommentLike(post_id);
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "RemoteException", e);
        }
    }
}
