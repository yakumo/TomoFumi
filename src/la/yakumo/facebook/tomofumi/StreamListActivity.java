package la.yakumo.facebook.tomofumi;

import android.app.Activity;
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

    private IClientServiceCallback listener = new IClientServiceCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);

            if (null != service) {
                try {
                    service.updateStream();
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
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
            /*
            rdb.query(
                "stream",
                new String[] {
                    "_id",
                    "post_id",
                    "message",
                    "comments",
                    "comment_can_post",
                    "like_count",
                    "like_posted",
                    "can_like",
                    "actor_id",
                    "updated",
                },
                null,
                null,
                null,
                null,
                "created_time desc",
                "400");
            */
            rdb.rawQuery(
                "SELECT *"+
                " FROM stream"+
                " LEFT JOIN user"+
                " ON stream.actor_id=user.uid"+
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
                        try {
                            service.updateStream();
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException", e);
                        }
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

    public void postStream(View v)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, TextPostActivity.class);
        startActivity(intent);
    }

    public class StreamCursorAdapter
        extends CursorAdapter
    {
        private int idxUserName;
        private int idxPostId;
        private int idxMessage;
        private int idxUpdate;
        private int idxComments;
        private int idxCommentCanPost;
        private int idxLikeCount;
        private int idxLikePosted;
        private int idxCanLike;

        public StreamCursorAdapter(Context context, Cursor c)
        {
            super(context, c, false);
            idxUserName = c.getColumnIndex("name");
            idxPostId = c.getColumnIndex("post_id");
            idxMessage = c.getColumnIndex("message");
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

            Resources res = context.getResources();
            TextView message = (TextView) view.findViewById(R.id.message);
            TextView summary = (TextView) view.findViewById(R.id.summary);
            TextView comments = (TextView) view.findViewById(R.id.comments);
            TextView likes = (TextView) view.findViewById(R.id.likes);
            String post_id = cursor.getString(idxPostId);
            if (null != message) {
                message.setText(cursor.getString(idxMessage));
            }
            if (null != comments) {
                if (cursor.getInt(idxCommentCanPost) != 0) {
                    int comNum = cursor.getInt(idxComments);
                    String comFmt =
                        res.getQuantityString(
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
            if (null != summary) {
                summary.setText(cursor.getString(idxUserName));
            }
            if (null != likes) {
                if (cursor.getInt(idxCanLike) != 0) {
                    int likeNum = cursor.getInt(idxLikeCount);
                    String likeFmt =
                        res.getQuantityString(
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
