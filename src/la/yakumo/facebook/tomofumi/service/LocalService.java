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
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.TokenWatcher;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
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
            new Thread(new Runnable(){
                public void run()
                {
                    localServerStub.streamReadStart();
                }
            }).start();
        }

        public void reloadStreamFinish(final String errMsg)
        {
            new Thread(new Runnable(){
                public void run()
                {
                    localServerStub.streamReadFinish(errMsg);
                }
            }).start();
        }

        public void reloadedComment(String post_id, String errMsg)
        {
        }

        public void reloadedLike(String post_id, String errMsg)
        {
        }

        public void readedImage(final String url)
        {
            Log.i(TAG, "reloadedImage:"+url);
            new Thread(new Runnable(){
                public void run()
                {
                    localServerStub.readedImage(url);
                }
            }).start();
        }
    };

    final private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerCallback(callback);
                //localServerStub.notifyAll();
                localServerStub.serviceConnected();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    private final Stub localServerStub = new Stub();

    @Override
    public void onCreate()
    {
        Log.i(TAG, "LocalService#onCreate");

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClass(LocalService.this, ClientService.class);
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
        private ArrayList<Runnable> commandList = new ArrayList<Runnable>();
        private Handler waitingHandler = null;

        private HashMap<OnStreamRead,DeathCallback> streamReadCallbacks =
            new HashMap<OnStreamRead,DeathCallback>();
        private HashMap<OnImageRead,DeathCallback> imageReadCallbacks =
            new HashMap<OnImageRead,DeathCallback>();

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

        void serviceConnected()
        {
            if (null != waitingHandler) {
                waitingHandler.sendEmptyMessage(1);
            }
        }

        private void execute(Runnable command)
        {
            if (null == service) {
                if (null == waitingHandler) {
                    new Thread(new Runnable() {
                        public void run()
                        {
                            Looper.prepare();
                            waitingHandler = new Handler() {
                                public void handleMessage(Message msg)
                                {
                                    Looper.myLooper().quit();
                                }
                            };
                            Looper.loop();

                            waitingHandler = null;
                            for (Runnable r : commandList) {
                                r.run();
                            }
                        }
                    }).start();
                }
                commandList.add(command);
            }
            else {
                new Thread(command).start();
            }
        }

        // stream readed ////////////////////////////////////////////////////////
        public void addStreamReadCallback(OnStreamRead sr)
        {
            IBinder binder = sr.asBinder();
            DeathCallback cb = new DeathCallback(sr);
            try {
                binder.linkToDeath(cb, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
            streamReadCallbacks.put(sr, cb);
        }

        public void removeStreamReadCallback(OnStreamRead sr)
        {
            IBinder binder = sr.asBinder();
            binder.unlinkToDeath(streamReadCallbacks.get(sr), 0);
            streamReadCallbacks.remove(sr);
        }

        void streamReadStart()
        {
            for (OnStreamRead sr : streamReadCallbacks.keySet()) {
                sr.onStreamReadStart();
            }
        }

        void streamReadFinish(String errStr)
        {
            for (OnStreamRead sr : streamReadCallbacks.keySet()) {
                sr.onStreamReadFinish(errStr);
            }
        }

        public void reloadStream()
        {
            execute(new Runnable() {
                public void run()
                {
                    try {
                        service.reloadStream(true);
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException", e);
                    }
                }
            });
        }

        // image readed /////////////////////////////////////////////////////////
        public void addImageReadCallback(OnImageRead sr)
        {
            IBinder binder = sr.asBinder();
            DeathCallback cb = new DeathCallback(sr);
            try {
                binder.linkToDeath(cb, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
            imageReadCallbacks.put(sr, cb);
        }

        public void removeImageReadCallback(OnImageRead sr)
        {
            IBinder binder = sr.asBinder();
            binder.unlinkToDeath(imageReadCallbacks.get(sr), 0);
            imageReadCallbacks.remove(sr);
        }

        public void readedImage(String url)
        {
            for (OnImageRead ir : imageReadCallbacks.keySet()) {
                ir.onImageReaded(url);
            }
        }

        // add like to stream message ///////////////////////////////////////////
        public void setStreamLike(final String post_id)
        {
            execute(new Runnable() {
                public void run()
                {
                    try {
                        service.setStreamLike(post_id);
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException", e);
                    }
                }
            });
        }

        public void resetStreamLike(final String post_id)
        {
            execute(new Runnable() {
                public void run()
                {
                    try {
                        service.resetStreamLike(post_id);
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException", e);
                    }
                }
            });
        }

        // callback settings ////////////////////////////////////////////////////
        final class DeathCallback
            implements IBinder.DeathRecipient
        {
            final IInterface watch;

            DeathCallback(IInterface obj)
            {
                watch = obj;
            }

            public void binderDied()
            {
                Log.i(TAG, "DeathCallback#binderDied, object:"+watch);
                synchronized (watch) {
                    if (watch instanceof OnStreamRead &&
                        streamReadCallbacks.containsKey((OnStreamRead)watch)) {
                        streamReadCallbacks.remove((OnStreamRead)watch);
                    }
                }
            }
        }
    }

    public static interface OnStreamRead
        extends IInterface
    {
        public void onStreamReadStart();
        public void onStreamReadFinish(String errMsg);
    }

    public static interface OnImageRead
        extends IInterface
    {
        public void onImageReaded(String url);
    }
}
