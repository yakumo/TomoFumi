package la.yakumo.facebook.tomofumi.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.R;
import org.json.JSONException;
import org.json.JSONObject;

public class Facebook
{
    private static final String TAG = Constants.LOG_TAG;

    private static final String[] PERMISSIONS = {
        "offline_access",
        "publish_stream",
        "read_stream",
    };

    private static Facebook fb = null;
    private Context context;
    private SharedPreferences pref;
    private com.facebook.android.Facebook facebook;
    private String userID = null;

    public static final Facebook getInstance(Context context)
    {
        if (null == fb) {
            fb = new Facebook(context);
        }
        return fb;
    }

    private Facebook(Context context)
    {
        this.context = context;
        pref = context.getSharedPreferences("facebook", Context.MODE_PRIVATE);
        facebook = new com.facebook.android.Facebook(Constants.APP_ID);
        facebook.setAccessToken(pref.getString("access_token", null));
        facebook.setAccessExpires(pref.getLong("access_exires", 0));
        userID = pref.getString("user_id", null);
    }

    public boolean loginCheck()
    {
        if (!facebook.isSessionValid()) {
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
            return false;
        }
        return true;
    }

    public void login(final Activity parent, final OnLoginCallback callback)
    {
        facebook.authorize(
            parent,
            PERMISSIONS,
            -1,
            new DialogListener()
            {
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(
                        "access_token",
                        facebook.getAccessToken());
                    edit.putLong(
                        "access_expires",
                        facebook.getAccessExpires());
                    edit.commit();

                    if (null == userID) {
                        com.facebook.android.AsyncFacebookRunner r =
                            new AsyncFacebookRunner(facebook);
                        r.request(
                            "me",
                            new AsyncFacebookRunner.RequestListener() {
                                public void onComplete(
                                    String response)
                                {
                                    parent.finish();
                                    try {
                                        JSONObject me = Util.parseJson(response);
                                        userID = me.getString("id");
                                        SharedPreferences.Editor e = pref.edit();
                                        e.putString("user_id", userID);
                                        e.commit();
                                        callback.onLoginSuccess();
                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSONException", e);
                                        callback.onLoginFail(e.getMessage());
                                    } catch (FacebookError e){
                                        Log.e(TAG, "FacebookError", e);
                                        callback.onLoginFail(e.getMessage());
                                    }
                                }
                                public void onIOException(
                                    IOException e)
                                {
                                    parent.finish();
                                    callback.onLoginFail(e.getMessage());
                                }
                                public void onFileNotFoundException(
                                    FileNotFoundException e)
                                {
                                    parent.finish();
                                    callback.onLoginFail(e.getMessage());
                                }
                                public void onMalformedURLException(
                                    MalformedURLException e)
                                {
                                    parent.finish();
                                    callback.onLoginFail(e.getMessage());
                                }
                                public void onFacebookError(
                                    FacebookError e)
                                {
                                    parent.finish();
                                    callback.onLoginFail(e.getMessage());
                                }
                            });
                    }
                }
                public void onFacebookError(FacebookError error) {
                    callback.onLoginFail(error.getMessage());
                    parent.finish();
                }
                public void onError(DialogError error) {
                    callback.onLoginFail(error.getMessage());
                    parent.finish();
                }
                public void onCancel() {
                    callback.onLoginFail(
                        context.getResources().getString(
                            R.string.error_login_cancel));
                    parent.finish();
                }
            });
    }

    public final String getUserID()
    {
        return userID;
    }

    public String request(Bundle parameters)
    throws MalformedURLException, IOException
    {
        return facebook.request(parameters);
    }

    public String request(String graphPath)
    throws MalformedURLException, IOException
    {
        return facebook.request(graphPath);
    }

    public String request(String graphPath, Bundle parameters)
    throws MalformedURLException, IOException
    {
        return facebook.request(graphPath, parameters);
    }

    public String request(Bundle parameters, String httpMethod)
    throws MalformedURLException, IOException
    {
        return facebook.request(null, parameters, httpMethod);
    }

    public String request(String graphPath, String httpMethod)
    throws MalformedURLException, IOException
    {
        Bundle b = new Bundle();
        return facebook.request(graphPath, b, httpMethod);
    }

    public String request(String graphPath, Bundle parameters, String httpMethod)
    throws FileNotFoundException, MalformedURLException, IOException
    {
        return facebook.request(graphPath, parameters, httpMethod);
    }

    public String fqlQuery(String query)
    throws MalformedURLException, IOException
    {
        Bundle b = new Bundle();
        b.putString("method", "fql.query");
        b.putString("query", query);
        String ret = request(b);
        return ret;
    }

    public String fqlMultiQuery(String... queries)
    throws MalformedURLException, IOException
    {
        Bundle b = new Bundle();
        b.putString("method", "fql.multiquery");
        JSONObject ql = new JSONObject();
        for (int i = 0 ; i < queries.length ; i++) {
            try {
                ql.put(String.format("query%d", i + 1), queries[i]);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }
        b.putString("queries", ql.toString());
        String ret = request(b);
        return ret;
    }

    public static interface OnLoginCallback
    {
        public void onLoginSuccess();
        public void onLoginFail(String reason);
    }
}
