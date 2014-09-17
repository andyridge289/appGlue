package com.appglue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

import static com.appglue.Constants.TAG;

import java.util.ArrayList;

public class ActivityWiring extends ActionBarActivity {
	private CompositeService cs;

    private FragmentWiringPager wiringFragment;
    private FragmentComponentListPager componentListFragment;

    private CharSequence mTitle;

    private int position = 0;

    public ActivityWiring() {
    }

    private TextView status;

	private Registry registry;

    private int mode;
    public static final int MODE_CREATE = 0;
    public static final int MODE_CHOOSE = 1;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		setContentView(R.layout.activity_wiring);
    }

    public void setMode(int mode) {
        this.mode = mode;

        if(wiringFragment == null) {
            wiringFragment = (FragmentWiringPager) FragmentWiringPager.create(cs.getID());
            componentListFragment = (FragmentComponentListPager) FragmentComponentListPager.create(false);
        }

        Fragment attach = null;

        switch(mode) {
            case MODE_CREATE:
                mTitle = "Create glued app";
                attach = wiringFragment;
                break;

            case MODE_CHOOSE:
                mTitle = "Choose a component";
                attach = componentListFragment;
                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, attach).commit();

        setActionBar();
    }

    private void setActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(mTitle);
    }

    public void onBackPressed() {
        // TODO If we're on the list page go back to the wire-er
        // TODO If we're on the wiring page go back to the home page
    }

	public void onPause()
	{
		super.onPause();
	}

	public void onResume()
	{
		super.onResume();

        Intent intent = this.getIntent();
        long compositeId = intent.getLongExtra(COMPOSITE_ID, -1);

        registry = Registry.getInstance(this);

        if(cs == null && compositeId == -1) {
            // They are DEFINITELY creating a new one
            if(registry.tempExists())
            {
                // There is stuff in the temp -- they might want to save it
                AlertDialog.Builder keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to carry on with it, or start again?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Keep draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cs = registry.getTemp();
                                setMode(MODE_CREATE);
                            }
                        });

                keepTemp.setNegativeButton("Start new",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cs = registry.createTemp();
                                setMode(MODE_CHOOSE);
                            }
                        });

                keepTemp.create().show();
            }
            else
            {
                // There isn't stuff in the temp, just use that
                cs = registry.createTemp();
                setMode(MODE_CHOOSE);
            }
        } else { // They might not be creating a new one
            if(cs == null) {
                // If they've come in from the composite list then CS might not have been set yet, so set it.
                cs = registry.getComposite(compositeId);
            }

            if(registry.tempExists()) {
                AlertDialog.Builder keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to keep it?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Save draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.saveTempAsComposite("Saved draft");
                                setMode(MODE_CREATE);
                            }
                        });
                keepTemp.setNegativeButton("Discard draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.createTemp();
                                cs = registry.getComposite(cs.getID());
                                setMode(MODE_CREATE);
                            }
                        });

                keepTemp.create().show();
            } else {
                cs = registry.getComposite(cs.getID());
                setMode(MODE_CREATE);
            }
        }
	}

	public ArrayList<ComponentService> getComponents() {
		return cs.getComponentsAL();
	}

	public int getMode() {
		return mode;
	}

	public void setStatus(String statusString) {
		status.setText(statusString);
	}

    public void redraw() {
        if(wiringFragment != null)
            wiringFragment.redraw();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
//		MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.wiring, menu);
//
//        modeMenuItem = menu.findItem(R.id.wiring_value_switch);
//        setMode(mode);
        return true;
    }

	public boolean onOptionsItemSelected(MenuItem item)
	{
//		if(item.getItemId() == android.R.id.home)
//		{
//			finish();
//		}
//		else if(item.getItemId() == R.id.wiring_done)
//		{
//			saveDialog();
//		} else if (item.getItemId() == R.id.wiring_value_switch) {
//            if (mode == MODE_WIRING)
//                setMode(MODE_VALUE);
//            else
//                setMode(MODE_WIRING);
//        }
        return true;
	}

    // FIXME Get composites get app might not be working -- that would explain why it doesn't load the icon first time round


    // FIXME 19 components. 19!!!!!!11111111!!!!!
    public void chooseItem(String className) {
        ServiceDescription sd = registry.getServiceDescription(className);
        ComponentService component = new ComponentService(cs, sd, position);
        long id = registry.addComponent(component);

        if(id != -1) {
            component.setID(id);
            cs.addComponent(component, position);
        } else {
            // TODO Report an error to the user
            Log.e(TAG, "Failed to add component");
        }

        setMode(MODE_CREATE);
    }

}