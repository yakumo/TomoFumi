package la.yakumo.facebook.tomofumi.service.callback;

interface IStreamCallback
{
    void updatedStream(String errorMessage);
    void addedStream(String errorMessage);
}
