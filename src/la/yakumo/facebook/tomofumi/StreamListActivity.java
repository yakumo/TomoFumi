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

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerLoginCallback(loginListener);
                service.registerStreamCallback(streamListener);
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
        StreamListAdapter a =
            (StreamListAdapter)streamList.getAdapter();
        if (null != a) {
            a.releaseData();
        }
        super.onDestroy();
        try {
            if (null != service) {
                service.unregisterLoginCallback(loginListener);
                service.unregisterStreamCallback(streamListener);
                unbindService(conn);
            }
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
