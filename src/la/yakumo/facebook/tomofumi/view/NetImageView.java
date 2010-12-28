package la.yakumo.facebook.tomofumi.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;

public class NetImageView
    extends ImageView
{
    private static final String TAG = Constants.LOG_TAG;

    private Handler handler = new Handler();
    private float imageWidth;
    private float imageHeight;

    public NetImageView(Context context)
    {
        super(context);
    }

    public NetImageView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public NetImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        TypedArray a =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.PostItemView);
        imageWidth =
            a.getFloat(
                R.styleable.NetImageView_imageWidth, 0);
        imageHeight =
            a.getFloat(
                R.styleable.NetImageView_imageHeight, 0);
        a.recycle();
    }

    @Override
    public void setImageURI(Uri uri)
    {
        String uriStr = uri.toString();
        setTag(uriStr);
        new ImageLoader(getContext()).execute(uriStr);
    }

    public class ImageLoader
        extends AsyncTask<String,Integer,Integer>
    {
        private Context context;

        public ImageLoader(Context context)
        {
            super();
            this.context = context;
        }

        protected Integer doInBackground(String... params)
        {
            Database db = new Database(context);
            SQLiteDatabase rdb = db.getReadableDatabase();
            Cursor c = rdb.query(
                "images",
                new String[] {"image_data"},
                "image_url=?",
                params,
                null, null, null);
            byte[] imageData = null;
            if (c.moveToFirst()) {
                imageData = c.getBlob(0);
            }

            String imgUri = params[0];
            if (null == imageData || imageData.length <= 0) {
                handler.post(new Runnable() {
                    public void run()
                    {
                        setImageResource(R.drawable.icon);
                    }
                });

                SQLiteDatabase wdb = db.getWritableDatabase();
                try {
                    URL url = new URL(imgUri);
                    HttpURLConnection conn =
                        (HttpURLConnection)url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setDoInput(true);
                    conn.connect();
                    int length = conn.getContentLength();

                    DataInputStream src =
                        new DataInputStream(conn.getInputStream());
                    ByteArrayOutputStream dst =
                        new ByteArrayOutputStream();
                    int ch;
                    while ((ch = src.read()) != -1) {
                        dst.write(ch);
                    }
                    imageData = dst.toByteArray();

                    wdb.beginTransaction();
                    ContentValues val = new ContentValues();
                    val.put("image_url", imgUri);
                    val.put("image_data", imageData);
                    wdb.insertWithOnConflict(
                        "images",
                        null,
                        val,
                        SQLiteDatabase.CONFLICT_REPLACE);
                    wdb.setTransactionSuccessful();
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                } finally {
                    wdb.endTransaction();
                }
            }

            if (null != imageData && imageData.length > 0) {
                final byte[] img = imageData;
                handler.post(new Runnable() {
                    public void run()
                    {
                        Bitmap bmp = BitmapFactory.decodeByteArray(
                            img, 0, img.length);
                        BitmapDrawable bd = new BitmapDrawable(bmp);
                        bd.setBounds(0, 0, (int)imageWidth, (int)imageHeight);
                        setImageDrawable(bd);
                    }
                });
            }

            return 0;
        }

        protected void onProgressUpdate(Integer... values)
        {
        }

        protected void onPostExecute(Integer result)
        {
        }
    }
}
