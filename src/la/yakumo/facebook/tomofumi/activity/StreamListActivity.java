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
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.service.LocalService;

public class StreamListActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private Handler handler = new Handler();
    private Resources resources;
    private LocalService.Stub mService;
    private View progress;

    private class StreamReadCallback
        extends Binder
        implements IInterface,
        LocalService.OnStreamRead
    {
        private String DESCRIPTOR =
            "la.yakumo.facebook.tomofumi.activity.StreamListActivity";
        public StreamReadCallback()
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
    }
    private StreamReadCallback streamReadCallback = new StreamReadCallback();

    private ServiceConnection conn = new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.i(TAG, "onServiceConnected:"+service);
            mService = (LocalService.Stub) service;
            mService.addStreamReadCallback(streamReadCallback);
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

    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(R.string.menu_refresh)
            .setIcon(R.drawable.ic_menu_refresh)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item)
                {
                    return true;
                }
            })
            ;
        menu.add(R.string.menu_post_stream)
            .setIcon(R.drawable.ic_menu_start_conversation)
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item)
                {
                    return true;
                }
            })
            ;
        return true;
    }

    public void streamReadStart()
    {
        Log.i(TAG, "!!!! onStreamReadStart !!!!");
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

    public void streamReadFinish(String errMsg)
    {
        Log.i(TAG, "!!!! onStreamReadFinish !!!!");
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
                    }
                });
            if (null != anim) {
                progress.startAnimation(anim);
            }
        }
    }
}
