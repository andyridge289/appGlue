package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ShutdownTrigger extends GenericTrigger {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = new Bundle();


        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }
}
