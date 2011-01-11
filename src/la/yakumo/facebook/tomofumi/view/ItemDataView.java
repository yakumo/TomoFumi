package la.yakumo.facebook.tomofumi.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.net.MalformedURLException;
import java.net.URL;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;

public class ItemDataView
    extends LinearLayout
{
    private static final String TAG = Constants.LOG_TAG;

    protected Spannable.Factory factory = Spannable.Factory.getInstance();
    protected MovementMethod movementmethod = LinkMovementMethod.getInstance();
    protected Resources resources;
    protected TextAppearanceSpan messageSpan;
    protected TextAppearanceSpan usernameSpan;
    protected TextAppearanceSpan summarySpan;
    protected TextView messageView;
    protected TextView summaryView;
    protected TextView descriptionView;
    protected ImageView shareImageView;
    protected ImageView streamIconView;
    protected ImageView summaryIconView;
    protected ImageView appIconView;
    protected View summaryBaseView;
    protected PostItemView commentView;
    protected PostItemView likeView;

    protected Database.MessageItem messageItem = null;

    public ItemDataView(Context context)
    {
        super(context);
        privateInit(context);
    }

    public ItemDataView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        privateInit(context);
    }

    private void privateInit(Context context)
    {
        resources = context.getResources();

        messageSpan = new TextAppearanceSpan(context, R.style.StreamMessage);
        usernameSpan = new TextAppearanceSpan(context, R.style.StreamMessageUser);
        summarySpan = new TextAppearanceSpan(context, R.style.StreamSummary);

        addView(
            LayoutInflater.from(context).inflate(layoutResourceId(), null),
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        messageView = (TextView) findViewById(R.id.message);
        summaryView = (TextView) findViewById(R.id.summary);
        descriptionView = (TextView) findViewById(R.id.description);
        shareImageView = (ImageView) findViewById(R.id.share_button);
        streamIconView = (ImageView) findViewById(R.id.stream_icon);
        summaryIconView = (ImageView) findViewById(R.id.summary_icon);
        appIconView = (ImageView) findViewById(R.id.app_icon);
        summaryBaseView = findViewById(R.id.summary_base);
        commentView = (PostItemView) findViewById(R.id.comment_item);
        likeView = (PostItemView) findViewById(R.id.like_item);

        if (!Constants.IS_FREE) {
            if (null != messageView) {
                messageView.setMovementMethod(movementmethod);
            }
            if (null != summaryView) {
                summaryView.setMovementMethod(movementmethod);
            }
        }

        if (null != commentView) {
            commentView.setOnClickListener(new OnClickListener() {
                public void onClick (View v)
                {
                    onClickCommentView();
                }
            });
        }
        if (null != likeView) {
            likeView.setOnClickListener(new OnClickListener() {
                public void onClick (View v)
                {
                    onClickLikeView();
                }
            });
        }
        if (null != shareImageView) {
            shareImageView.setOnClickListener(new OnClickListener() {
                public void onClick (View v)
                {
                    onClickShareView();
                }
            });
        }
    }

    protected void onClickCommentView()
    {
        Log.i(TAG, "ItemDataView#onClickCommentView");
    }

    protected void onClickLikeView()
    {
        Log.i(TAG, "ItemDataView#onClickLikeView");
    }

    protected void onClickShareView()
    {
        Log.i(TAG, "ItemDataView#onClickShareView");
    }

    protected int layoutResourceId()
    {
        return R.layout.item_data;
    }

    public void put(Database.MessageItem item)
    {
        messageItem = item;
        updateData();
    }

    protected void updateData()
    {
        if (null != streamIconView) {
            streamIconView.setImageResource(R.drawable.clear_image);
        }
        if (null != summaryIconView) {
            summaryIconView.setImageResource(R.drawable.clear_image);
        }
        if (null != appIconView) {
            appIconView.setVisibility(View.GONE);
        }

        if (null != messageView) {
            String usr = messageItem.name;
            String msg = messageItem.message;
            String userUrl = messageItem.profile_url;
            if (null == usr) {
                usr = "???";
            }
            String allMsg = usr + " " + msg;
            Spannable spannable = factory.newSpannable(allMsg);
            spannable.setSpan(
                messageSpan, 0, allMsg.length(),
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
            Log.i(TAG, "pic_data:"+messageItem.pic_data);
            streamIconView.setVisibility(View.VISIBLE);
            if (null != messageItem.pic_data) {
                Bitmap bmp =
                    BitmapFactory.decodeByteArray(
                        messageItem.pic_data, 0, messageItem.pic_data.length);
                streamIconView.setImageBitmap(bmp);
            }
            if (null != messageItem.pic_square) {
                streamIconView.setTag(messageItem.pic_square);
            }
        }

        if (null != commentView) {
            commentView.setPostItem(messageItem.comment);
        }
        if (null != likeView) {
            likeView.setPostItem(messageItem.like);
        }
    }
}
