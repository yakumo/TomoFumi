package la.yakumo.facebook.tomofumi.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import java.util.HashMap;
import java.util.Iterator;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;
import la.yakumo.facebook.tomofumi.view.ItemDataView;
import la.yakumo.facebook.tomofumi.view.CommentDataView;
import la.yakumo.facebook.tomofumi.view.LikeDataView;
import la.yakumo.facebook.tomofumi.view.StreamDataView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int COMMENTMODE_MESSAGE = 2;
    public static final int COMMENTMODE_COMMENT = 1;
    public static final int COMMENTMODE_LIKE = 0;

    private HashMap<Integer,View> updateChecker = new HashMap<Integer,View>();
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private MovementMethod movementmethod = LinkMovementMethod.getInstance();
    private Database.MessageItem[] items = null;
    private Context context;
    private Resources resources;
    private Database db;
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;
    private String user_id;
    private boolean isFirstRead = true;

    public CommentListAdapter(Context context)
    {
        super();
        this.context = context;
        this.resources = context.getResources();
        this.db = new Database(context);
        messageSpan =
            new TextAppearanceSpan(
                context,
                R.style.StreamMessage);
        usernameSpan =
            new TextAppearanceSpan(
                context,
                R.style.StreamMessageUser);
    }

    public void setUserId(String user_id)
    {
        this.user_id = user_id;
    }

    public void reloadData(String postId)
    {
        Database.CommentListItem[] list = db.getCommentListItems(postId);
        items = new Database.MessageItem[list.length + 1];
        System.arraycopy(list, 0, items, 1, list.length);
        Database.StreamListItem sli = db.getStreamListItem(postId);
        sli.comment.can_do = false;
        items[0] = sli;

        if (isFirstRead) {
            for (Database.MessageItem item : items) {
                if (item instanceof Database.CommentListItem) {
                    item.like.state_changing = true;
                }
                else {
                    sli = (Database.StreamListItem)item;
                    sli.updated = true;
                    if (!Constants.IS_FREE) {
                        sli.show_share = true;
                    }
                }
            }
            isFirstRead = false;
        }
        notifyDataSetChanged();
    }

    public void updateLikeCount(
        View topView,
        String post_id,
        int likes,
        boolean liked)
    {
        if (null == items) {
            return;
        }

        int len = items.length;
        for (int i = 0 ; i < len ; i++) {
            if (post_id.equals(items[i].post_id)) {
                items[i].like.state_changing = false;
                items[i].like.enable_item = liked;
                items[i].like.count = likes;
                View v = topView.findViewWithTag(i);
                if (null != v) {
                    Log.i(TAG, "find view:"+v);
                    if (v instanceof CommentDataView) {
                        CommentDataView cv = (CommentDataView) v;
                        cv.put(items[i]);
                    }
                }
            }
        }
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

    public int getItemViewType(int position)
    {
        if (null == items) {
            return 0;
        }
        if (items[position] instanceof Database.CommentListItem) {
            return ((Database.CommentListItem)items[position]).data_mode;
        }
        return COMMENTMODE_MESSAGE;
    }

    public int getViewTypeCount()
    {
        return 3;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        if (null == items) {
            return null;
        }

        Database.MessageItem ci = items[position];
        ItemDataView ret = (ItemDataView) convertView;
        int viewType = getItemViewType(position);
        if (null == ret) {
            switch (viewType) {
            case COMMENTMODE_MESSAGE:
                ret = new StreamDataView(context);
                break;
            case COMMENTMODE_COMMENT:
                ret = new CommentDataView(context);
                break;
            case COMMENTMODE_LIKE:
                ret = new LikeDataView(context);
                break;
            default:
                break;
            }
        }
        if (null != ret) {
            switch (viewType) {
            case COMMENTMODE_MESSAGE:
                ret.put(ci);
                ret.setTag(position);
                break;
            case COMMENTMODE_COMMENT:
            case COMMENTMODE_LIKE:
                ret.put(ci);
                ret.setTag(position);
                updateChecker.put(position, ret);
                break;
            default:
                break;
            }
        }
        return ret;
    }
}
