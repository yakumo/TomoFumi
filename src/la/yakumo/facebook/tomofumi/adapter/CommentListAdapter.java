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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int COMMENTMODE_COMMENT = 1;
    public static final int COMMENTMODE_LIKE = 0;

    private HashMap<Integer,View> updateChecker = new HashMap<Integer,View>();
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private MovementMethod movementmethod = LinkMovementMethod.getInstance();
    private Database.CommentListItem[] items = null;
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

    /*
    private boolean likeViewUpdate(int position, View v)
    {
        if (((Integer) v.getTag()).intValue() == position) {
            TextView likes = (TextView) v;
            CommentItem ci = (CommentItem) getItem(position);
            String tmp = "[";
            String sep = "";
            for (String i : ci.like_users) {
                tmp = tmp + sep + i;
                sep = ",";
            }
            tmp = tmp + "]";
            Log.i(TAG, "comment likes:"+tmp);
            int likeNum = ci.like_users.size();
            String likeFmt =
                resources.getQuantityString(
                    R.plurals.plural_like_format,
                    likeNum);
            likes.setText(String.format(likeFmt, likeNum));
            likes.setVisibility(View.VISIBLE);
            likes.setCompoundDrawablesWithIntrinsicBounds(
                ((ci.like_press)?
                 R.drawable.like_press:
                 ((ci.like_users.contains(user_id))?
                  R.drawable.like_light:
                  R.drawable.like_dark)),
                0, 0, 0);
            return true;
        }
        return false;
    }

    public void likeUpdating(int position, View v)
    {
        CommentItem ci = (CommentItem) getItem(position);
        ci.like_press = true;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(false);
        }
    }

    public void likeRegisted(int position, View v)
    {
        CommentItem ci = (CommentItem) getItem(position);
        ci.like_press = false;
        ci.like_users.add(user_id);
        if (likeViewUpdate(position, v)) {
            v.setEnabled(true);
        }
    }

    public void likeUnregisted(int position, View v)
    {
        CommentItem ci = (CommentItem) getItem(position);
        ci.like_press = false;
        ci.like_users.remove(user_id);
        if (likeViewUpdate(position, v)) {
            v.setEnabled(true);
        }
    }
    */

    public void setUserId(String user_id)
    {
        this.user_id = user_id;
    }

    public void reloadData(String postId)
    {
        items = db.getCommentListItems(postId);
        if (isFirstRead) {
            for (Database.CommentListItem item : items) {
                item.like.state_changing = true;
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
        int len = items.length;
        for (int i = 0 ; i < len ; i++) {
            if (post_id.equals(items[i].post_id)) {
                items[i].like.state_changing = false;
                items[i].like.enable_item = liked;
                items[i].like.count = likes;
                View v = topView.findViewWithTag(i);
                if (null != v) {
                    Log.i(TAG, "find view:"+v);
                    CommentDataView cv = (CommentDataView) v;
                    cv.put(items[i]);
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
        return items[position].data_mode;
    }

    public int getViewTypeCount()
    {
        return 2;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        Database.CommentListItem ci = items[position];
        ItemDataView ret = (ItemDataView) convertView;
        if (null == ret) {
            switch (ci.data_mode) {
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
            ret.put(ci);
            ret.setTag(position);
            updateChecker.put(position, ret);
        }
        return ret;
    }
}
