package la.yakumo.facebook.tomofumi.service;

interface IClientServiceCallback
{
    void loggedIn(String userID);
    void loginFailed(String reason);
    void updatedStream(String errorMessage);
    void updatedComment(String post_id, String errorMessage);
    void updatedLike(String post_id, String errorMessage);
    void updateProgress(int now, int max, String msg);
    void addedStream(String errorMessage);
    void addedComment(String post_id, String errorMessage);
    void addedLike(String post_id, String errorMessage);
}
