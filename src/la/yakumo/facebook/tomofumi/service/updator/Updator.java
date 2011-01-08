package la.yakumo.facebook.tomofumi.service.updator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import la.yakumo.facebook.tomofumi.Constants;
import la.yakumo.facebook.tomofumi.service.ClientService;
import la.yakumo.facebook.tomofumi.service.Facebook;

public class Updator
{
    protected static final String TAG = Constants.LOG_TAG;

    protected Context context;
    protected Resources resources;
    protected Facebook facebook;
    private Handler handler = null;

    public Updator(Context context)
    {
        this.context = context;
        this.resources = context.getResources();
        this.facebook = Facebook.getInstance(context);
    }

    public Bundle execute(OnStatusChange status)
    {
        return
            execute(
                new OnUpdateCommand()
                {
                    public void onUpdateCommand(Bundle info)
                    {
                        updateCommand(info);
                    }
                },
                status);
    }

    protected void updateCommand(Bundle info)
    {
        Log.i(TAG, "Updator#updateCommand");
    }

    protected Bundle execute(OnUpdateCommand command, OnStatusChange status)
    {
        status.onStart();
        Bundle ret = execute(command);
        status.onFinish(ret);
        return ret;
    }

    protected Bundle execute(OnUpdateCommand... commands)
    {
        while (true) {
            Log.i(TAG, "Update#execute");
            if (!Facebook.getInstance(context).loginCheck()) {
                Looper.prepare();
                final String errMsg = null;
                final Handler handler = new Handler() {
                    public void handleMessage(Message msg)
                    {
                        if (msg.what == 1) {
                            Looper.myLooper().quit();
                        }
                    }
                };
                BroadcastReceiver loginStatusReceiver =
                    new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent)
                        {
                            String action = intent.getAction();
                            int sessionID = intent.getIntExtra(
                                ClientService.EXTRA_LOGIN_SESSION_ID,
                                Constants.SESSION_UNKNOWN);
                            if (ClientService.ACTION_LOGIN_SUCCESS.equals(action)) {
                            }
                            if (ClientService.ACTION_LOGIN_FAIL.equals(action)) {
                                errMsg.replace(
                                    errMsg,
                                    intent.getStringExtra(
                                        ClientService.EXTRA_LOGIN_REASON));
                            }
                            handler.sendEmptyMessage(1);
                        }
                    };
                IntentFilter f = new IntentFilter();
                f.addAction(ClientService.ACTION_LOGIN_SUCCESS);
                f.addAction(ClientService.ACTION_LOGIN_FAIL);
                context.registerReceiver(loginStatusReceiver, f);
                Log.i(TAG, "wait login ...");
                Looper.loop();
                Log.i(TAG, "logged in !!!");
                context.unregisterReceiver(loginStatusReceiver);
                break;
            }
            else {
                break;
            }
        }

        Bundle info = new Bundle();
        for (OnUpdateCommand command : commands) {
            command.onUpdateCommand(info);
        }

        return info;
    }

    public interface OnUpdateCommand
    {
        public void onUpdateCommand(Bundle info);
    }

    public interface OnStatusChange
    {
        public void onStart();
        public void onFinish(Bundle info);
    }
}
