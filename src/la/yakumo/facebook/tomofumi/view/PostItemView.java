package la.yakumo.facebook.tomofumi.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import la.yakumo.facebook.tomofumi.data.Database;

public class PostItemView
    extends LinearLayout
{
    private static final String TAG = Constants.LOG_TAG;

    private TextView itemText;
    private ProgressBar itemProgress;
    private ImageView itemImage;

    private Drawable enabledIcon;
    private Drawable disabledIcon;
    private int puralText;

    public PostItemView(Context context)
    {
        super(context);
        privateInit(context);
    }

    public PostItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray a =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.PostItemView);
        enabledIcon =
            a.getDrawable(
                R.styleable.PostItemView_enabledIcon);
        disabledIcon =
            a.getDrawable(
                R.styleable.PostItemView_disabledIcon);
        puralText = a.getResourceId(R.styleable.PostItemView_puralText, 0);
        a.recycle();
        privateInit(context);
    }

    private void privateInit(Context context)
    {
        View.OnClickListener clickListener = new View.OnClickListener()
        {
            public void onClick(View v)
            {
                PostItemView.this.performClick();
            }
        };

        setLayoutParams(
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(
            LayoutInflater.from(context).inflate(
                R.layout.post_item_view,
                null),
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        itemText = (TextView) findViewById(R.id.item_text);
        itemProgress = (ProgressBar) findViewById(R.id.item_progress);
        itemImage = (ImageView) findViewById(R.id.item_image);

        itemText.setClickable(true);
        itemText.setOnClickListener(clickListener);
        itemProgress.setClickable(true);
        itemProgress.setOnClickListener(clickListener);
        itemImage.setClickable(true);
        itemImage.setOnClickListener(clickListener);

        setItemCount(0);
    }

    public void setPostItem(Database.PostItem postItem)
    {
        if (postItem.can_do) {
            itemImage.setImageDrawable(
                ((postItem.enable_item)?
                 enabledIcon:
                 disabledIcon));
            if (postItem.state_changing) {
                itemImage.setVisibility(View.INVISIBLE);
                itemProgress.setVisibility(View.VISIBLE);
            }
            else {
                itemProgress.setVisibility(View.INVISIBLE);
                itemImage.setVisibility(View.VISIBLE);
            }
            setItemCount(postItem.count);
            setVisibility(View.VISIBLE);
        }
        else {
            setVisibility(View.GONE);
        }
    }

    public void setItemCount(int count)
    {
        if (0 == puralText) {
            return;
        }

        Resources resources = getContext().getResources();
        String itemFmt = resources.getQuantityString(puralText, count);
        itemText.setText(String.format(itemFmt, count));
    }
}
