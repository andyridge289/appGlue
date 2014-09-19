package com.appglue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

public class ActivityWiring extends ActionBarActivity {
	private CompositeService cs;

    private FragmentWiringPager wiringFragment;
    private FragmentComponentListPager componentListFragment;

    private CharSequence mTitle;

    private Menu menu;

    private boolean makingNew = false;

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

        Fragment attach = null;

        switch(mode) {
            case MODE_CREATE:
                makingNew = false;
                mTitle = "Create glued app";
                wiringFragment = (FragmentWiringPager) FragmentWiringPager.create(cs.getID());
                attach = wiringFragment;
                break;

            case MODE_CHOOSE:
                mTitle = "Choose a component";
                componentListFragment = (FragmentComponentListPager) FragmentComponentListPager.create(false);
                attach = componentListFragment;
                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, attach).commit();

        setActionBar();
        invalidateOptionsMenu();
    }

    public void setActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(mTitle);
    }

    public void onBackPressed() {
        if (mode == MODE_CHOOSE && !makingNew) {
            setMode(MODE_CREATE);
            redraw();
            return;
        }

        super.onBackPressed();
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

        AlertDialog.Builder keepTemp = null;

        if(cs == null && compositeId == -1) {
            // They are DEFINITELY creating a new one
            if(registry.tempExists())
            {
                // There is stuff in the temp -- they might want to save it
                keepTemp = new AlertDialog.Builder(this);
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
                                cs = registry.resetTemp();
                                setMode(MODE_CHOOSE);
                                makingNew = true;

                            }
                        });
            }
            else
            {
                // There isn't stuff in the temp, just use that
                cs = registry.resetTemp();
                setMode(MODE_CHOOSE);
                makingNew = true;
            }
        } else { // They might not be creating a new one
            if(cs == null) {
                // If they've come in from the composite list then CS might not have been set yet, so set it.
                cs = registry.getComposite(compositeId);
            }

            if(registry.tempExists()) {
                keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to keep it?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Save draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.saveTempAsComposite("Saved draft");
                                setMode(MODE_CREATE);
                                makingNew = true;
                            }
                        });
                keepTemp.setNegativeButton("Discard draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.resetTemp();
                                cs = registry.getComposite(cs.getID());
                                setMode(MODE_CREATE);
                                makingNew = true;
                            }
                        });
            } else {
                cs = registry.getComposite(cs.getID());
                setMode(MODE_CREATE);
            }
        }

        if(keepTemp != null)
            keepTemp.create().show();
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
        this.menu = menu;

		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.wiring, menu);

        if(mode == MODE_CREATE) {

            if(wiringFragment != null && wiringFragment.getWiringMode() == FragmentWiringPager.MODE_WIRING) {
                menu.setGroupVisible(R.id.menu_group_create_wiring, true);
                menu.setGroupVisible(R.id.menu_group_create_value, false);
            } else {
                menu.setGroupVisible(R.id.menu_group_create_wiring, false);
                menu.setGroupVisible(R.id.menu_group_create_value, true);
            }
        } else {
            menu.setGroupVisible(R.id.menu_group_create_wiring, false);
            menu.setGroupVisible(R.id.menu_group_create_value, false);
            menu.setGroupVisible(R.id.menu_group_choose, true);
        }

        return true;
    }

	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			finish();
		} else if(item.getItemId() == R.id.wiring_done) {
			wiringFragment.saveDialog();
		} else if (item.getItemId() == R.id.wiring_value_switch) {
            wiringFragment.setWiringMode(FragmentWiringPager.MODE_VALUE);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.wiring_wiring_switch) {
            wiringFragment.setWiringMode(FragmentWiringPager.MODE_WIRING);
            invalidateOptionsMenu();
        }

        return true;
	}

    public void chooseComponentFromList(int position) {
        this.position = position;
        setMode(MODE_CHOOSE);
    }

    public void chooseItem(String className) {
        ServiceDescription sd = registry.getServiceDescription(className);
        ComponentService component = new ComponentService(cs, sd, position);
        long id = registry.addComponent(component);

        if(id != -1) {
            component.setID(id);
            cs.addComponent(component, position);
        } else {
            Toast.makeText(this, "Failed to add component \"" + className + "\" for some reason.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to add component");
        }

        setMode(MODE_CREATE);
    }

    public CompositeService getComposite() {
        return cs;
    }
}