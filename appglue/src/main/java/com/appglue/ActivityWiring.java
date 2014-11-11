package com.appglue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.FilterValueView;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.library.AppGlueLibrary;
import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;

public class ActivityWiring extends ActionBarActivity {

    private FragmentWiringPager wiringFragment;
    private FragmentComponentListPager componentListFragment;
    private FragmentFilter filterFragment;

    public static final int CONTACT_PICKER_VALUE = 1001;
    public static final int CONTACT_PICKER_FILTER = 1002;

    private long componentId = -1;
    private long filterId = -1;

    private boolean createNew = false;
    private boolean editExisting = false;

    private int pagerPosition = 0;
    private int componentPosition = 0;
    private Object callbackView;

    private Toolbar toolbar;

    public ActivityWiring() {
    }

    private Registry registry;

    private int mode;
    public static final int MODE_CREATE = 0;
    public static final int MODE_CHOOSE = 1;
    public static final int MODE_FILTER = 2;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_wiring);

        toolbar = (Toolbar) findViewById(R.id.toolbar_wiring);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        editExisting = intent.getBooleanExtra(EDIT_EXISTING, false);
        createNew = intent.getBooleanExtra(CREATE_NEW, false);

        intent.putExtra(EDIT_EXISTING, false);
        intent.putExtra(CREATE_NEW, false);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void redraw() {
        Fragment attach = null;

        CompositeService composite = registry.getCurrent(true);

        switch (mode) {
            case MODE_CREATE:
                if (wiringFragment == null)
                    wiringFragment = (FragmentWiringPager) FragmentWiringPager.create(composite.getID(), pagerPosition);
                attach = wiringFragment;
                break;

            case MODE_CHOOSE:
                componentListFragment = (FragmentComponentListPager) FragmentComponentListPager.create(false);
                attach = componentListFragment;
                break;

            case MODE_FILTER:
                filterFragment = (FragmentFilter) FragmentFilter.create(componentId, filterId);
                attach = filterFragment;
                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, attach).commit();
        // XXX Unable to resume, can't do this after on save instance state

        invalidateOptionsMenu();

        if (wiringFragment != null) {
            wiringFragment.redraw(pagerPosition, true);
        }
    }

    public void onBackPressed() {

        if (mode == MODE_CHOOSE) {

            if (componentListFragment.onBackPressed()) {
                redraw();
                return;
            } else if (registry.getCurrent(false).size() > 0) {
                setMode(MODE_CREATE);
                redraw();
                return;
            }
            // If there's nothing in the thing then we should leave

        } else if (mode == MODE_FILTER) {
            // There's nothing special that should happen in the filter fragment I don't think
            if (filterFragment.onBackPressed()) {
                redraw();
                return;
            }

            setMode(MODE_CREATE);
            redraw();
            return;
        }

        super.onBackPressed();
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();

        Intent intent = this.getIntent();
        final long compositeId = intent.getLongExtra(COMPOSITE_ID, -1);
        registry = Registry.getInstance(this);
        AlertDialog.Builder keepTemp = null;

        if (createNew) {
            // They are DEFINITELY creating a new one
            if (registry.tempExists()) {
                // There is stuff in the temp -- they might want to save it
                keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to carry on with it, or start again?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Keep draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.setCurrent(registry.getTemp());
                                setMode(MODE_CREATE);
                                redraw();
                            }
                        });

                keepTemp.setNegativeButton("Start new",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.setCurrent(registry.resetTemp());
                                setMode(MODE_CHOOSE);
                                redraw();
                            }
                        });
            } else {
                // There isn't stuff in the temp, just use that
                registry.setCurrent(registry.resetTemp());
                setMode(MODE_CHOOSE);
                redraw();
            }
        } else if (editExisting) { // They might not be creating a new one
            if (registry.getCurrent(false) == null) {
                // If they've come in from the composite list then CS might not have been set yet, so set it.
                registry.setCurrent(registry.getComposite(compositeId));
            }

            if (registry.tempExists()) {
                keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to keep it?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Save draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                saveDialog("Saved draft", true);
                                registry.resetTemp();
                                registry.setCurrent(compositeId);
                                setMode(MODE_CREATE);
                                redraw();
                            }
                        });
                keepTemp.setNegativeButton("Discard draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.resetTemp();
                                registry.setCurrent(compositeId);
                                setMode(MODE_CREATE);
                                redraw();
                            }
                        });
            } else {
                registry.setCurrent(compositeId);
                setMode(MODE_CREATE);
                redraw();
            }
        } else {
            Log.d(TAG, "They aren't creating a new one or editing, we shouldn't need the dialog");
        }

        createNew = false;
        editExisting = false;

        if (keepTemp != null) {
            keepTemp.create().show();
        }
    }

    public void saveDialog(String name, boolean autoSave) {
        boolean enabled = true;
        CompositeService cs = registry.getCurrent(false);
        for (ServiceIO io : cs.getMandatoryInputs()) {
            if (!io.hasValueOrConnection()) {
                enabled = false;
                Toast.makeText(this, "You've missed some of the mandatory values, so your composite has been disabled for now", Toast.LENGTH_LONG).show();
            }
        }

        if (autoSave) {
            registry.saveTempAsComposite(name, enabled);
            return;
        } else if (cs.getID() == 1) {


            registry.saveTempAsComposite(name, enabled);
        } else if (cs.getID() == -1) {
            // It's not the temp, but we're still saving a new one (I'm not really sure how this has happened)
            if (LOG) Log.d(TAG, "the CS is -1, this might be bad.");
        } else {
            // We're just updating one that already exists
            registry.updateComposite(cs);
        }

        finish();
    }

    public void startActivityForResult(Object v, Intent intent, int code) {
        this.callbackView = v;
        startActivityForResult(intent, code);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (callbackView == null) {
                Log.e(TAG, "Tried to do lookup callback on dead reference");
                return;
            }

            switch (requestCode) {
                case CONTACT_PICKER_VALUE:
                    if (callbackView instanceof DialogIO) {
                        ((DialogIO) callbackView).setContact(AppGlueLibrary.getContact(this, data));
                    }
                    break;

                case CONTACT_PICKER_FILTER:
                    if (callbackView instanceof FilterValueView) {
                        ((FilterValueView) callbackView).setContact(AppGlueLibrary.getContact(this, data));
                    }
                    break;
            }

        } else {
            // gracefully handle failure
            Log.w(TAG, "Warning: activity result not ok");
        }
    }

    public SparseArray<ComponentService> getComponents() {
        CompositeService composite = registry.getCurrent(false);
        if (registry != null)
            return composite.getComponents();
        else
            return new SparseArray<ComponentService>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wiring, menu);

        int background = R.color.composite;
        String title = "";

        switch (mode) {
            case MODE_CREATE:
                if (wiringFragment == null) {
                    return true;
                }

                background = R.color.composite;
                title = wiringFragment.onCreateOptionsMenu(menu);
                break;

            case MODE_FILTER:
                if (wiringFragment == null) {
                    return true;
                }
                background = R.color.filter;
                title = filterFragment.onCreateOptionsMenu(menu);
                break;

            case MODE_CHOOSE:
                background = R.color.component;
                title = componentListFragment.onCreateOptionsMenu(menu);
                break;
        }

        if (toolbar != null) {
            toolbar.setBackgroundResource(background);
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
            toolbar.setTitle(title);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.wiring_done) {
            wiringFragment.saveDialog();
        } else if (item.getItemId() == R.id.filter_done) {
            setMode(MODE_CREATE);
            registry.updateComposite(registry.getCurrent(false));
            redraw();
        } else if (item.getItemId() == R.id.wiring_auto) {
            // We must be on a wiring page or we wouldn't be able to see this menu item
            wiringFragment.getCurrentFragment().autoConnect();
        } else if (item.getItemId() == R.id.wiring_remove) {
            // Ditto to the above
            wiringFragment.getCurrentFragment().removeConnections();
        }

        return true;
    }

    public void setStatus(String status) {
        // TODO Need to do something with this at some point, or just remove it entirely?
    }

    public void chooseComponentFromList(boolean first, int currentPagerPosition) {

        if (first) {
            // To look at the same thing we were looking at we need to go along one
            this.pagerPosition = currentPagerPosition + 1;
            this.componentPosition = currentPagerPosition;
        } else {
            // TO look at the same thing we can just stay where we were
            this.pagerPosition = currentPagerPosition;
            this.componentPosition = currentPagerPosition;
        }

        setMode(MODE_CHOOSE);
        redraw();
    }

    public void chooseItem(String className) {
        ServiceDescription sd = registry.getServiceDescription(className);
        ComponentService component = new ComponentService(registry.getCurrent(false), sd, componentPosition);
        long id = registry.addComponent(component);

        if (id != -1) {
            component.setID(id);
            registry.getCurrent(false).addComponent(component, componentPosition);
        } else {
            Toast.makeText(this, "Failed to add component \"" + className + "\" for some reason.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to add component");
        }

        setMode(MODE_CREATE);
        redraw();
    }

    public void filter(long componentId, long filterId, int position) {
        this.componentId = componentId;
        this.filterId = filterId;
        this.pagerPosition = position;
        this.setMode(MODE_FILTER);
        this.redraw();
    }
}