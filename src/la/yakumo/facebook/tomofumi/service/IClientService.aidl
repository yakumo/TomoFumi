package la.yakumo.facebook.tomofumi.service;

import la.yakumo.facebook.tomofumi.service.IClientServiceCallback;

interface IClientService
{
    void registerCallback(IClientServiceCallback callback);
    void unregisterCallback(IClientServiceCallback callback);

    void updateStream();
    void updateComment(String post_id);
    void updateLike(String post_id);

    void addStream(String text);
    void addComment(String post_id, String text);
    void addStreamLike(String post_id);
    void addCommentLike(String post_id);
}
