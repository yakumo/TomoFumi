package la.yakumo.facebook.tomofumi.service.callback;

interface ILoginCallback
{
    void loggedIn(String userID);
    void loginFailed(String reason);
}
