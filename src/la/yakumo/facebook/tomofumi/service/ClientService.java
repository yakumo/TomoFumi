package la.yakumo.facebook.tomofumi.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class ClientService
    extends Service
{
    private RemoteCallbackList<IClientServiceCallback> listeners =
        new RemoteCallbackList<IClientServiceCallback>();

    private final IClientService.Stub stub =
        new IClientService.Stub()
        {
            public void registerCallback(IClientServiceCallback callback)
            throws RemoteException
            {
                listeners.register(callback);
            }

            public void unregisterCallback(IClientServiceCallback callback)
            throws RemoteException
            {
                listeners.unregister(callback);
            }

            public void updateStream()
            throws RemoteException
            {
            }

            public void updateComment(String post_id)
            throws RemoteException
            {
            }

            public void updateLike(String post_id)
            throws RemoteException
            {
            }

            public void addStream(String text)
            throws RemoteException
            {
            }

            public void addComment(String post_id, String text)
            throws RemoteException
            {
            }

            public void addStreamLike(String post_id)
            throws RemoteException
            {
            }

            public void addCommentLike(String post_id)
            throws RemoteException
            {
            }
        };

    public IBinder onBind(Intent intent)
    {
        return stub;
    }
}
