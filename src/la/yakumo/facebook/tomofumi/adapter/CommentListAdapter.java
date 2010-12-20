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
import org.json.JSONException;
import org.json.JSONObject;

public class CommentListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    public static final int COMMENTMODE_COMMENT = 1;
    public static final int COMMENTMODE_LIKE = 0;

    private ArrayList<CommentItem> items = new ArrayList<CommentItem>();
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private MovementMethod movementmethod = LinkMovementMethod.getInstance();
    private Context context;
    private Resources resources;
    private Database db;
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;

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
                likes.setTag(commentId);
                int likeNum = ci.like_users.size();
                String likeFmt =
                    resources.getQuantityString(
                        R.plurals.plural_like_format,
                        likeNum);
                likes.setText(String.format(likeFmt, likeNum));
                likes.setVisibility(View.VISIBLE);
            /*
                likes.setCompoundDrawablesWithIntrinsicBounds(
                    ((likePressed.containsKey(commentId))?
                     R.drawable.like_press:
                     ((lObj.has(userId))?
                      R.drawable.like_light:
                      R.drawable.like_dark)),
                    0, 0, 0);
            */
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

    class CommentItem
    {
        public String post_id;
        public int data_mode;
        public int like_count;
        public long time;
        public String message;
        public HashMap<String,String> like_users;
        public String name;
        public String pic_square;
        public String username;
        public String profile_url;
        public byte[] pic_data;

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

            like_users = new HashMap<String,String>();
            String lu = c.getString(c.getColumnIndex("likes"));
            if (null != lu) {
                if ("[]".equals(lu)) {
                    lu = "{}";
                }
                try {
                    Log.i(TAG, "likes:"+lu);
                    JSONObject a = new JSONObject(lu);
                    for (Iterator i = a.keys() ; i.hasNext() ; ) {
                        String k = (String) i.next();
                        like_users.put(k, a.getString(k));
                    }
                } catch (JSONException e) {
                    Log.i(TAG, "JSONException", e);
                }
            }

            /*
                "(_id INTEGER primary key unique"+
                ",post_id TEXT"+
                ",item_id TEXT"+
                ",data_mode INTEGER"+
                ",user_id INTEGER"+
                ",like_count INTEGER"+
                ",time INTEGER default 0"+
                ",message TEXT default ''"+
                ",likes TEXT default '[]'"+
                "(_id INTEGER primary key unique"+
                ",name TEXT"+
                ",pic_square TEXT"+
                ",username TEXT"+
                ",profile_url TEXT"+
                ",pic_data BLOB"+
            */
        }
    }
}


/*
    public class CommentListAdapter
        extends CursorAdapter
    {
        private int idxUserName;
        private int idxUserIcon;
        private int idxMessage;
        private int idxLikes;
        private int idxItemId;
        private int idxDataMode;
        private Spannable.Factory factory = Spannable.Factory.getInstance();
        private TextAppearanceSpan messageSpan =
            new TextAppearanceSpan(
                StreamItemActivity.this,
                R.style.StreamMessage);
        private TextAppearanceSpan usernameSpan =
            new TextAppearanceSpan(
                StreamItemActivity.this,
                R.style.StreamMessageUser);
        private MovementMethod movementmethod =
            LinkMovementMethod.getInstance();

        public CommentListAdapter(Context context, Cursor c)
        {
            super(context, c);
            idxUserName = c.getColumnIndex("name");
            idxUserIcon = c.getColumnIndex("pic_square");
            idxMessage = c.getColumnIndex("message");
            idxLikes = c.getColumnIndex("likes");
            idxItemId = c.getColumnIndex("item_id");
            idxDataMode = c.getColumnIndex("data_mode");
        }

        public void bindView(View view, Context context, Cursor cursor)
        {
            TextView message = (TextView) view.findViewById(R.id.message);
            TextView likes = (TextView) findViewById(R.id.likes);
            NetImageView icon = (NetImageView)
                view.findViewById(R.id.stream_icon);
            String commentId = cursor.getString(idxItemId);
            boolean isComment = (cursor.getInt(idxDataMode) == 1);
            Log.i(TAG, "datamode:"+isComment+", "+cursor.getString(idxMessage));
            if (null != likes) {
                if (isComment) {
                    likes.setTag(commentId);
                    String l = cursor.getString(idxLikes);
                    try {
                        JSONObject lObj = new JSONObject(l);
                        int likeNum = lObj.length();
                        String likeFmt =
                            resources.getQuantityString(
                                R.plurals.plural_like_format,
                                likeNum);
                        likes.setText(String.format(likeFmt, likeNum));
                        likes.setVisibility(View.VISIBLE);
                        likes.setCompoundDrawablesWithIntrinsicBounds(
                            ((likePressed.containsKey(commentId))?
                             R.drawable.like_press:
                            ((lObj.has(userId))?
                              R.drawable.like_light:
                              R.drawable.like_dark)),
                            0, 0, 0);
                    } catch (JSONException e) {
                        Log.i(TAG, "JSONException:"+l, e);
                        likes.setVisibility(View.GONE);
                    }
                }
                else {
                    likes.setVisibility(View.GONE);
                }
            }
            if (null != message) {
                message.setText("");
                String msg =
                    (cursor.isNull(idxMessage)?
                     "":
                     cursor.getString(idxMessage));
                String usr =
                    (cursor.isNull(idxUserName)?
                     "":
                     cursor.getString(idxUserName));
                String allMsg = usr + " " + msg;
                Spannable spannable = factory.newSpannable(allMsg);
                spannable.setSpan(
                    messageSpan, 0, allMsg.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(
                    usernameSpan, 0, usr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                message.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            if (null != icon) {
                String iconUri = cursor.getString(idxUserIcon);
                if (null != icon) {
                    icon.setImageURI(Uri.parse(iconUri));
                }
            }
        }

        public View newView (Context context, Cursor cursor, ViewGroup parent)
        {
            View ret =
                View.inflate(
                    context,
                    R.layout.stream_item_comment_item,
                    null);
            return ret;
        }
    }
*/
