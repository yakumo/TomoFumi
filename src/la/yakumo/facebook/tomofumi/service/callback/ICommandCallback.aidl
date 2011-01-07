package la.yakumo.facebook.tomofumi.service.callback;

interface ICommandCallback
{
    void reloadedStream(String errMsg);
    void reloadedComment(String post_id, String errMsg);
    void reloadedLike(String post_id, String errMsg);
}
