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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import la.yakumo.facebook.tomofumi.adapter.StreamListAdapter;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.callback.*;
import la.yakumo.facebook.tomofumi.view.NetImageView;

public class StreamListActivity extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private IClientService service = null;

    private ListView streamList;
    private Database db;
    private Handler handler = new Handler();
    private Resources resources = null;
    private ProgressDialog progress = null;
    private HashMap<String,View> likePosting = new HashMap<String,View>();

    /*
    private IClientServiceCallback listener = new IClientServiceCallback.Stub() {
        public void loggedIn(int sessionID, String userID)
        {
            Log.i(TAG, "loggedIn:"+sessionID+","+userID);

            if (null != service &&
                (Constants.SESSION_STREAM_LIST == sessionID ||
                 Constants.SESSION_UNKNOWN == sessionID)) {
                requestUpdateStream();
            }
        }

        public void loginFailed(int sessionID, String reason)
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

        public void addedStream(String errorMessage)
        {
        }

        public void addedComment(String post_id, String errorMessage)
        {
        }

        public void addedLike(final String post_id, String errorMessage)
        {
            handler.post(new Runnable() {
                public void run()
                {
                    StreamCursorAdapter a =
                        (StreamCursorAdapter)streamList.getAdapter();
                    a.getCursor().requery();

                    View v = likePosting.get(post_id);
                    likePosting.remove(post_id);
                    if (post_id.equals(v.getTag())) {
                        v.setEnabled(true);
                    }
                }
            });
        }
    };
    */

    private ILoginCallback loginListener = new ILoginCallback.Stub() {
        public void loggedIn(String userID)
        {
            Log.i(TAG, "loggedIn:"+userID);

            try {
                service.unregisterLoginCallback(loginListener);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
            requestUpdateStream();
        }

        public void loginFailed(String reason)
        {
            Log.i(TAG, "loginFailed:"+reason);
            try {
                service.unregisterLoginCallback(loginListener);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }
    };

    private IStreamCallback streamListener = new IStreamCallback.Stub() {
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

                    StreamListAdapter a =
                        (StreamListAdapter)streamList.getAdapter();
                    a.reloadData();
                }
            });
        }

        public void addedStream(String errorMessage)
        {
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
                    StreamListAdapter a =
                        (StreamListAdapter)streamList.getAdapter();
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
                    StreamListAdapter a =
                        (StreamListAdapter)streamList.getAdapter();
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
                    StreamListAdapter a =
                        (StreamListAdapter)streamList.getAdapter();
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
                    StreamListAdapter a =
                        (StreamListAdapter)streamList.getAdapter();
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
                service.registerStreamCallback(streamListener);
                service.registerLikeCallback(likeListener);
                service.login(Constants.SESSION_STREAM_LIST);
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

        /*
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c =
            rdb.rawQuery(
                "SELECT *, stream._id as post_id"+
                " FROM stream"+
                " LEFT JOIN user"+
                " ON stream.actor_id=user._id"+
                " ORDER BY created_time DESC"+
                " LIMIT 400"+
                "",
                null);
        */
        streamList = (ListView) findViewById(R.id.stream_list);
        streamList.setAdapter(new StreamListAdapter(this));

        Intent intent = new Intent(this, ClientService.class);
        if (bindService(intent, conn, BIND_AUTO_CREATE)) {
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        StreamListAdapter a =
            (StreamListAdapter)streamList.getAdapter();
        a.reloadData();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try {
            if (null != service) {
                service.unregisterLoginCallback(loginListener);
                service.unregisterStreamCallback(streamListener);
                service.unregisterLikeCallback(likeListener);
                unbindService(conn);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
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
        /*
        String post_id = (String) v.getTag();
        if (likePosting.containsKey(post_id)) {
            return;
        }
        try {
            likePosting.put(post_id, v);
            service.toggleStreamLike(post_id);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
        */
        StreamListAdapter a = (StreamListAdapter)streamList.getAdapter();
        Integer idx = (Integer) v.getTag();
        String post_id = a.getPostId(idx);
        if (likePosting.containsKey(post_id)) {
            return;
        }
        try {
            likePosting.put(post_id, v);
            service.toggleStreamLike(post_id);
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
}
