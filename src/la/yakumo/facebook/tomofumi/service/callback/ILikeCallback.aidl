package la.yakumo.facebook.tomofumi.service.callback;

interface ILikeCallback
{
    void registerLike(String post_id);
    void registedLike(String post_id);
    void unregisterLike(String post_id);
    void unregistedLike(String post_id);
    void likeDataUpdated(String post_id, int likes, boolean liked);
}
