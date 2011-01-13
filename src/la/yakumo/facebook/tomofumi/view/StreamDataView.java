package la.yakumo.facebook.tomofumi.view;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;

public class StreamDataView
    extends ItemDataView
{
    private static final String TAG = Constants.LOG_TAG;

    private Database db;

    public StreamDataView(Context context)
    {
        super(context);
        db = new Database(context);
    }

    public StreamDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        db = new Database(context);
    }

    @Override
    public void reload()
    {
        put(db.getStreamListItem(messageItem.post_id));
    }

    @Override
    protected void updateData()
    {
        super.updateData();

        Database.StreamListItem streamItem =
            (Database.StreamListItem) messageItem;

        if (null != summaryView) {
            String name = streamItem.attachment_name;
            String caption = streamItem.attachment_caption;
            String link = streamItem.attachment_link;
            String icon = streamItem.attachment_icon;
            byte[] icon_data = streamItem.attachment_icon_data;
            String image = streamItem.attachment_image;
            byte[] image_data = streamItem.attachment_image_data;
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
                if (null != icon_data && null != appIconView) {
                    appIconView.setVisibility(View.VISIBLE);
                }
                setImageToView(appIconView, icon, icon_data);
                if (null != summaryIconView) {
                    if (null != image_data && image_data.length > 0){
                        summaryIconView.setVisibility(View.VISIBLE);
                    }
                    else {
                        summaryIconView.setVisibility(View.GONE);
                    }
                    setImageToView(summaryIconView, image, image_data);
                }
                summaryBaseView.setVisibility(View.VISIBLE);
                summaryView.setText(spannable, TextView.BufferType.SPANNABLE);
            }
            else {
                summaryBaseView.setVisibility(View.GONE);
            }
        }
        if (null != descriptionView) {
            if (null != streamItem.description &&
                streamItem.description.length() > 0) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(streamItem.description);
            }
            else {
                descriptionView.setVisibility(View.GONE);
            }
        }

        if (null != shareImageView && streamItem.show_share) {
            shareImageView.setVisibility(View.VISIBLE);
        }

        if (streamItem.updated) {
            setBackgroundResource(
                R.color.stream_updated_background_color);
        }
        else {
            setBackgroundResource(
                R.color.stream_no_updated_background_color);
        }
    }
}
