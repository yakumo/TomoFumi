package la.yakumo.facebook.tomofumi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;

public class Database
    extends SQLiteOpenHelper
{
    private static final String TAG = Constants.LOG_TAG;

    private static final String DATABASE_NAME = "facebook.db";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context)
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
            ",comments INTEGER default 0"+
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
