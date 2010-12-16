package la.yakumo.facebook.tomofumi.service;

import la.yakumo.facebook.tomofumi.service.IClientServiceCallback;

interface IClientService
{
    void registerCallback(IClientServiceCallback callback);
    void unregisterCallback(IClientServiceCallback callback);

    void login(int sessionID);

    int updateStream();
    int updateComment(String post_id);
    int updateLike(String post_id);

    int addStream(String text);
    int addComment(String post_id, String text);
    int addStreamLike(String post_id);
    int addCommentLike(String post_id);
}
