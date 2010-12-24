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

    //private ArrayList<StreamListItem> items = new ArrayList<StreamListItem>();
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

    private boolean likeViewUpdate(int position, View v)
    {
        /*
        if (((Integer) v.getTag()).intValue() == position) {
            TextView likes = (TextView) v;
            StreamListItem li = (StreamListItem) getItem(position);
            int likeNum = li.like_count;
            String likeFmt =
                resources.getQuantityString(
                    R.plurals.plural_like_format,
                    likeNum);
            likes.setText(String.format(likeFmt, likeNum));
            likes.setCompoundDrawablesWithIntrinsicBounds(
                ((li.like_posting)?
                 R.drawable.like_press:
                 ((li.like_posted)?
                  R.drawable.like_light:
                  R.drawable.like_dark)),
                0, 0, 0);
            return true;
        }
        */
        return false;
    }

    public void likeUpdating(int position, View v)
    {
        /*
        StreamListItem li = (StreamListItem) getItem(position);
        li.like_posting = true;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(false);
        }
        */
    }

    public void likeRegisted(int position, View v)
    {
        /*
        StreamListItem li = (StreamListItem) getItem(position);
        li.like_posting = false;
        li.like_count++;
        li.like_posted = true;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(true);
        }
        */
    }

    public void likeUnregisted(int position, View v)
    {
        /*
        StreamListItem li = (StreamListItem) getItem(position);
        li.like_posting = false;
        li.like_count--;
        li.like_posted = false;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(true);
        }
        */
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
