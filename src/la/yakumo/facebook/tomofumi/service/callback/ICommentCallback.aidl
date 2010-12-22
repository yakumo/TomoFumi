package la.yakumo.facebook.tomofumi.service.callback;

interface ICommentCallback
{
    void updatedComment(String post_id, String errMsg);
    void registerComment(String post_id);
    void registedComment(String post_id, String comment_post_id, String errMsg);
}
