package la.yakumo.facebook.tomofumi.service.callback;

interface ICommentCallback
{
    void updatedComment(String post_id, String errorMessage);
    void addedComment(String post_id, String errorMessage);
}
