package la.yakumo.facebook.tomofumi.service;

import la.yakumo.facebook.tomofumi.service.callback.ILoginCallback;
import la.yakumo.facebook.tomofumi.service.callback.IStreamCallback;
import la.yakumo.facebook.tomofumi.service.callback.ICommentCallback;
import la.yakumo.facebook.tomofumi.service.callback.ILikeCallback;

interface IClientService
{
    void registerLoginCallback(ILoginCallback callback);
    void unregisterLoginCallback(ILoginCallback callback);
    void registerStreamCallback(IStreamCallback callback);
    void unregisterStreamCallback(IStreamCallback callback);
    void registerCommentCallback(ICommentCallback callback);
    void unregisterCommentCallback(ICommentCallback callback);
    void registerLikeCallback(ILikeCallback callback);
    void unregisterLikeCallback(ILikeCallback callback);

    void login(int sessionID);

    int updateStream();
    int updateComment(String post_id);
    int updateLike(String post_id);

    int addStream(String text);
    int addComment(String post_id, String text);
    int toggleStreamLike(String post_id);
    int registStreamLike(String post_id);
    int unregistStreamLike(String post_id);
    int toggleCommentLike(String post_id);
    int registCommentLike(String post_id);
    int unregistCommentLike(String post_id);
}
