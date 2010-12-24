package la.yakumo.facebook.tomofumi.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.view.NetImageView;
import la.yakumo.facebook.tomofumi.view.StreamDataView;

public class StreamListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    private Database.StreamListItem[] items = null;
    private Context context;
    private Resources resources;
    private Database db;

    public StreamListAdapter(Context context)
    {
        super();
        this.context = context;
        this.resources = context.getResources();
        this.db = new Database(context);
    }

    public void reloadData()
    {
        items = db.getStreamListItems();
        notifyDataSetChanged();
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
            ret = View.inflate(context, R.layout.stream_list_item, null);
        }
        if (null != ret) {
            StreamDataView liv = (StreamDataView) ret;
            liv.put(li);
        }
        return ret;
    }
}
