package la.yakumo.facebook.tomofumi.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ListView;
import la.yakumo.facebook.tomofumi.R;

public class CommonListView
    extends ListView
{
    public CommonListView(Context context)
    {
        super(context);
    }

    public CommonListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CommonListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public int getSolidColor()
    {
        return getContext().getResources().getColor(R.color.list_fading_color);
    }
}
