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
import la.yakumo.facebook.tomofumi.view.NetImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int COMMENTMODE_COMMENT = 1;
    public static final int COMMENTMODE_LIKE = 0;

    private ArrayList<CommentItem> items = new ArrayList<CommentItem>();
    private HashMap<String,View> likePressed = new HashMap<String,View>();
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private MovementMethod movementmethod = LinkMovementMethod.getInstance();
    private Context context;
    private Resources resources;
    private Database db;
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;
    private String user_id;

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

    public void setUserId(String user_id)
    {
        this.user_id = user_id;
    }

    public void reloadData(String postId)
    {
        items.clear();

        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c =
            rdb.rawQuery(
                "SELECT *"+
                " FROM comments"+
                " LEFT JOIN user"+
                " ON comments.user_id=user._id"+
                " WHERE comments.post_id=?"+
                " ORDER BY"+
                " data_mode DESC"+
                ",time ASC"+
                ",name ASC"+
                "",
                new String[] {
                    postId
                });
        if (c.moveToFirst()) {
            do {
                CommentItem item = new CommentItem(c);
                items.add(item);
            } while (c.moveToNext());
        }

        notifyDataSetChanged();
    }

    public int getCount()
    {
        return items.size();
    }

    public Object getItem (int position)
    {
        return items.get(position);
    }

    public long getItemId (int position)
    {
        return position;
    }

    public int getItemViewType(int position)
    {
        CommentItem ci = (CommentItem) getItem(position);
        return ci.data_mode;
    }

    public int getViewTypeCount()
    {
        return 2;
    }

    public View getView (int position, View convertView, ViewGroup parent)
    {
        CommentItem ci = (CommentItem) getItem(position);
        View ret = convertView;
        if (null == ret) {
            int resNum = R.layout.stream_item_comment_item;
            switch (ci.data_mode) {
            case COMMENTMODE_COMMENT:
                resNum = R.layout.stream_item_comment_item;
                break;
            case COMMENTMODE_LIKE:
                resNum = R.layout.stream_item_like_item;
                break;
            default:
                break;
            }
            ret = View.inflate(context, resNum, null);
            TextView message = (TextView) ret.findViewById(R.id.message);
            if (null != message) {
                message.setMovementMethod(movementmethod);
            }
        }
        if (null != ret) {
            TextView message = (TextView) ret.findViewById(R.id.message);
            NetImageView icon = (NetImageView)
                ret.findViewById(R.id.stream_icon);
            String commentId = ci.post_id;
            if (COMMENTMODE_COMMENT == ci.data_mode) {
                TextView likes = (TextView) ret.findViewById(R.id.likes);
                likes.setTag(position);
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
            }
            if (null != message) {
                message.setText("");
                String msg = (null == ci.message)? "": ci.message;
                String usr = (null == ci.name)? "": ci.name;
                String allMsg = usr + " " + msg;
                Spannable spannable = factory.newSpannable(allMsg);
                spannable.setSpan(
                    messageSpan, 0, allMsg.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(
                    usernameSpan, 0, usr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (null != ci.profile_url) {
                    try {
                        URL url = new URL(ci.profile_url);
                        URLSpan s = new URLSpan(ci.profile_url);
                        spannable.setSpan(
                            s, 0, usr.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (MalformedURLException e) {
                    }
                }
                message.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            if (null != icon) {
                if (null != icon) {
                    icon.setImageURI(Uri.parse(ci.pic_square));
                }
            }
        }
        return ret;
    }

    public class CommentItem
    {
        public String post_id;
        public int data_mode;
        public int like_count;
        public long time;
        public String message;
        public ArrayList<String> like_users;
        public String name;
        public String pic_square;
        public String username;
        public String profile_url;
        public byte[] pic_data;
        public boolean like_press;

        public CommentItem(Cursor c)
        {
            post_id = c.getString(c.getColumnIndex("item_id"));
            data_mode = c.getInt(c.getColumnIndex("data_mode"));
            like_count = c.getInt(c.getColumnIndex("like_count"));
            time = c.getLong(c.getColumnIndex("time"));
            message = c.getString(c.getColumnIndex("message"));
            name = c.getString(c.getColumnIndex("name"));
            pic_square = c.getString(c.getColumnIndex("pic_square"));
            username = c.getString(c.getColumnIndex("username"));
            profile_url = c.getString(c.getColumnIndex("profile_url"));
            pic_data = c.getBlob(c.getColumnIndex("pic_data"));

            like_press = false;

            like_users = new ArrayList<String>();
            String lu = c.getString(c.getColumnIndex("likes"));
            if (null != lu) {
                try {
                    Log.i(TAG, "comment likes:"+lu);
                    JSONArray a = new JSONArray(lu);
                    for (int i = 0 ; i < a.length() ; i++) {
                        like_users.add(a.getString(i));
                    }
                } catch (JSONException e) {
                    Log.i(TAG, "JSONException", e);
                }
            }
        }
    }
}
