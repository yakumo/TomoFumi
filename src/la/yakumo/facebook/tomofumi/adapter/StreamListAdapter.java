package la.yakumo.facebook.tomofumi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.view.StreamDataView;

public class StreamListAdapter
    extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private Database db;
    private Database.MessageItem[] items = new Database.MessageItem[0];

    public StreamListAdapter(Context context)
    {
        this.context = context;
        this.layoutInflater =
            (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        db = new Database(context);
    }

    public void reloadData()
    {
        items = db.getStreamListItems();
        notifyDataSetChanged();
    }

    public int getCount()
    {
        return items.length;
    }

    public Object getItem(int position)
    {
        return items[position];
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getViewTypeCount()
    {
        return 1;
    }

    public int getItemViewType(int position)
    {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View ret = convertView;
        if (null == ret) {
            ret = layoutInflater.inflate(R.layout.stream_list_item, null);
        }
        if (null != ret) {
            StreamDataView sdv = (StreamDataView) ret;
            sdv.put((Database.MessageItem)getItem(position));
        }
        return ret;
    }
}
