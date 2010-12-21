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

public class StreamListAdapter
    extends BaseAdapter
{
    private static final String TAG = Constants.LOG_TAG;

    private ArrayList<StreamListItem> items = new ArrayList<StreamListItem>();
    private Spannable.Factory factory = Spannable.Factory.getInstance();
    private MovementMethod movementmethod = LinkMovementMethod.getInstance();
    private Context context;
    private Resources resources;
    private Database db;
    private TextAppearanceSpan messageSpan;
    private TextAppearanceSpan usernameSpan;
    private TextAppearanceSpan summarySpan;

    public StreamListAdapter(Context context)
    {
        super();
        this.context = context;
        this.resources = context.getResources();
        this.db = new Database(context);
        this.messageSpan =
            new TextAppearanceSpan(context, R.style.StreamMessage);
        this.usernameSpan =
            new TextAppearanceSpan(context, R.style.StreamMessageUser);
        this.summarySpan =
            new TextAppearanceSpan(context, R.style.StreamSummary);
    }

    public void reloadData()
    {
        items.clear();

        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c =
            rdb.rawQuery(
                "SELECT *, stream._id as post_id"+
                " FROM stream"+
                " LEFT JOIN user"+
                " ON stream.actor_id=user._id"+
                " ORDER BY created_time DESC"+
                " LIMIT 400"+
                "",
                null);
        if (c.moveToFirst()) {
            do {
                StreamListItem li = new StreamListItem(c);
                items.add(li);
            } while (c.moveToNext());
        }

        notifyDataSetChanged();
    }

    private boolean likeViewUpdate(int position, View v)
    {
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
        return false;
    }

    public void likeUpdating(int position, View v)
    {
        StreamListItem li = (StreamListItem) getItem(position);
        li.like_posting = true;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(false);
        }
    }

    public void likeRegisted(int position, View v)
    {
        StreamListItem li = (StreamListItem) getItem(position);
        li.like_posting = false;
        li.like_count++;
        li.like_posted = true;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(true);
        }
    }

    public void likeUnregisted(int position, View v)
    {
        StreamListItem li = (StreamListItem) getItem(position);
        li.like_posting = false;
        li.like_count--;
        li.like_posted = false;
        if (likeViewUpdate(position, v)) {
            v.setEnabled(true);
        }
    }

    public String getPostId(int position)
    {
        return items.get(position).post_id;
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

    public View getView (int position, View convertView, ViewGroup parent)
    {
        View ret = convertView;

        StreamListItem li = (StreamListItem) getItem(position);
        if (null == ret) {
            ret = View.inflate(context, R.layout.stream_list_item, null);
            if (!Constants.IS_FREE) {
                TextView message = (TextView) ret.findViewById(R.id.message);
                if (null != message) {
                    message.setMovementMethod(movementmethod);
                }
                TextView summary = (TextView) ret.findViewById(R.id.summary);
                if (null != summary) {
                    summary.setMovementMethod(movementmethod);
                }
            }
        }
        if (null != ret) {
            TextView message = (TextView) ret.findViewById(R.id.message);
            TextView summary = (TextView) ret.findViewById(R.id.summary);
            TextView description = (TextView) ret.findViewById(R.id.description);
            TextView comments = (TextView) ret.findViewById(R.id.comments);
            TextView likes = (TextView) ret.findViewById(R.id.likes);
            NetImageView streamIcon = (NetImageView)
                ret.findViewById(R.id.stream_icon);
            NetImageView summaryIcon = (NetImageView)
                ret.findViewById(R.id.summary_icon);
            NetImageView appIcon = (NetImageView)
                ret.findViewById(R.id.app_icon);
            View summaryBase = ret.findViewById(R.id.summary_base);
            String post_id = li.post_id;
            if (null != streamIcon) {
                streamIcon.setImageResource(R.drawable.clear_image);
            }
            if (null != summaryIcon) {
                summaryIcon.setImageResource(R.drawable.clear_image);
            }
            if (null != appIcon) {
                appIcon.setVisibility(View.GONE);
            }
            if (null != message) {
                String usr = li.name;
                String msg = li.message;
                String userUrl = li.profile_url;
                if (null == usr) {
                    usr = "???";
                }
                String allMsg = usr + " " + msg;
                Spannable spannable = factory.newSpannable(allMsg);
                spannable.setSpan(
                    messageSpan, 0, allMsg.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(
                    usernameSpan, 0, usr.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (null != userUrl) {
                    try {
                        URL url = new URL(userUrl);
                        URLSpan s = new URLSpan(userUrl);
                        spannable.setSpan(
                            s, 0, usr.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (MalformedURLException e) {
                    }
                }
                message.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            if (null != summary) {
                String name = li.attachment_name;
                String caption = li.attachment_caption;
                String link = li.attachment_link;
                String icon = li.attachment_icon;
                String image = li.attachment_image;
                String msg = "";
                String sep = "";
                if (null != name && name.length() > 0) {
                    msg = msg + sep + name;
                    sep = "\n";
                }
                if (null != caption && caption.length() > 0) {
                    msg = msg + sep + caption;
                    sep = "\n";
                }
                if (msg.length() > 0) {
                    Spannable spannable = factory.newSpannable(msg);
                    spannable.setSpan(
                        summarySpan, 0, msg.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (null != name && name.length() > 0 &&
                        null != link && link.length() > 0) {
                        try {
                            URL url = new URL(link);
                            URLSpan s = new URLSpan(link);
                            spannable.setSpan(
                                s, 0, name.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } catch (MalformedURLException e) {
                            Log.i(TAG, "MalformedURLException", e);
                        }
                    }
                    if (null != icon && null != appIcon) {
                        appIcon.setVisibility(View.VISIBLE);
                        appIcon.setImageURI(Uri.parse(icon));
                    }
                    if (null != summaryIcon) {
                        if (null != image && image.length() > 0){
                            summaryIcon.setVisibility(View.VISIBLE);
                            summaryIcon.setImageURI(Uri.parse(image));
                        }
                        else {
                            summaryIcon.setVisibility(View.GONE);
                        }
                    }
                    summaryBase.setVisibility(View.VISIBLE);
                    summary.setText(spannable, TextView.BufferType.SPANNABLE);
                }
                else {
                    summaryBase.setVisibility(View.GONE);
                }
            }
            if (null != description) {
                if (null != li.description && li.description.length() > 0) {
                    description.setVisibility(View.VISIBLE);
                    description.setText(li.description);
                }
                else {
                    description.setVisibility(View.GONE);
                }
            }
            if (null != streamIcon) {
                if (null != li.pic_square) {
                    streamIcon.setImageURI(Uri.parse(li.pic_square));
                    streamIcon.setVisibility(View.VISIBLE);
                }
                else {
                    streamIcon.setVisibility(View.GONE);
                }
            }
            if (null != comments) {
                if (li.comment_can_post) {
                    int comNum = li.comment_count;
                    String comFmt =
                        resources.getQuantityString(
                            R.plurals.plural_comment_format,
                            comNum);
                    comments.setText(String.format(comFmt, comNum));
                    comments.setTag(post_id);
                    comments.setVisibility(View.VISIBLE);
                }
                else {
                    comments.setVisibility(View.GONE);
                }
            }
            if (null != likes) {
                if (li.like_posting) {
                    likes.setEnabled(false);
                }
                else {
                    likes.setEnabled(true);
                }
                if (li.comment_can_post) {
                    int likeNum = li.like_count;
                    String likeFmt =
                        resources.getQuantityString(
                            R.plurals.plural_like_format,
                            likeNum);
                    likes.setText(String.format(likeFmt, likeNum));
                    likes.setTag(new Integer(position));
                    likes.setVisibility(View.VISIBLE);
                    likes.setCompoundDrawablesWithIntrinsicBounds(
                        ((li.like_posting)?
                         R.drawable.like_press:
                         ((li.like_posted)?
                          R.drawable.like_light:
                          R.drawable.like_dark)),
                        0, 0, 0);
                }
                else {
                    likes.setVisibility(View.GONE);
                }
            }

            if (li.updated) {
                ret.setBackgroundResource(
                    R.color.stream_updated_background_color);
            }
            else {
                ret.setBackgroundResource(
                    R.color.stream_no_updated_background_color);
            }
        }
        return ret;
    }

    class StreamListItem
    {
        public String post_id;
        public long created_time;
        public String message;
        public String description;
        public String attachment_name;
        public String attachment_caption;
        public String attachment_link;
        public String attachment_image;
        public String attachment_icon;
        public int comment_count;
        public boolean comment_can_post;
        public int like_count;
        public boolean like_posted;
        public boolean can_like;
        public boolean like_posting;
        public boolean updated;
        public String name;
        public String pic_square;
        public String username;
        public String profile_url;
        public byte[] pic_data;

        public StreamListItem(Cursor c)
        {
            post_id = c.getString(c.getColumnIndex("post_id"));
            created_time = c.getLong(c.getColumnIndex("created_time"));
            message = c.getString(c.getColumnIndex("message"));
            description = c.getString(c.getColumnIndex("description"));
            attachment_name = c.getString(c.getColumnIndex("attachment_name"));
            attachment_caption = c.getString(c.getColumnIndex("attachment_caption"));
            attachment_link = c.getString(c.getColumnIndex("attachment_link"));
            attachment_image = c.getString(c.getColumnIndex("attachment_image"));
            attachment_icon = c.getString(c.getColumnIndex("attachment_icon"));
            comment_count = c.getInt(c.getColumnIndex("comment_count"));
            comment_can_post = (c.getInt(c.getColumnIndex("comment_can_post")) != 0);
            like_count = c.getInt(c.getColumnIndex("like_count"));
            like_posted = (c.getInt(c.getColumnIndex("like_posted")) != 0);
            can_like = (c.getInt(c.getColumnIndex("can_like")) != 0);
            like_posting = false;
            updated = (c.getInt(c.getColumnIndex("updated")) != 0);
            name = c.getString(c.getColumnIndex("name"));
            pic_square = c.getString(c.getColumnIndex("pic_square"));
            username = c.getString(c.getColumnIndex("username"));
            profile_url = c.getString(c.getColumnIndex("profile_url"));
            pic_data = c.getBlob(c.getColumnIndex("pic_data"));
        }
    }
}
