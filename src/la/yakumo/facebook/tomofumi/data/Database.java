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
            StreamItemIndexs idx = new StreamItemIndexs(c);
            li.readFromDatabase(c, idx);
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
            StreamItemIndexs idx = new StreamItemIndexs(c);
            do {
                StreamListItem li = new StreamListItem();
                li.readFromDatabase(c, idx);
                items.add(li);
            } while (c.moveToNext());
        }
        int cnt = items.size();
        StreamListItem[] ret = new StreamListItem[cnt];
        System.arraycopy(items.toArray(), 0, ret, 0, cnt);

        return ret;
    }

    public CommentListItem[] getCommentListItems(String post_id)
    {
        ArrayList<CommentListItem> items = new ArrayList<CommentListItem>();
        SQLiteDatabase rdb = getReadableDatabase();
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
                    post_id
                });
        if (c.moveToFirst()) {
            CommentItemIndexs idx = new CommentItemIndexs(c);
            do {
                CommentListItem li = new CommentListItem();
                li.readFromDatabase(c, idx);
                items.add(li);
            } while (c.moveToNext());
        }
        int cnt = items.size();
        CommentListItem[] ret = new CommentListItem[cnt];
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
                "CREATE TABLE comments "+
                "(_id INTEGER primary key unique"+
                ",post_id TEXT"+
                ",item_id TEXT"+
                ",data_mode INTEGER"+
                ",user_id INTEGER"+
                ",time INTEGER default 0"+
                ",message TEXT default ''"+
                ",likes TEXT default '[]'"+
                ",like_count INTEGER default 0"+
                ",like_posted INTEGER default 0"+
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
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
        }
    }

    class StreamItemIndexs
    {
        public int post_id; /* post_id */
        public int created_time; /* created_time */
        public int message; /* message */
        public int description; /* description */
        public int attachment_name; /* attachment_name */
        public int attachment_caption; /* attachment_caption */
        public int attachment_link; /* attachment_link */
        public int attachment_image; /* attachment_image */
        public int attachment_icon; /* attachment_icon */
        public int comment_count; /* comment_count */
        public int comment_can_post; /* comment_can_post */
        public int like_count; /* like_count */
        public int like_posted; /* like_posted */
        public int can_like; /* can_like */
        public int updated; /* updated */
        public int name; /* name */
        public int pic_square; /* pic_square */
        public int username; /* username */
        public int profile_url; /* profile_url */
        public int pic_data; /* pic_data */

        StreamItemIndexs(Cursor c)
        {
            post_id = c.getColumnIndex("post_id");
            created_time = c.getColumnIndex("created_time");
            message = c.getColumnIndex("message");
            description = c.getColumnIndex("description");
            attachment_name = c.getColumnIndex("attachment_name");
            attachment_caption = c.getColumnIndex("attachment_caption");
            attachment_link = c.getColumnIndex("attachment_link");
            attachment_image = c.getColumnIndex("attachment_image");
            attachment_icon = c.getColumnIndex("attachment_icon");
            comment_count = c.getColumnIndex("comment_count");
            comment_can_post = c.getColumnIndex("comment_can_post");
            like_count = c.getColumnIndex("like_count");
            like_posted = c.getColumnIndex("like_posted");
            can_like = c.getColumnIndex("can_like");
            updated = c.getColumnIndex("updated");
            name = c.getColumnIndex("name");
            pic_square = c.getColumnIndex("pic_square");
            username = c.getColumnIndex("username");
            profile_url = c.getColumnIndex("profile_url");
            pic_data = c.getColumnIndex("pic_data");
        }
    }

    class CommentItemIndexs
    {
        public int item_id;
        public int data_mode;
        public int time;
        public int message;
        public int name;
        public int pic_square;
        public int username;
        public int profile_url;
        public int pic_data;
        public int likes;
        public int like_count;
        public int like_posted;

        CommentItemIndexs(Cursor c)
        {
            item_id = c.getColumnIndex("item_id");
            data_mode = c.getColumnIndex("data_mode");
            time = c.getColumnIndex("time");
            message = c.getColumnIndex("message");
            name = c.getColumnIndex("name");
            pic_square = c.getColumnIndex("pic_square");
            username = c.getColumnIndex("username");
            profile_url = c.getColumnIndex("profile_url");
            pic_data = c.getColumnIndex("pic_data");
            likes = c.getColumnIndex("likes");
            like_count = c.getColumnIndex("like_count");
            like_posted = c.getColumnIndex("like_posted");
        }
    }

    public class PostItem
    {
        public boolean enable_item;
        public boolean can_do;
        public boolean have_item;
        public boolean state_changing;
        public int count;
    }

    public class MessageItem
    {
        public String post_id;
        public long created_time;
        public String message;

        public String name;
        public String pic_square;
        public String username;
        public String profile_url;
        public byte[] pic_data;

        public PostItem comment = new PostItem();
        public PostItem like = new PostItem();
    }

    public class StreamListItem
        extends MessageItem
    {
        public String description;
        public String attachment_name;
        public String attachment_caption;
        public String attachment_link;
        public String attachment_image;
        public String attachment_icon;
        public boolean updated;

        void readFromDatabase(Cursor c, StreamItemIndexs idx)
        {
            post_id = c.getString(idx.post_id);
            created_time = c.getLong(idx.created_time);
            message = c.getString(idx.message);
            description = c.getString(idx.description);
            attachment_name = c.getString(idx.attachment_name);
            attachment_caption = c.getString(idx.attachment_caption);
            attachment_link = c.getString(idx.attachment_link);
            attachment_image = c.getString(idx.attachment_image);
            attachment_icon = c.getString(idx.attachment_icon);
            updated = (c.getInt(idx.updated) != 0);
            name = c.getString(idx.name);
            pic_square = c.getString(idx.pic_square);
            username = c.getString(idx.username);
            profile_url = c.getString(idx.profile_url);
            pic_data = c.getBlob(idx.pic_data);
            comment.enable_item = true;
            comment.count = c.getInt(idx.comment_count);
            comment.have_item = (comment.count > 0);
            comment.can_do = (c.getInt(idx.comment_can_post) != 0);
            comment.state_changing = false;
            like.enable_item = (c.getInt(idx.like_posted) != 0);
            like.count = c.getInt(idx.like_count);
            like.have_item = (like.count > 0);
            like.can_do = (c.getInt(idx.can_like) != 0);
            like.state_changing = false;
        }
    }

    public class CommentListItem
        extends MessageItem
    {
        public int data_mode;

        void readFromDatabase(Cursor c, CommentItemIndexs idx)
        {
            post_id = c.getString(idx.item_id);
            data_mode = c.getInt(idx.data_mode);
            created_time = c.getLong(idx.time);
            message = c.getString(idx.message);
            name = c.getString(idx.name);
            pic_square = c.getString(idx.pic_square);
            username = c.getString(idx.username);
            profile_url = c.getString(idx.profile_url);
            pic_data = c.getBlob(idx.pic_data);
            like.enable_item = (c.getInt(idx.like_posted) != 0);
            like.count = c.getInt(idx.like_count);
            like.have_item = true;
            like.can_do = true;
            like.state_changing = false;
        }
    }
}
