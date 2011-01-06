package la.yakumo.facebook.tomofumi.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;

public class LinkCheckActivity
    extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    private String link;
    private boolean isChecked = false;
    private ProgressDialog progress;
    private Handler handler = new Handler();
    private ArrayList<String> imageUris = new ArrayList<String>();
    private String title = "";
    private LinkCheckTask task = null;
    private GridView grid = null;

    private Pattern titleStartPattern = Pattern.compile("<[Tt][Ii][Tt][Ll][Ee]");
    private Pattern titleGetPattern = Pattern.compile(">([^<]*)<");
    private Pattern imgStartPattern = Pattern.compile("<[Ii][Mm][Gg]\\s");
    private Pattern imgEndPattern = Pattern.compile(">");
    private Pattern[] imgSrcPattern = new Pattern[] {
        Pattern.compile("src\\s*=\\s*\"([^\"]*)\""),
        Pattern.compile("src\\s*=\\s*'([^']*)'"),
        Pattern.compile("src\\s*=\\s*([^\\s>]*)")
    };

    @Override
    public void onCreate(Bundle bndl)
    {
        super.onCreate(bndl);

        Intent intent = getIntent();
        link = intent.getStringExtra("link");
        if (null == link) {
            finish();
            return;
        }

        setContentView(R.layout.post_link_check);
        TextView et = (TextView) findViewById(R.id.link_text);
        if (null != et) {
            et.setText(link);
        }
        grid = (GridView) findViewById(R.id.image_grid_list);
        grid.setAdapter(new ImageGridAdapter(this));
        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(
                AdapterView<?> parent,
                View view,
                int position,
                long id)
            {
                Adapter a = parent.getAdapter();
                byte[] image = null;
                String imageUrl = null;
                if (0 < position) {
                    ImageGridData imgData = (ImageGridData) a.getItem(position);
                    image = imgData.getImage();
                    imageUrl = imgData.getUrl();
                }

                Intent data = new Intent();
                data.putExtra("link", link);
                if (null != image) {
                    data.putExtra("image", image);
                }
                if (null != imageUrl) {
                    data.putExtra("image_url", imageUrl);
                }
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (null == link || isChecked) {
            return;
        }

        linkCheck(link);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (null != task) {
            task.cancel(true);
        }
    }

    public void onClickLinkCheck(View v)
    {
        TextView et = (TextView) findViewById(R.id.link_text);
        String link = et.getText().toString();
        isChecked = false;
        linkCheck(link);
    }

    private void linkCheck(String link)
    {
        Log.i(TAG, "link:"+link);
        new LinkCheckTask(link).execute();
    }

    public class LinkCheckTask
        extends AsyncTask<Integer,Integer,Integer>
    {
        HttpURLConnection conn = null;
        String title = null;
        String link;

        public LinkCheckTask(String link)
        {
            this.link = link;
        }

        protected Integer doInBackground(Integer... params)
        {
            final Resources res = LinkCheckActivity.this.getResources();
            final ImageGridAdapter gridAdapter =
                (ImageGridAdapter) grid.getAdapter();
            handler.post(new Runnable() {
                public void run()
                {
                    gridAdapter.clearImage();
                    grid.setSelection(0);
                    progress = ProgressDialog.show(
                        LinkCheckActivity.this,
                        null,
                        res.getText(R.string.progress_link_check_message),
                        true,
                        true,
                        new DialogInterface.OnCancelListener()
                        {
                            public void onCancel(DialogInterface dialog)
                            {
                                synchronized (conn) {
                                    if (null != conn) {
                                        conn.disconnect();
                                    }
                                }
                                Log.i(TAG, "cancel request");
                                LinkCheckTask.this.cancel(true);
                            }
                        });
                }
            });

            do {
                try {
                    URL url = new URL(link);
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setDoInput(true);
                    conn.connect();

                    if (isCancelled()) {
                        break;
                    }

                    String encoding = conn.getContentEncoding();
                    if (null == encoding) {
                        encoding = "UTF-8";
                    }
                    DataInputStream src =
                        new DataInputStream(conn.getInputStream());
                    ByteArrayOutputStream dst =
                        new ByteArrayOutputStream();
                    int ch;
                    while ((ch = src.read()) != -1) {
                        dst.write(ch);
                    }

                    if (isCancelled()) {
                        break;
                    }

                    byte[] html = dst.toByteArray();
                    BufferedReader reader =
                        new BufferedReader(
                            new InputStreamReader(
                                new ByteArrayInputStream(html),
                                encoding));
                    boolean titleReaded = false;
                    boolean connectNext = false;
                    String sLines = "";
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (connectNext) {
                            line = sLines + line;
                        }
                        connectNext = false;

                        if (!titleReaded) {
                            Matcher sm = titleStartPattern.matcher(line);
                            if (sm.find()) {
                                Matcher gm = titleGetPattern.matcher(line);
                                if (gm.find()) {
                                    sLines = "";
                                    title = gm.group(1);
                                    titleReaded = true;
                                }
                                else {
                                    connectNext = true;
                                    sLines = line;
                                }
                                continue;
                            }
                        }

                        Matcher ism = imgStartPattern.matcher(line);
                        while (ism.find()) {
                            //Log.i(TAG, "find image tag line:"+line);
                            Matcher iem = imgEndPattern.matcher(line);
                            if (iem.find(ism.start())) {
                                String imgtag =
                                    line.substring(ism.start(),
                                                   iem.start() + 1);
                                //Log.i(TAG, "find image tag:"+imgtag);
                                for (Pattern p : imgSrcPattern) {
                                    Matcher srcm = p.matcher(line);
                                    if (srcm.find()) {
                                        sLines = "";
                                        String imgSrc =
                                            Html.fromHtml(
                                                srcm.group(1)).toString();
                                        //Log.i(TAG, "find image source:"+imgSrc);
                                        URL imgUrl = new URL(url, imgSrc);
                                        String imgUrlStr = imgUrl.toString();
                                        if (!imageUris.contains(imgUrlStr)) {
                                            imageUris.add(imgUrlStr);
                                        }
                                        break;
                                    }
                                }
                            }
                            else {
                                connectNext = true;
                                sLines = sLines + line.substring(ism.start());
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }
                synchronized (conn) {
                    conn = null;
                }
            } while (false);

            handler.post(new Runnable() {
                public void run()
                {
                    TextView titleText =
                        (TextView) findViewById(R.id.title_display_text);
                    if (null != titleText && null != title) {
                        titleText.setText(title);
                    }
                    progress.dismiss();
                    progress = null;
                }
            });

            isChecked = true;

            if (null != grid) {
                for (String image : imageUris) {
                    Log.i(TAG, "image:"+image);
                    try {
                        URL url = new URL(image);
                        conn = (HttpURLConnection)url.openConnection();
                        conn.addRequestProperty("Referer", link);
                        conn.setInstanceFollowRedirects(true);
                        conn.setDoInput(true);
                        conn.connect();

                        if (isCancelled()) {
                            break;
                        }

                        DataInputStream src =
                            new DataInputStream(conn.getInputStream());
                        ByteArrayOutputStream dst =
                            new ByteArrayOutputStream();
                        int ch;
                        while ((ch = src.read()) != -1) {
                            dst.write(ch);
                        }

                        if (isCancelled()) {
                            break;
                        }

                        byte[] img = dst.toByteArray();
                        gridAdapter.addImage(
                            new ImageGridData(image, img));
                    } catch (MalformedURLException e) {
                    } catch (IOException e) {
                    }
                }
            }

            return 0;
        }

        protected void onProgressUpdate(Integer... values)
        {
        }

        protected void onPostExecute(Integer result)
        {
            Log.i(TAG, ""+this.getClass().toString()+"#onPostExecute:"+result);
        }

        protected void onCancelled()
        {
            if (null != progress) {
                Toast.makeText(
                    LinkCheckActivity.this,
                    R.string.progress_cancel_message,
                    Constants.PROGRESS_CANCELLED_TOAST_DURATION);
            }
        }
    }

    public class ImageGridData
    {
        private String url;
        private byte[] image;

        public ImageGridData(String url, byte[] image)
        {
            this.url = url;
            this.image = image;
        }

        public String getUrl()
        {
            return url;
        }

        public byte[] getImage()
        {
            return image;
        }
    }

    public class ImageGridAdapter
        extends BaseAdapter
    {
        private Context context;
        private ArrayList<ImageGridData> images = new ArrayList<ImageGridData>();
        private float imageWidth;
        private LayoutInflater layoutInflater;

        public ImageGridAdapter(Context context)
        {
            this.context = context;
            Resources res = context.getResources();
            imageWidth = res.getDimension(R.dimen.link_check_image_width);
            layoutInflater = LayoutInflater.from(context);
        }

        public void addImage(ImageGridData imgData)
        {
            images.add(imgData);
            handler.post(new Runnable() {
                public void run()
                {
                    notifyDataSetChanged();
                }
            });
        }

        public void clearImage()
        {
            images.clear();

            Resources res = context.getResources();
            try {
                InputStream is = res.openRawResource(R.drawable.no_image);
                byte[] no_image = new byte[is.available()];
                is.read(no_image);
                images.add(new ImageGridData(null, no_image));
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }
        }

        public int getCount()
        {
            return images.size();
        }

        public Object getItem(int position)
        {
            return images.get(position);
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View ret = convertView;
            ImageView img = null;
            if (null == ret) {
                ret = layoutInflater.inflate(R.layout.link_check_image, null);
                final View tmp = ret;
                ret.setOnFocusChangeListener(
                    new View.OnFocusChangeListener()
                    {
                        public void onFocusChange(View v, boolean hasFocus)
                        {
                            Log.i(TAG, "focus change, "+tmp+":"+v);
                        }
                    });
            }
            if (null != ret) {
                img = (ImageView) ret.findViewById(R.id.image_item);
            }
            if (null != img) {
                ImageGridData imgData = images.get(position);
                Bitmap bmp =
                    BitmapFactory.decodeByteArray(
                        imgData.getImage(), 0, imgData.getImage().length);
                if (null != bmp) {
                    float dh = imageWidth * bmp.getHeight();
                    dh /= bmp.getWidth();
                    Bitmap nimg =
                        Bitmap.createScaledBitmap(
                            bmp,
                            (int)imageWidth,
                            (int)dh,
                            true);
                    bmp.recycle();
                    img.setImageBitmap(nimg);
                }
            }
            return ret;
        }
    }
}
