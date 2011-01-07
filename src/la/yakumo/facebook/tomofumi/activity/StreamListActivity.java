package la.yakumo.facebook.tomofumi.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MenuItem;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.service.LocalService;

public class StreamListActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private Handler handler = new Handler();
    private Messenger mService;
    private Resources resources;

    private ServiceConnection conn = new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mService = new Messenger(service);
            sendMessage(
                Message.obtain(
                    null,
                    LocalService.MSG_REGISTER_CLIENT));
            sendMessage(
                Message.obtain(
                    null,
                    LocalService.MSG_RELOAD_STREAM,
                    1, 0, null));
        }

        public void onServiceDisconnected(ComponentName className)
        {
            mService = null;
        }
    };

    private Messenger client = new Messenger(new Handler() {
         public void handleMessage(Message msg)
        {
            switch (msg.what) {
            case LocalService.MSG_RELOADED_STREAM:
                Log.i(TAG, "LocalClient.MSG_RELOADED_STREAM");
                break;
            default:
                break;
            }

        }
   });

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        resources = getResources();
        setContentView(R.layout.main);

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

    private void sendMessage(Message msg)
    {
        try {
            msg.replyTo = client;
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }
}
