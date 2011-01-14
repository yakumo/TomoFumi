package la.yakumo.facebook.tomofumi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.view.ItemDataView;
import la.yakumo.facebook.tomofumi.view.StreamDataView;

public class StreamListAdapter
    extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;
    private Database db;
    private Database.MessageItem[] items = new Database.MessageItem[0];
    private ItemDataView.OnClickItem clickItem = null;

    public StreamListAdapter(Context context)
    {
        this.context = context;
        this.layoutInflater =
            (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        db = new Database(context);
    }

    public void setOnClickItem(ItemDataView.OnClickItem clickItem)
    {
        this.clickItem = clickItem;
    }

    public void reloadData()
    {
        items = db.getStreamListItems();
        notifyDataSetChanged();
    }

    public void reloadData(Database.MessageItem item, boolean updateFlag)
    {
        for (int i = 0 ; i < items.length ; i++) {
            if (item.post_id.equals(items[i].post_id)) {
                items[i] = db.getStreamListItem(item.post_id);
                if (updateFlag) {
                    notifyDataSetChanged();
                }
                return;
            }
        }
    }

    public boolean hasPostId(String post_id)
    {
        for (Database.MessageItem item : items) {
            if (item.post_id.equals(post_id)) {
                return true;
            }
        }
        return false;
    }

    public void imageLoaded(String url)
    {
        int cnt = items.length;
        for (int i = 0 ; i < cnt ; i++) {
            if (items[i].hasNullImage(url)) {
                items[i] = db.getStreamListItem(items[i].post_id);
            }
        }
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
            ((ItemDataView) ret).setOnClickItem(clickItem);
        }
        if (null != ret) {
            StreamDataView sdv = (StreamDataView) ret;
            sdv.put((Database.MessageItem)getItem(position));
        }
        return ret;
    }
}
