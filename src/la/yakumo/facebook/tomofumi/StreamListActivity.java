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

                    SQLiteDatabase wdb = db.getWritableDatabase();
                    try {
                        ContentValues val = new ContentValues();
                        val.put("updated", 0);
                        wdb.beginTransaction();
                        wdb.update("stream", val, "updated=1", null);
                        wdb.setTransactionSuccessful();
                    } finally {
                        wdb.endTransaction();
                    }
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
            rdb.query(
                "stream",
                new String[] {
                    "_id",
                    "message",
                    "comments",
                    "likes",
                    "actor_id",
                    "updated",
                },
                null,
                null,
                null,
                null,
                "created_time desc",
                "400");
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
        private int idxMessage;
        private int idxUpdate;
        private int idxComments;
        private int idxLikes;

        public StreamCursorAdapter(Context context, Cursor c)
        {
            super(context, c, false);
            idxMessage = c.getColumnIndex("message");
            idxUpdate = c.getColumnIndex("updated");
            idxComments = c.getColumnIndex("comments");
            idxLikes = c.getColumnIndex("likes");
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
            if (null != message) {
                message.setText(cursor.getString(idxMessage));
            }
            if (null != comments) {
                int comNum = cursor.getInt(idxComments);
                int likeNum = cursor.getInt(idxLikes);
                String comFmt =
                    res.getQuantityString(
                        R.plurals.plural_comment_format,
                        comNum);
                String likeFmt =
                    res.getQuantityString(
                        R.plurals.plural_like_format,
                        likeNum);
                comments.setText(
                    String.format(comFmt, comNum) + " " +
                    String.format(likeFmt, likeNum));
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
