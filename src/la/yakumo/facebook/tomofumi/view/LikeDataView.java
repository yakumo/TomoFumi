package la.yakumo.facebook.tomofumi.view;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;

public class LikeDataView
    extends ItemDataView
{
    private Database.CommentListItem listItem;

    public LikeDataView(Context context)
    {
        super(context);
    }

    public LikeDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void put(Object obj)
    {
        listItem = (Database.CommentListItem) obj;
        updateData();
    }

    void updateData()
    {
        if (null != streamIconView) {
            streamIconView.setImageResource(R.drawable.clear_image);
        }
        if (null != summaryView) {
            summaryView.setVisibility(View.GONE);
        }
        if (null != summaryIconView) {
            summaryIconView.setVisibility(View.GONE);
        }
        if (null != appIconView) {
            appIconView.setVisibility(View.GONE);
        }
        if (null != descriptionView) {
            descriptionView.setVisibility(View.GONE);
        }
        if (null != commentView) {
            commentView.setVisibility(View.GONE);
        }
        if (null != likeView) {
            likeView.setVisibility(View.GONE);
        }
        if (null != messageView) {
            String usr = listItem.name;
            String userUrl = listItem.profile_url;
            if (null == usr) {
                usr = "???";
            }
            Spannable spannable = factory.newSpannable(usr);
            spannable.setSpan(
                messageSpan, 0, usr.length(),
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
            messageView.setText(spannable, TextView.BufferType.SPANNABLE);
        }
        if (null != streamIconView) {
            if (null != listItem.pic_square) {
                streamIconView.setImageURI(Uri.parse(listItem.pic_square));
                streamIconView.setVisibility(View.VISIBLE);
            }
            else {
                streamIconView.setVisibility(View.GONE);
            }
        }

        setBackgroundResource(
            R.color.stream_like_background_color);
    }
}
