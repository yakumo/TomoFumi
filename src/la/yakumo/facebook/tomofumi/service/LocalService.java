package la.yakumo.facebook.tomofumi.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import javax.rmi.CORBA.Stub;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.callback.ICommandCallback;

public class LocalService
    extends Service
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;

    public static final int MSG_RELOAD_STREAM = 101;

    public static final int MSG_RELOAD_STREAM_START = 201;
    public static final int MSG_RELOAD_STREAM_FINISH = 202;

    private IClientService service = null;
    private ArrayList<Message> bootMessageList = new ArrayList<Message>();
    private ArrayList<Messenger> clients = new ArrayList<Messenger>();

    final private ICommandCallback callback = new ICommandCallback.Stub() {
        public void reloadStreamStart()
        {
            new Thread(new Runnable() {
                public void run()
                {
                    Log.i(TAG, "!!! (1)ICommandCallback.Stub#reloadStreamStart !!!");
                    Log.i(TAG, "task:"+Thread.currentThread().getId());
                    for (Messenger c : clients) {
                        try {
                            c.send(
                                Message.obtain(
                                    null,
                                    MSG_RELOAD_STREAM_START));
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException", e);
                        }
                    }
                    Log.i(TAG, "!!! (2)ICommandCallback.Stub#reloadStreamStart !!!");
                }
            }).start();
        }

        public void reloadStreamFinish(final String errMsg)
        {
            new Thread(new Runnable() {
                public void run()
                {
                    Log.i(TAG, "!!! (1)ICommandCallback.Stub#reloadStreamFinish !!!");
                    Log.i(TAG, "task:"+Thread.currentThread().getId());
                    for (Messenger c : clients) {
                        try {
                            c.send(
                                Message.obtain(
                                    null,
                                    MSG_RELOAD_STREAM_FINISH,
                                    errMsg));
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException", e);
                        }
                    }
                    Log.i(TAG, "!!! (2)ICommandCallback.Stub#reloadStreamFinish !!!");
                }
            }).start();
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
                localServerStub.notifyAll();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    private static final Stub localServerStub = new Stub();

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
        return localServerStub;
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

    public class Stub
        extends Binder
        implements IInterface
    {
        private String DESCRIPTOR =
            "la.yakumo.facebook.tomofumi.service.LocalService.Stub";
        public Stub()
        {
            attachInterface(this, DESCRIPTOR);
        }

        public IBinder asBinder()
        {
            return this;
        }

        public void callTest()
        {
            Log.i(TAG, "call !!!");
        }
    }
}
