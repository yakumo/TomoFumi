package la.yakumo.facebook.tomofumi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database
    extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "facebook.db";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db)
    {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
