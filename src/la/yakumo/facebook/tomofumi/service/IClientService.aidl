package la.yakumo.facebook.tomofumi.service;

import la.yakumo.facebook.tomofumi.service.callback.ICommandCallback;

interface IClientService
{
    void registerCallback(ICommandCallback callback);
    void unregisterCallback(ICommandCallback callback);

    void reloadStream(boolean isClear);
    void reloadComment(String post_id);
    void reloadLike(String post_id);

    void addStreamMessage(String text);
    void addLink(String text, String linkUrl, String imageUrl);
}
