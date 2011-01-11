package la.yakumo.facebook.tomofumi.service.callback;

interface ICommandCallback
{
    void reloadStreamStart();
    void reloadStreamFinish(String errMsg);
    void reloadedComment(String post_id, String errMsg);
    void reloadedLike(String post_id, String errMsg);

    void readedImage(String url);
}
