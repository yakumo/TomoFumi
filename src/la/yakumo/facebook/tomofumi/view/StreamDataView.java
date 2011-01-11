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

    public StreamDataView(Context context)
    {
        super(context);
    }

    public StreamDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
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
            String image = streamItem.attachment_image;
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
                if (null != icon && null != appIconView) {
                    appIconView.setVisibility(View.VISIBLE);
                    appIconView.setImageURI(Uri.parse(icon));
                }
                if (null != summaryIconView) {
                    if (null != image && image.length() > 0){
                        summaryIconView.setVisibility(View.VISIBLE);
                        summaryIconView.setImageURI(Uri.parse(image));
                    }
                    else {
                        summaryIconView.setVisibility(View.GONE);
                    }
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
        if (null != streamIconView) {
            if (null != streamItem.pic_square) {
                streamIconView.setImageURI(Uri.parse(streamItem.pic_square));
                streamIconView.setVisibility(View.VISIBLE);
            }
            else {
                streamIconView.setVisibility(View.GONE);
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
