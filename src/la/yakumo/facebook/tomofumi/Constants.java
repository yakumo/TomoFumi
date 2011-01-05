package la.yakumo.facebook.tomofumi;

import la.yakumo.facebook.AppConst;

public class Constants
{
    public static final String LOG_TAG = "TomoFumi";

    public static final boolean IS_FREE = AppConst.IS_FREE;
    public static final String APP_ID = AppConst.APP_ID;
    public static final String API_KEY = AppConst.API_KEY;
    public static final String SECRET_KEY = AppConst.SECRET_KEY;

    public static final int PROGRESS_CANCELLED_TOAST_DURATION = 800;

    public static final int SESSION_UNKNOWN = 0;
    public static final int SESSION_STREAM_LIST = 1;
    public static final int SESSION_POST = 2;
    public static final int SESSION_UPDATE_COMMENTS = 3;

    public static final int COMMENTMODE_COMMENT = 1;
    public static final int COMMENTMODE_LIKE = 0;
}
