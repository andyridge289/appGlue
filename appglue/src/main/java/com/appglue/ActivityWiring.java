package com.appglue;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.layout.DepthPageTransformer;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.FIRST;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;

public class ActivityWiring extends FragmentActivity
{
	private CompositeService cs;

    private ViewPager wiringPager;
    private ViewPager valuePager;

    private WiringPagerAdapter wiringPagerAdapter;
    private WiringPagerAdapter valuePagerAdapter;

	private TextView status;

	private Registry registry;

    private static final int MODE_WIRING = 0;
    public static final int MODE_VALUE = 1;

    private final int COMPOSITE_LIST = 0;
    private final int COMPONENT_LIST = 1;
    private int source = COMPOSITE_LIST;

    private int mode = MODE_VALUE;

    private TextView csNameText;
	private EditText csNameEdit;
	private Button csNameSet;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.activity_wiring);

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(R.string.comp_title);

		registry = Registry.getInstance(this);

        wiringPager = (ViewPager) findViewById(R.id.wiring_pager);
        valuePager = (ViewPager) findViewById(R.id.value_pager);
        valuePager.setPageTransformer(true, new DepthPageTransformer());
    }

    private void finishWiringSetup()
    {
		status = (TextView) findViewById(R.id.status);
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // FIXME Click on the status message to view more status messages
            }
        });

		csNameText = (TextView) findViewById(R.id.cs_name);
		csNameEdit = (EditText) findViewById(R.id.cs_name_edit);
		csNameSet = (Button) findViewById(R.id.cs_name_edit_button);

		if(cs.getName().equals(""))
		{
			csNameText.setText("Temp name");
			csNameEdit.setText("Temp name");
		}
		else
		{
			csNameText.setText(cs.getName());
			csNameEdit.setText(cs.getName());
		}

		csNameText.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				csNameText.setVisibility(View.GONE);
				csNameEdit.setVisibility(View.VISIBLE);
				csNameSet.setVisibility(View.VISIBLE);
			}
		});

		csNameSet.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String name = csNameEdit.getText().toString();
				cs.setName(name);
				registry.updateCurrent();
				csNameText.setText(name);

				csNameText.setVisibility(View.VISIBLE);
				csNameEdit.setVisibility(View.GONE);
				csNameSet.setVisibility(View.INVISIBLE);
			}
		});

        final Button modeButton = (Button) findViewById(R.id.change_mode);
        modeButton.setText(mode == MODE_WIRING ? "Setting Mode" : "Wiring Mode");
		modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == MODE_WIRING) {
                    modeButton.setText("Setting mode");
                    setMode(MODE_VALUE);
                } else {
                    modeButton.setText("Wiring mode");
                    setMode(MODE_WIRING);
                }
                redraw();
            }
        });

		Intent intent = this.getIntent();
		int index = intent.getIntExtra(INDEX, -1);

        wiringPagerAdapter = new WiringPagerAdapter(getFragmentManager(), true);
        valuePagerAdapter = new WiringPagerAdapter(getFragmentManager(), false);

        wiringPagerAdapter.notifyDataSetChanged();
        valuePagerAdapter.notifyDataSetChanged();

        wiringPager.setAdapter(wiringPagerAdapter);
        valuePager.setAdapter(valuePagerAdapter);

        setMode(mode);

        if (index != -1) {
            wiringPager.setCurrentItem(index);
            valuePager.setCurrentItem(index);
        }

		if(source == COMPOSITE_LIST)
		{
			Log.w(TAG, "In resume, going to get another component");
			// Then we need to add a component.
			Intent anotherIntent = new Intent(ActivityWiring.this, ActivityComponentList.class);
			anotherIntent.putExtra(POSITION, 0);
			anotherIntent.putExtra(FIRST, true);
			anotherIntent.putExtra(CREATE_NEW, true);
			startActivityForResult(anotherIntent, SERVICE_REQUEST);
		}
		else
		{
            wiringPager.setCurrentItem(index);
        }
	}

    private void setMode(int mode) {
        this.mode = mode;

        if (mode == MODE_WIRING) {
            wiringPager.setVisibility(View.VISIBLE);
            valuePager.setVisibility(View.GONE);
            wiringPager.setCurrentItem(valuePager.getCurrentItem());
        } else {
            wiringPager.setVisibility(View.GONE);
            valuePager.setVisibility(View.VISIBLE);
            valuePager.setCurrentItem(valuePager.getCurrentItem());
        }

        redraw();
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

        if(source == this.COMPONENT_LIST)
        {
            // Then we don't need to do any of the checks
            finishWiringSetup();
        }
        else if(cs == null && compositeId == -1)
        {
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
                                source = COMPONENT_LIST;
                                finishWiringSetup();
                            }
                        });
                keepTemp.setNegativeButton("Start new",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cs = registry.createTemp();
                                finishWiringSetup();
                            }
                        });

                keepTemp.create().show();
            }
            else
            {
                // There isn't stuff in the temp, just use that
                cs = registry.createTemp();
                finishWiringSetup();
            }
        }
        else // They might not be creating a new one
        {
            if(cs == null)
            {
                // If they've come in from the composite list then CS might not have been set yet, so set it.
                cs = registry.getComposite(compositeId);
                source = COMPONENT_LIST;
            }

            if(registry.tempExists())
            {
                AlertDialog.Builder keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to keep it?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Save draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.saveTemp("Saved draft");
                                finishWiringSetup();
                            }
                        });
                keepTemp.setNegativeButton("Discard draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.createTemp();
                                cs = registry.getComposite(cs.getId());
                                finishWiringSetup();
                            }
                        });

                keepTemp.create().show();
            }
            else
            {
                cs = registry.getComposite(cs.getId());
                finishWiringSetup();
            }
        }
	}

	private void redraw()
	{
		if(mode == MODE_WIRING)
		{
            getActionBar().setTitle("Wiring");
        }
		else
		{
            getActionBar().setSubtitle("Set Inputs and Filters");
        }

        WiringPagerAdapter adapter = mode == MODE_WIRING ? wiringPagerAdapter : valuePagerAdapter;

		// Tell all the fragments to redraw...
        for (int i = 0; i < adapter.getCount(); i++) {
            FragmentVW f = (FragmentVW) adapter.getItem(i);
            f.redraw();
		}
	}

	public ArrayList<ServiceDescription> getComponents()
	{
		return cs.getComponents();
	}

	public int getMode()
	{
		return mode;
	}

	public void setStatus(String statusString)
	{
		status.setText(statusString);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.wiring, menu);
        return true;
    }

	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == android.R.id.home)
		{
			finish();
		}
		else if(item.getItemId() == R.id.wiring_done)
		{
			saveDialog();
		} else if (item.getItemId() == R.id.wiring_value_switch) {
            if (mode == MODE_WIRING) {
                mode = MODE_VALUE;
                item.setTitle("Wiring");
            } else {
                mode = MODE_WIRING;
                item.setTitle("Values");
            }
        }
		else if(item.getItemId() == R.id.wiring_next)
		{
            if (mode == MODE_WIRING) {
                if (wiringPager != null)
                    wiringPager.setCurrentItem(wiringPager.getCurrentItem() + 1);
            } else {
                if (valuePager != null)
                    valuePager.setCurrentItem(valuePager.getCurrentItem() + 1);
            }
        }

        // FIXME Need to check whether the above are in bounds?
        return true;
	}


    private void saveDialog()
    {
        if(cs.getId() == 1)
        {
            // Then it's the temp, we should save it
            String name = csNameEdit.getText().toString();

            if(name.equals("Temp name"))
            {
                String tempName = "";
                for(ServiceDescription sd : cs.getComponents())
                    tempName += sd.getName() + "  ";

                name = tempName;
            }

            registry.saveTemp(name);
        }
        else if(cs.getId() == -1)
        {
            // It's not the temp, but we're still saving a new one (I'm not really sure how this has happened)
            if(LOG) Log.d(TAG, "the CS is -1, this might be bad.");
        }
        else
        {
            // We're just updating one that already exists
            boolean success = registry.updateWiring(cs);
            if(success)
                Log.d(TAG, "Updated " + cs.getName());
        }

        Intent intent = new Intent();
        if (getParent() == null)
        {
            setResult(Activity.RESULT_OK, intent);
        }
        else
        {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

	/**
	 * Whatever happens, update the current one
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode == SERVICE_REQUEST) {

            source = COMPONENT_LIST;

            if(resultCode == Activity.RESULT_OK) {
                String className = intent.getStringExtra(CLASSNAME);
                int position = intent.getIntExtra(INDEX, -1);
                Log.w(TAG, "Got " + className + " at " + position);

                ServiceDescription component = registry.getAtomic(className);
                final boolean first = intent.getBooleanExtra(FIRST, false);

                if (first) {
                    cs.addComponent(position, component);
                } else {
                    cs.addComponent(component);
                }

                registry.updateCurrent();
            }
        }
	}

    private class WiringPagerAdapter extends FragmentStatePagerAdapter {
        private Fragment[] fragments;
        private boolean wiring;

        public WiringPagerAdapter(FragmentManager fragmentManager, boolean wiring) {
            super(fragmentManager);
            this.wiring = wiring;

            fragments = wiring ?
                    new Fragment[cs.getComponents().size() + 1] :
                    new Fragment[cs.getComponents().size()];
        }

		@Override
        public Fragment getItem(int position)
		{
            if (fragments.length <= position) {
                fragments = new Fragment[cs.getComponents().size() + 1];
			}

			if(fragments[position] == null)
                fragments[position] = FragmentVW.create(position, wiring);

            return fragments[position];
        }

        @Override
        public int getCount() {
            ArrayList<ServiceDescription> components = cs.getComponents();

            if (wiring)
                return components == null ? 0 : components.size() + 1;
            else
                return components == null ? 0 : components.size();
        }
    }
}