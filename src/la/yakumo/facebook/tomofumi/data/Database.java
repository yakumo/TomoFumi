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
            "(_id INTEGER primary key unique"+
            ",post_id TEXT unique not null"+
            ",app_id INTEGER"+
            ",actor_id INTEGER"+
            ",target_id TEXT"+
            ",created_time INTEGER"+
            ",updated_time INTEGER"+
            ",message TEXT"+
            ",app_data BLOB"+
            ",comments INTEGER"+
            ",likes INTEGER"+
            ",updated INTEGER"+
            ");");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
