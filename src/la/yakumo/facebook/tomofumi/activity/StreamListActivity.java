package la.yakumo.facebook.tomofumi.activity;

import android.R.anim;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.adapter.StreamListAdapter;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.LocalService;
import la.yakumo.facebook.tomofumi.view.ItemDataView;
import la.yakumo.facebook.tomofumi.view.StreamDataView;

public class StreamListActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private Handler handler = new Handler();
    private Resources resources;
    private LocalService.Stub mService;
    private View progress;
    private ListView streamList;
    private boolean isReloading = false;

    private class ServiceCallback
        extends Binder
        implements IInterface
        ,LocalService.OnStreamRead
        ,LocalService.OnImageRead
        ,LocalService.OnStreamLikeChange
    {
        private String DESCRIPTOR =
            "la.yakumo.facebook.tomofumi.activity.StreamListActivity$ServiceCallback";
        public ServiceCallback()
        {
            attachInterface(this, DESCRIPTOR);
        }

        public IBinder asBinder()
        {
            return this;
        }

        public void onStreamReadStart()
        {
            handler.post(new Runnable() {
                public void run()
                {
                    streamReadStart();
                }
            });
        }

        public void onStreamReadFinish(final String errMsg)
        {
            handler.post(new Runnable() {
                public void run()
                {
                    streamReadFinish(errMsg);
                }
            });
        }

        public void onImageReaded(final String url)
        {
            handler.post(new Runnable() {
                public void run()
                {
                    imageReaded(url);
                }
            });
        }

        public void onStreamLikeChange(
            final String post_id,
            final boolean isAccess)
        {
            handler.post(new Runnable() {
                public void run()
                {
                    streamLikeChange(post_id, isAccess);
                }
            });
        }
    }
    private ServiceCallback serviceCallback = new ServiceCallback();

    private ServiceConnection conn = new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.i(TAG, "onServiceConnected:"+service);
            mService = (LocalService.Stub) service;
            mService.addStreamReadCallback(serviceCallback);
            mService.addImageReadCallback(serviceCallback);
            mService.addStreamLikeChangeCallback(serviceCallback);
            mService.reloadStream();
        }

        public void onServiceDisconnected(ComponentName className)
        {
            mService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        resources = getResources();
        setContentView(R.layout.main);

        progress = findViewById(R.id.stream_progress_view);
        StreamListAdapter a = new StreamListAdapter(this);
        a.setOnClickItem(new ItemDataView.OnClickItem() {
            public void onClickComment(Database.MessageItem item)
            {
                Log.i(TAG, "comment:"+item.post_id);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(
                    StreamListActivity.this,
                    StreamItemActivity.class);
                intent.putExtra("post_id", item.post_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            public void onClickLike(Database.MessageItem item)
            {
                Log.i(TAG, "like:"+item.post_id);
                if (item.like.enable_item) {
                    mService.resetStreamLike(item.post_id);
                }
                else {
                    mService.setStreamLike(item.post_id);
                }
            }
            public void onClickShare(Database.MessageItem item)
            {
            }
            public void onClickUserIcon(Database.MessageItem item)
            {
            }
        });
        streamList = (ListView) findViewById(R.id.stream_list);
        streamList.setAdapter(a);

        Intent intent = new Intent(this, LocalService.class);
        if (bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stream_list_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem menuItem;

        menuItem = menu.findItem(R.id.menuitem_reload);
        if (null != menuItem) {
            boolean flg = true;
            if (isReloading) {
                flg = false;
            }
            menuItem.setEnabled(flg);
        }

        menuItem = menu.findItem(R.id.menuitem_stream_post);
        if (null != menuItem) {
            boolean flg = true;
            menuItem.setEnabled(flg);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.menuitem_reload:
            if (null != mService) {
                mService.reloadStream();
            }
            return true;
        case R.id.menuitem_stream_post:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    void streamReadStart()
    {
        Log.i(TAG, "!!!! onStreamReadStart !!!!");
        isReloading = true;
        if (null != progress) {
            Animation anim =
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.stream_progress_start);
            anim.setAnimationListener(
                new AnimationListener()
                {
                    public void onAnimationStart(Animation animation)
                    {
                        progress.setVisibility(View.VISIBLE);
                        Log.i(TAG, "onAnimationStart");
                    }

                    public void onAnimationRepeat(Animation animation)
                    {
                        Log.i(TAG, "onAnimationRepeat");
                    }

                    public void onAnimationEnd(Animation animation)
                    {
                        Log.i(TAG, "onAnimationEnd");
                    }
                });
            if (null != anim) {
                progress.startAnimation(anim);
            }
        }
    }

    void streamReadFinish(String errMsg)
    {
        Log.i(TAG, "!!!! onStreamReadFinish !!!!");
        isReloading = false;
        if (null != progress) {
            Animation anim =
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.stream_progress_finish);
            anim.setAnimationListener(
                new AnimationListener()
                {
                    public void onAnimationStart(Animation animation)
                    {
                        Log.i(TAG, "onAnimationStart");
                    }

                    public void onAnimationRepeat(Animation animation)
                    {
                        Log.i(TAG, "onAnimationRepeat");
                    }

                    public void onAnimationEnd(Animation animation)
                    {
                        Log.i(TAG, "onAnimationEnd");
                        progress.setVisibility(View.INVISIBLE);
                        handler.post(new Runnable() {
                            public void run()
                            {
                                StreamListAdapter a =
                                    (StreamListAdapter)streamList.getAdapter();
                                a.reloadData();
                            }
                        });
                    }
                });
            if (null != anim) {
                progress.startAnimation(anim);
            }
        }
    }

    void imageReaded(String url)
    {
        Log.i(TAG, "StreamListActivity#imageReaded:"+url);

        StreamListAdapter a = (StreamListAdapter)streamList.getAdapter();

        int cnt = streamList.getChildCount();
        for (int i = 0 ; i < cnt ; i++) {
            View v = streamList.getChildAt(i);
            View tv = v.findViewWithTag((Object)url);
            while (tv != null) {
                if (tv instanceof ItemDataView) {
                    break;
                }
                tv = (View) tv.getParent();
            }

            if (null != tv) {
                ItemDataView itemView = (ItemDataView) tv;
                if (null != itemView) {
                    Log.i(TAG, "find view !!!, "+itemView);
                    a.reloadData(itemView.get(), false);
                    itemView.reload();
                }
            }
        }

        a.imageLoaded(url);
    }

    void streamLikeChange(String post_id, boolean isAccess)
    {
        Log.i(TAG, "stream like change:"+post_id+","+isAccess);

        StreamListAdapter a = (StreamListAdapter)streamList.getAdapter();
        int cnt = streamList.getChildCount();
        View v = streamList.findViewWithTag(post_id);
        Log.i(TAG, "streamLikeChange:"+v);
        if (null != v) {
            if (v instanceof ItemDataView) {
                ItemDataView idv = (ItemDataView) v;
                if (isAccess) {
                    Database.MessageItem item = idv.get();
                    item.like.state_changing = true;
                    idv.put(item);
                }
                else {
                    a.reloadData(idv.get(), true);
                }
            }
        }
        else {
            if (!isAccess) {
                if (a.hasPostId(post_id)) {
                    a.reloadData();
                }
            }
        }
    }
}
