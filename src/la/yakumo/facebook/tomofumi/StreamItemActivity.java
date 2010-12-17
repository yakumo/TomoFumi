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
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.IClientServiceCallback;
import la.yakumo.facebook.tomofumi.view.NetImageView;

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
    private ListView commentListView;
    private Database db;
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;
    private TextAppearanceSpan summarySpan;
    private MovementMethod movementmethod =
        LinkMovementMethod.getInstance();

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

                        SQLiteDatabase rdb = db.getReadableDatabase();
                        Cursor c =
                            rdb.rawQuery(
                                "SELECT *"+
                                " FROM comments"+
                                " LEFT JOIN user"+
                                " ON comments.user_id=user._id"+
                                " WHERE comments.post_id=?"+
                                " ORDER BY"+
                                " data_mode DESC"+
                                ",time ASC"+
                                ",name ASC"+
                                "",
                                new String[] {
                                    postId
                                });
                        if (c.moveToFirst()) {
                            do {
                                Log.i(
                                    TAG,
                                    "post_id:"+
                                    c.getString(c.getColumnIndex("post_id"))+
                                    ", name:"+
                                    c.getString(c.getColumnIndex("name"))+
                                    ", message:"+
                                    c.getString(c.getColumnIndex("message"))+
                                    ", pic_square:"+
                                    c.getString(c.getColumnIndex("pic_square")));
                            } while (c.moveToNext());
                        }

                        CommentListAdapter ca = (CommentListAdapter)
                            commentListView.getAdapter();
                        ca.getCursor().requery();
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
        setContentView(R.layout.stream_item_comment);

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
        SQLiteDatabase rdb = db.getReadableDatabase();

        Cursor c =
            rdb.rawQuery(
                "SELECT *"+
                " FROM stream"+
                " LEFT JOIN user"+
                " ON stream.actor_id=user._id"+
                " WHERE stream._id=?",
                new String[] {
                    postId,
                });
        if (c.moveToFirst()) {
            TextView message = (TextView) findViewById(R.id.message);
            TextView summary = (TextView) findViewById(R.id.summary);
            TextView description = (TextView) findViewById(R.id.description);
            NetImageView streamIcon = (NetImageView)
                findViewById(R.id.stream_icon);
            NetImageView summaryIcon = (NetImageView)
                findViewById(R.id.summary_icon);
            View summaryBase = findViewById(R.id.summary_base);
            if (null != streamIcon) {
                streamIcon.setImageResource(R.drawable.clear_image);
            }
            if (null != summaryIcon) {
                summaryIcon.setImageResource(R.drawable.clear_image);
            }
            if (null != message) {
                String usr = c.getString(c.getColumnIndex("name"));
                String msg = c.getString(c.getColumnIndex("message"));
                String userUrl = c.getString(c.getColumnIndex("profile_url"));
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
                message.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            if (null != summary) {
                String name = c.getString(c.getColumnIndex("attachment_name"));
                String caption = c.getString(c.getColumnIndex("attachment_caption"));
                String link = c.getString(c.getColumnIndex("attachment_link"));
                String icon = c.getString(c.getColumnIndex("attachment_icon"));
                String image = c.getString(c.getColumnIndex("attachment_image"));
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
                    if (null != summaryIcon) {
                        if (null != image && image.length() > 0){
                            summaryIcon.setVisibility(View.VISIBLE);
                            summaryIcon.setImageURI(Uri.parse(image));
                        }
                        else {
                            summaryIcon.setVisibility(View.GONE);
                        }
                    }
                    summaryBase.setVisibility(View.VISIBLE);
                    summary.setText(spannable, TextView.BufferType.SPANNABLE);
                }
                else {
                    summaryBase.setVisibility(View.GONE);
                }
            }
            if (null != description) {
                String desc = c.getString(c.getColumnIndex("description"));
                if (null != desc && desc.length() > 0) {
                    description.setVisibility(View.VISIBLE);
                    description.setText(desc);
                }
                else {
                    description.setVisibility(View.GONE);
                }
            }
            if (null != streamIcon) {
                String uri = c.getString(c.getColumnIndex("pic_square"));
                if (null != uri) {
                    streamIcon.setImageURI(Uri.parse(uri));
                    streamIcon.setVisibility(View.VISIBLE);
                }
                else {
                    streamIcon.setVisibility(View.GONE);
                }
            }
        }
        c.close();

        c = rdb.rawQuery(
                "SELECT *"+
                " FROM comments"+
                " LEFT JOIN user"+
                " ON comments.user_id=user._id"+
                " WHERE comments.post_id=?"+
                " ORDER BY"+
                " data_mode DESC"+
                ",time ASC"+
                ",name ASC"+
                "",
                new String[] {
                    postId
                });
        commentListView = (ListView) findViewById(R.id.comment_list);
        commentListView.setAdapter(new CommentListAdapter(this, c));

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
                service.unregisterCallback(listener);
                unbindService(conn);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
        db.close();
    }

    public class CommentListAdapter
        extends CursorAdapter
    {
        private int idxUserName;
        private int idxUserIcon;
        private int idxMessage;
        private Spannable.Factory factory = Spannable.Factory.getInstance();
        private TextAppearanceSpan messageSpan =
            new TextAppearanceSpan(
                StreamItemActivity.this,
                R.style.StreamMessage);
        private TextAppearanceSpan usernameSpan =
            new TextAppearanceSpan(
                StreamItemActivity.this,
                R.style.StreamMessageUser);
        private MovementMethod movementmethod =
            LinkMovementMethod.getInstance();

        public CommentListAdapter(Context context, Cursor c)
        {
            super(context, c);
            idxUserName = c.getColumnIndex("name");
            idxUserIcon = c.getColumnIndex("pic_square");
            idxMessage = c.getColumnIndex("message");
        }

        public void bindView(View view, Context context, Cursor cursor)
        {
            TextView message = (TextView) view.findViewById(R.id.message);
            NetImageView icon = (NetImageView)
                view.findViewById(R.id.stream_icon);
            if (null != message) {
                message.setText("");
                String msg =
                    (cursor.isNull(idxMessage)?
                     "":
                     cursor.getString(idxMessage));
                String usr =
                    (cursor.isNull(idxUserName)?
                     "":
                     cursor.getString(idxUserName));
                String allMsg = usr + " " + msg;
                Spannable spannable = factory.newSpannable(allMsg);
                spannable.setSpan(
                    messageSpan, 0, allMsg.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(
                    usernameSpan, 0, usr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                message.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            if (null != icon) {
                String iconUri = cursor.getString(idxUserIcon);
                if (null != icon) {
                    icon.setImageURI(Uri.parse(iconUri));
                }
            }
        }

        public View newView (Context context, Cursor cursor, ViewGroup parent)
        {
            View ret =
                View.inflate(
                    context,
                    R.layout.stream_item_comment_item,
                    null);
            return ret;
        }
    }
}
