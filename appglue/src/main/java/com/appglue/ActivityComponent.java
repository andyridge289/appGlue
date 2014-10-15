package com.appglue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.appglue.Constants.ServiceType;
import com.appglue.description.ServiceDescription;
import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.RESULT;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;
import static com.appglue.library.AppGlueConstants.MARKET_LOOKUP;

public class ActivityComponent extends ActionBarActivity {
    private ServiceDescription service;

    private boolean atomicList = false;

    private int type;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_component);
        Intent intent = this.getIntent();

        if (!intent.hasExtra(CLASSNAME))
            finish();

        String className = intent.getStringExtra(CLASSNAME);
        atomicList = intent.getBooleanExtra(JUST_A_LIST, false);
        type = intent.getIntExtra(SERVICE_TYPE, -1);

        if (className == null || className.equals("")) {
            className = icicle.getString(CLASSNAME);
            atomicList = icicle.getBoolean(JUST_A_LIST);
            type = icicle.getInt(SERVICE_TYPE);
        }

        if (className == null || className.equals(""))
            finish();

        Registry registry = Registry.getInstance(this);
        if (type == ServiceType.LOCAL.index || type == ServiceType.DEVICE.index)
            service = registry.getServiceDescription(className);
        else if (type == ServiceType.REMOTE.index)
            service = registry.getRemote(className);
        else
            service = null;

        if (service == null)
            finish();


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setIcon(R.drawable.ic_menu_back);
            actionBar.setHomeButtonEnabled(true);

            if (this.atomicList)
                actionBar.setTitle(R.string.component_title_view);
            else
                actionBar.setTitle(R.string.component_title_use);
        }
    }

    /**
     * @param icicle The Bundle into which to save all the stuff
     */
    @Override
    public void onSaveInstanceState(Bundle icicle) {
        icicle.putString(CLASSNAME, service.getClassName());
        icicle.putBoolean(JUST_A_LIST, atomicList);
        icicle.putInt(SERVICE_TYPE, this.type);
    }

    @Override
    public void onRestoreInstanceState(Bundle icicle) {
        super.onRestoreInstanceState(icicle);

        if (!icicle.containsKey(CLASSNAME))
            return;

        restoreState(icicle);
    }

    private void restoreState(Bundle icicle) {
        String className = icicle.getString(CLASSNAME);
        atomicList = icicle.getBoolean(JUST_A_LIST, false);
        type = icicle.getInt(SERVICE_TYPE, -1);

        Registry registry = Registry.getInstance(this);
        if (type == ServiceType.LOCAL.index || type == ServiceType.DEVICE.index)
            service = registry.getServiceDescription(className);
        else if (type == ServiceType.REMOTE.index)
            service = registry.getRemote(className);
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.story_components, menu); // TODO Not sure we need this, might need to put the component fragment in the right place
//
//        MenuItem useItem = menu.findItem(R.id.simple_use_button);
//        MenuItem getItem = menu.findItem(R.id.simple_get_button);
//
//        if (useItem == null || getItem == null)
//            return false;
//
//        if (atomicList)
//            useItem.setVisible(false);
//
//        if (this.type == ServiceType.REMOTE.index) {
//            useItem.setVisible(false);
//        } else {
//            getItem.setVisible(false);
//        }

        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MARKET_LOOKUP) {
            Intent i = new Intent();
            i.putExtra(RESULT, MARKET_LOOKUP);

            if (getParent() == null) {
                setResult(Activity.RESULT_OK, i);
            } else {
                getParent().setResult(Activity.RESULT_OK, i);
            }

            finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

//        if (item.getItemId() == R.id.simple_get_button) {
//            String marketUri = "market://details?id=" + service.getPackageName();
//            if (LOG) Log.d(TAG, "Market URI " + marketUri);
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketUri));
//            startActivityForResult(intent, MARKET_LOOKUP);
//            return true;
//        } else {
//            if (!atomicList) {
//                int result = NOT_SET;
//                if (item.getItemId() == R.id.simple_use_button) {
//                    result = SUCCESS;
//                } else if (item.getItemId() != android.R.id.home) {
//                    result = NOT_SET;
//                }
//
//                Intent i = new Intent();
//                i.putExtra(RESULT, result);
//                i.putExtra(CLASSNAME, service.getClassName());
//                i.putExtra(SERVICE_TYPE, type);
//
//                if (getParent() == null) {
//                    setResult(Activity.RESULT_OK, i);
//                } else {
//                    getParent().setResult(Activity.RESULT_OK, i);
//                }
//            }
//            finish();
//        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (!atomicList) {
            Intent i = new Intent();
            i.putExtra("result", false);

            if (getParent() == null) {
                setResult(Activity.RESULT_OK, i);
            } else {
                getParent().setResult(Activity.RESULT_OK, i);
            }
        }
        finish();
    }
}
