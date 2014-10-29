package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StartupTrigger extends GenericTrigger {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = new Bundle();

        if (!intent.getAction().intern().equals(Intent.ACTION_BOOT_COMPLETED)) {
            super.fail(context, "Not really a startup trigger");
            return;
        }

        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }
}
