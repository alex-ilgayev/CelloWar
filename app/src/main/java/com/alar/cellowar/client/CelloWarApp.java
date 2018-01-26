package com.alar.cellowar.client;

import android.app.Activity;
import android.app.Application;

/**
 * Created by alexi on 1/26/2018.
 */

public class CelloWarApp extends Application {
    public void onCreate() {
        super.onCreate();
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}
