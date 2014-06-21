package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import static com.appglue.Constants.TAG;

/**
 * Created by andyridge on 04/06/2014.
 */
public class NewPictureTrigger extends GenericTrigger {

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getData();
        Log.d(TAG, uri.toString());

    }
}
