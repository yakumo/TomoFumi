package la.yakumo.facebook.tomofumi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.IClientServiceCallback;

public class StreamListActivity extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private IClientService service = null;

    private ListView streamList;
    private Database db;
    private Handler handler = new Handler();
    private Resources resources = null;
    private ProgressDialog progress = null;

    private IClientServiceCallback listener = new IClientServiceCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);

            if (null != service) {
                requestUpdateStream();
            }
        }

        public void loginFailed(String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
        }

        public void updatedStream(String errorMessage)
        {
            Log.i(TAG, "updatedStream:"+errorMessage);
            handler.post(new Runnable() {
                public void run()
                {
                    if (null != progress) {
                        progress.dismiss();
                        progress = null;
                    }

                    StreamCursorAdapter a =
                        (StreamCursorAdapter)streamList.getAdapter();
                    a.getCursor().requery();
                }
            });
        }

        public void updatedComment(String post_id, String errorMessage)
        {
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
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerCallback(listener);
                service.login();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        resources = getResources();
        setContentView(R.layout.main);

        db = new Database(this);
        SQLiteDatabase wdb = db.getWritableDatabase();
        try {
            wdb.beginTransaction();
            wdb.execSQL("DELETE FROM stream");
            wdb.setTransactionSuccessful();
        } finally {
            wdb.endTransaction();
        }

        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c =
            rdb.rawQuery(
                "SELECT *"+
                " FROM stream"+
                " LEFT JOIN user"+
                " ON stream.actor_id=user._id"+
                " ORDER BY created_time DESC"+
                " LIMIT 400"+
                "",
                null);
        streamList = (ListView) findViewById(R.id.stream_list);
        streamList.setAdapter(new StreamCursorAdapter(this, c));

        Intent intent = new Intent(this, ClientService.class);
        if (bindService(intent, conn, BIND_AUTO_CREATE)) {
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

    public void onClickComment(View v)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, StreamItemActivity.class);
        intent.putExtra("post_id", (String)v.getTag());
        startActivity(intent);
    }

    public void onClickLike(View v)
    {
        String post_id = (String) v.getTag();
        try {
            service.addStreamLike(post_id);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(R.string.menu_refresh)
            .setIcon(R.drawable.ic_menu_refresh)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item)
                {
                    if (null != service) {
                        requestUpdateStream();
                    }
                    return true;
                }
            })
            ;
        menu.add(R.string.menu_post_stream)
            .setIcon(R.drawable.ic_menu_start_conversation)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item)
                {
                    postStream(null);
                    return true;
                }
            })
            ;
        return true;
    }

    public void requestUpdateStream()
    {
        handler.post(new Runnable() {
            public void run()
            {
                progress =
                    ProgressDialog.show(
                        StreamListActivity.this,
                        resources.getString(
                            R.string.progress_stream_update_title),
                        null,
                        false,
                        false);
                //progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
        });
        try {
            service.updateStream();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }

    public void postStream(View v)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, TextPostActivity.class);
        startActivity(intent);
    }

    public class StreamCursorAdapter
        extends CursorAdapter
    {
        private int idxPostId;
        private int idxUserName;
        private int idxMessage;
        private int idxDescription;
        private int idxUpdate;
        private int idxComments;
        private int idxCommentCanPost;
        private int idxLikeCount;
        private int idxLikePosted;
        private int idxCanLike;
        private Spannable.Factory factory = Spannable.Factory.getInstance();
        private TextAppearanceSpan usernameSpan =
            new TextAppearanceSpan(
                StreamListActivity.this,
                R.style.StreamMessageUser);

        public StreamCursorAdapter(Context context, Cursor c)
        {
            super(context, c, false);
            idxPostId = c.getColumnIndex("_id");
            idxUserName = c.getColumnIndex("name");
            idxMessage = c.getColumnIndex("message");
            idxDescription = c.getColumnIndex("description");
            idxUpdate = c.getColumnIndex("updated");
            idxComments = c.getColumnIndex("comments");
            idxCommentCanPost = c.getColumnIndex("comment_can_post");
            idxLikeCount = c.getColumnIndex("like_count");
            idxLikePosted = c.getColumnIndex("like_posted");
            idxCanLike = c.getColumnIndex("can_like");
        }

        public void bindView(View view, Context context, Cursor cursor)
        {
            if (null == view) {
                return;
            }

            TextView message = (TextView) view.findViewById(R.id.message);
            TextView summary = (TextView) view.findViewById(R.id.summary);
            TextView comments = (TextView) view.findViewById(R.id.comments);
            TextView likes = (TextView) view.findViewById(R.id.likes);
            String post_id = cursor.getString(idxPostId);
            if (null != message) {
                String usr = cursor.getString(idxUserName);
                String msg = cursor.getString(idxMessage);
                if (null == usr) {
                    usr = "???";
                }
                Spannable spannable =
                    factory.newSpannable(
                        usr + " " + msg);
                spannable.setSpan(
                    usernameSpan, 0, usr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                message.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            if (null != summary) {
                String desc = cursor.getString(idxDescription);
                if (desc.length() == 0) {
                    summary.setVisibility(View.GONE);
                }
                else {
                    summary.setVisibility(View.VISIBLE);
                    summary.setText(desc);
                }
            }
            if (null != comments) {
                if (cursor.getInt(idxCommentCanPost) != 0) {
                    int comNum = cursor.getInt(idxComments);
                    String comFmt =
                        resources.getQuantityString(
                            R.plurals.plural_comment_format,
                            comNum);
                    comments.setText(String.format(comFmt, comNum));
                    comments.setTag(post_id);
                    comments.setVisibility(View.VISIBLE);
                }
                else {
                    comments.setVisibility(View.GONE);
                }
            }
            if (null != likes) {
                if (cursor.getInt(idxCanLike) != 0) {
                    int likeNum = cursor.getInt(idxLikeCount);
                    String likeFmt =
                        resources.getQuantityString(
                            R.plurals.plural_like_format,
                            likeNum);
                    likes.setText(String.format(likeFmt, likeNum));
                    likes.setTag(post_id);
                    likes.setVisibility(View.VISIBLE);
                }
                else {
                    likes.setVisibility(View.GONE);
                }
            }

            if (cursor.getInt(idxUpdate) == 1) {
                view.setBackgroundResource(
                    R.color.stream_updated_background_color);
            }
            else {
                view.setBackgroundResource(
                    R.color.stream_no_updated_background_color);
            }
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            View ret = View.inflate(context, R.layout.stream_list_item, null);
            return ret;
        }
    }
}
