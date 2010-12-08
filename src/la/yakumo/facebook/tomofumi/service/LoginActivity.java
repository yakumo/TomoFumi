package la.yakumo.facebook.tomofumi.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;

public class LoginActivity extends Activity
{
    private static final String TAG = Constants.LOG_TAG;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Facebook.getInstance(this).login(this, new Facebook.OnLoginCallback() {
            public void onLoginSuccess()
            {
                Intent intent = new Intent(ClientService.ACTION_LOGIN_SUCCESS);
                LoginActivity.this.sendBroadcast(intent);
            }

            public void onLoginFail(String reason)
            {
                Intent intent = new Intent(ClientService.ACTION_LOGIN_FAIL);
                intent.putExtra(ClientService.EXTRA_LOGIN_REASON, reason);
                LoginActivity.this.sendBroadcast(intent);
            }
        });
    }
}
