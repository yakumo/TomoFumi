package la.yakumo.facebook.tomofumi.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import la.yakumo.facebook.tomofumi.Constants;

public class Database
{
    private static DatabaseHelper helper = null;

    public Database(Context context)
    {
        if (null == helper) {
            helper = new DatabaseHelper(context);
        }
    }

    public SQLiteDatabase getReadableDatabase()
    {
        return helper.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase()
    {
        return helper.getWritableDatabase();
    }

    public void close()
    {
    }

    public StreamListItem getStreamListItem(String post_id)
    {
        StreamListItem li = new StreamListItem();
        li.post_id = post_id;

        SQLiteDatabase rdb = getReadableDatabase();
        Cursor c =
            rdb.rawQuery(
                "SELECT *, stream._id as post_id"+
                " FROM stream"+
                " LEFT JOIN user"+
                " ON stream.actor_id=user._id"+
                " WHERE stream._id=?"+
                "",
                new String[] {post_id});
        if (c.moveToFirst()) {
            int idx_post_id = c.getColumnIndex("post_id");
            int idx_created_time = c.getColumnIndex("created_time");
            int idx_message = c.getColumnIndex("message");
            int idx_description = c.getColumnIndex("description");
            int idx_attachment_name = c.getColumnIndex("attachment_name");
            int idx_attachment_caption = c.getColumnIndex("attachment_caption");
            int idx_attachment_link = c.getColumnIndex("attachment_link");
            int idx_attachment_image = c.getColumnIndex("attachment_image");
            int idx_attachment_icon = c.getColumnIndex("attachment_icon");
            int idx_comment_count = c.getColumnIndex("comment_count");
            int idx_comment_can_post = c.getColumnIndex("comment_can_post");
            int idx_like_count = c.getColumnIndex("like_count");
            int idx_like_posted = c.getColumnIndex("like_posted");
            int idx_can_like = c.getColumnIndex("can_like");
            int idx_updated = c.getColumnIndex("updated");
            int idx_name = c.getColumnIndex("name");
            int idx_pic_square = c.getColumnIndex("pic_square");
            int idx_username = c.getColumnIndex("username");
            int idx_profile_url = c.getColumnIndex("profile_url");
            int idx_pic_data = c.getColumnIndex("pic_data");
            li.post_id = c.getString(idx_post_id);
            li.created_time = c.getLong(idx_created_time);
            li.message = c.getString(idx_message);
            li.description = c.getString(idx_description);
            li.attachment_name = c.getString(idx_attachment_name);
            li.attachment_caption = c.getString(idx_attachment_caption);
            li.attachment_link = c.getString(idx_attachment_link);
            li.attachment_image = c.getString(idx_attachment_image);
            li.attachment_icon = c.getString(idx_attachment_icon);
            li.comment_count = c.getInt(idx_comment_count);
            li.comment_can_post = (c.getInt(idx_comment_can_post) != 0);
            li.like_count = c.getInt(idx_like_count);
            li.like_posted = (c.getInt(idx_like_posted) != 0);
            li.can_like = (c.getInt(idx_can_like) != 0);
            li.like_posting = false;
            li.updated = (c.getInt(idx_updated) != 0);
            li.name = c.getString(idx_name);
            li.pic_square = c.getString(idx_pic_square);
            li.username = c.getString(idx_username);
            li.profile_url = c.getString(idx_profile_url);
            li.pic_data = c.getBlob(idx_pic_data);
        }

        return li;
    }

    public StreamListItem[] getStreamListItems()
    {
        ArrayList<StreamListItem> items = new ArrayList<StreamListItem>();
        SQLiteDatabase rdb = getReadableDatabase();
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
            int idx_post_id = c.getColumnIndex("post_id");
            int idx_created_time = c.getColumnIndex("created_time");
            int idx_message = c.getColumnIndex("message");
            int idx_description = c.getColumnIndex("description");
            int idx_attachment_name = c.getColumnIndex("attachment_name");
            int idx_attachment_caption = c.getColumnIndex("attachment_caption");
            int idx_attachment_link = c.getColumnIndex("attachment_link");
            int idx_attachment_image = c.getColumnIndex("attachment_image");
            int idx_attachment_icon = c.getColumnIndex("attachment_icon");
            int idx_comment_count = c.getColumnIndex("comment_count");
            int idx_comment_can_post = c.getColumnIndex("comment_can_post");
            int idx_like_count = c.getColumnIndex("like_count");
            int idx_like_posted = c.getColumnIndex("like_posted");
            int idx_can_like = c.getColumnIndex("can_like");
            int idx_updated = c.getColumnIndex("updated");
            int idx_name = c.getColumnIndex("name");
            int idx_pic_square = c.getColumnIndex("pic_square");
            int idx_username = c.getColumnIndex("username");
            int idx_profile_url = c.getColumnIndex("profile_url");
            int idx_pic_data = c.getColumnIndex("pic_data");
            do {
                StreamListItem li = new StreamListItem();
                li.post_id = c.getString(idx_post_id);
                li.created_time = c.getLong(idx_created_time);
                li.message = c.getString(idx_message);
                li.description = c.getString(idx_description);
                li.attachment_name = c.getString(idx_attachment_name);
                li.attachment_caption = c.getString(idx_attachment_caption);
                li.attachment_link = c.getString(idx_attachment_link);
                li.attachment_image = c.getString(idx_attachment_image);
                li.attachment_icon = c.getString(idx_attachment_icon);
                li.comment_count = c.getInt(idx_comment_count);
                li.comment_can_post = (c.getInt(idx_comment_can_post) != 0);
                li.like_count = c.getInt(idx_like_count);
                li.like_posted = (c.getInt(idx_like_posted) != 0);
                li.can_like = (c.getInt(idx_can_like) != 0);
                li.like_posting = false;
                li.updated = (c.getInt(idx_updated) != 0);
                li.name = c.getString(idx_name);
                li.pic_square = c.getString(idx_pic_square);
                li.username = c.getString(idx_username);
                li.profile_url = c.getString(idx_profile_url);
                li.pic_data = c.getBlob(idx_pic_data);
                items.add(li);
            } while (c.moveToNext());
        }
        int cnt = items.size();
        StreamListItem[] ret = new StreamListItem[cnt];
        System.arraycopy(items.toArray(), 0, ret, 0, cnt);

        return ret;
    }

    class DatabaseHelper
        extends SQLiteOpenHelper
    {
        private static final String TAG = Constants.LOG_TAG;

        private static final String DATABASE_NAME = "facebook.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db)
        {
            Log.i(TAG, "Database#onCreate");
            db.execSQL(
                "CREATE TABLE stream "+
                "(_id TEXT primary key unique"+
                ",app_id INTEGER"+
                ",actor_id INTEGER"+
                ",target_id TEXT"+
                ",created_time INTEGER"+
                ",updated_time INTEGER"+
                ",message TEXT"+
                ",description TEXT default ''"+
                ",attachment_name TEXT"+
                ",attachment_caption TEXT"+
                ",attachment_link TEXT"+
                ",attachment_image TEXT"+
                ",attachment_icon TEXT"+
                ",comment_count INTEGER default 0"+
                ",comment_can_post INTEGER default 0"+
                ",like_count INTEGER default 0"+
                ",like_posted INTEGER default 0"+
                ",can_like INTEGER default 0"+
                ",updated INTEGER default 0"+
                ");");
            db.execSQL(
                "CREATE TABLE user "+
                "(_id INTEGER primary key unique"+
                ",name TEXT"+
                ",pic_square TEXT"+
                ",username TEXT"+
                ",profile_url TEXT"+
                ",pic_data BLOB"+
                ");");
            db.execSQL(
                "CREATE TABLE images "+
                "(_id INTEGER primary key unique"+
                ",read_time INTEGER"+
                ",image_url TEXT"+
                ",image_data BLOB"+
                ");");
            db.execSQL(
                "CREATE TABLE comments "+
                "(_id INTEGER primary key unique"+
                ",post_id TEXT"+
                ",item_id TEXT"+
                ",data_mode INTEGER"+
                ",user_id INTEGER"+
                ",like_count INTEGER"+
                ",time INTEGER default 0"+
                ",message TEXT default ''"+
                ",likes TEXT default '[]'"+
                ");");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
        }
    }

    public class StreamListItem
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
    }
}
