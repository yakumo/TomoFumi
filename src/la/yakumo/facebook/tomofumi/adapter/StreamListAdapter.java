package la.yakumo.facebook.tomofumi.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.IClientService;
import la.yakumo.facebook.tomofumi.service.callback.*;
import la.yakumo.facebook.tomofumi.view.NetImageView;
import la.yakumo.facebook.tomofumi.view.StreamDataView;

public class StreamListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    private IClientService service = null;

    private HashMap<String,Database.StreamListItem> itemMap =
        new HashMap<String,Database.StreamListItem>();
    private Database.StreamListItem[] items = null;
    private Context context;
    private Resources resources;
    private LayoutInflater layoutInflater;
    private Database db;

    private ILikeCallback likeListener = new ILikeCallback.Stub() {
        public void registerLike(final String post_id)
        {
            Log.i(TAG, "(2)registerLike:"+post_id);
        }

        public void registedLike(final String post_id)
        {
            Log.i(TAG, "(2)registedLike:"+post_id);
            if (itemMap.containsKey(post_id)) {
                Database.StreamListItem listItem = itemMap.get(post_id);
                listItem.like.state_changing = false;
                listItem.like.count++;
                listItem.like.enable_item = true;
            }
        }

        public void unregisterLike(final String post_id)
        {
            Log.i(TAG, "(2)unregisterLike:"+post_id);
        }

        public void unregistedLike(final String post_id)
        {
            Log.i(TAG, "(2)unregistedLike:"+post_id);
            if (itemMap.containsKey(post_id)) {
                Database.StreamListItem listItem = itemMap.get(post_id);
                listItem.like.state_changing = false;
                listItem.like.count--;
                listItem.like.enable_item = false;
            }
        }

        public void likeDataUpdated(String post_id, int likes, boolean liked)
        {
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder)
        {
            service = IClientService.Stub.asInterface(binder);
            try {
                service.registerLikeCallback(likeListener);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
            }
        }

        public void onServiceDisconnected(ComponentName name)
        {
        }
    };

    public StreamListAdapter(Context context)
    {
        super();
        this.context = context;
        this.resources = context.getResources();
        this.layoutInflater = LayoutInflater.from(context);
        this.db = new Database(context);

        Intent intent = new Intent(context, ClientService.class);
        if (context.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
        }
    }

    public void reloadData()
    {
        items = db.getStreamListItems();
        for (Database.StreamListItem item : items) {
            itemMap.put(item.post_id, item);
        }
        notifyDataSetChanged();
    }

    public void releaseData()
    {
        try {
            service.unregisterLikeCallback(likeListener);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
        context.unbindService(conn);
    }

    public String getPostId(int position)
    {
        return items[position].post_id;
    }

    public int getCount()
    {
        if (null == items) {
            return 0;
        }
        return items.length;
    }

    public Object getItem (int position)
    {
        if (null == items) {
            return null;
        }
        return items[position];
    }

    public long getItemId (int position)
    {
        return position;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        View ret = convertView;

        Database.StreamListItem li = (Database.StreamListItem) getItem(position);
        if (null == ret) {
            ret =
                layoutInflater.inflate(
                    R.layout.stream_list_item,
                    null);
        }
        if (null != ret) {
            StreamDataView liv = (StreamDataView) ret;
            liv.put(li);
        }
        return ret;
    }
}
