package la.yakumo.facebook.tomofumi.service;

interface IClientServiceCallback
{
    void updatedStream(String errorMessage);
    void updatedComment(String post_id, String errorMessage);
    void updatedLike(String post_id, String errorMessage);
}
