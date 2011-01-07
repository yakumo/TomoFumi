package la.yakumo.facebook.tomofumi.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.callback.ICommandCallback;

public class LocalService
    extends Service
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;

    public static final int MSG_RELOAD_STREAM = 101;

    public static final int MSG_RELOADED_STREAM = 201;

    private IClientService service = null;
    private ArrayList<Message> bootMessageList = new ArrayList<Message>();
    private ArrayList<Messenger> clients = new ArrayList<Messenger>();

    final private ICommandCallback callback = new ICommandCallback.Stub() {
        public void reloadedStream(String errMsg)
        {
            for (Messenger c : clients) {
                try {
                    c.send(Message.obtain(null, MSG_RELOADED_STREAM, errMsg));
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                }
            }
        }

        public void reloadedComment(String post_id, String errMsg)
        {
        }

        public void reloadedLike(String post_id, String errMsg)
        {
        }
    };

    final private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerCallback(callback);
                for (Message msg : bootMessageList) {
                    messenger.send(
                        Message.obtain(
                            null,
                            msg.what,
                            msg.arg1,
                            msg.arg2,
                            msg.obj));
                }
                bootMessageList.clear();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    final Messenger messenger = new Messenger(new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                clients.add(msg.replyTo);
                return;
            case MSG_UNREGISTER_CLIENT:
                clients.remove(msg.replyTo);
                return;
            }

            if (null == service) {
                Message message = new Message();
                message.what = msg.what;
                message.arg1 = msg.arg1;
                message.arg2 = msg.arg2;
                message.obj = msg.obj;
                bootMessageList.add(message);
                return;
            }

            switch (msg.what) {
            case MSG_RELOAD_STREAM:
                try {
                    if (null != service) {
                        service.reloadStream(msg.arg1 != 0);
                    }
                } catch (RemoteException e) {
                }
                break;
            default:
                break;
            }
        }
    });

    @Override
    public void onCreate()
    {
        Log.i(TAG, "LocalService#onCreate");
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClass(this, ClientService.class);
        if (bindService(i, conn, Context.BIND_AUTO_CREATE)) {
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return messenger.getBinder();
    }

    @Override
    public void onDestroy()
    {
        try {
            service.unregisterCallback(callback);
            unbindService(conn);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }
}
