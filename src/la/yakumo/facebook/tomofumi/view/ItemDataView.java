package la.yakumo.facebook.tomofumi.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.StreamItemActivity;

public abstract class ItemDataView
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
    protected NetImageView streamIconView;
    protected NetImageView summaryIconView;
    protected NetImageView appIconView;
    protected View summaryBaseView;
    protected PostItemView commentView;
    protected PostItemView likeView;

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
        streamIconView = (NetImageView) findViewById(R.id.stream_icon);
        summaryIconView = (NetImageView) findViewById(R.id.summary_icon);
        appIconView = (NetImageView) findViewById(R.id.app_icon);
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
    }

    protected void onClickCommentView()
    {
        Log.i(TAG, "ItemDataView#onClickCommentView");
    }

    protected void onClickLikeView()
    {
        Log.i(TAG, "ItemDataView#onClickLikeView");
    }

    protected int layoutResourceId()
    {
        return R.layout.item_data;
    }

    public abstract void put(Object o);
    abstract void updateData();
}
