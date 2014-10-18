package com.appglue.layout.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.ActivityWiring;
import com.appglue.IODescription;
import com.appglue.R;
import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.library.FilterFactory;
import com.appglue.serviceregistry.Registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class DialogIO extends AlertDialog {

    private ActivityWiring activity;
    private IODescription description;
    private ServiceIO item;
    private Registry registry;

    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;

    private DialogContactView dcv;

    public DialogIO(ActivityWiring context, final ServiceIO io) {
        super(context);

        this.activity = context;
        this.item = io;
        this.description = item.getDescription();
        this.registry = Registry.getInstance(context);

        LayoutInflater inflater = activity.getLayoutInflater();

        // We need to have a different base depending on what's going in the dialog, for resizing and showing of buttons
        IOType type = io.getType();
        int layout = R.layout.dialog_io_linear;
        if (type.typeEquals(IOType.Factory.getType(IOType.Factory.APP)) ||
            type.typeEquals(IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE))) {
            layout = R.layout.dialog_io_relative;
        }

        final View v = inflater.inflate(layout, null);

        LinearLayout container = (LinearLayout) v.findViewById(R.id.ioview_container);

        positiveButton = (Button) v.findViewById(R.id.dialog_io_positive);
        neutralButton = (Button) v.findViewById(R.id.dialog_io_neutral);
        negativeButton = (Button) v.findViewById(R.id.dialog_io_negative);

        if (type.typeEquals(IOType.Factory.getType(IOType.Factory.APP))) {
            // We need to show the app one
            DialogAppView dav = new DialogAppView(context);
            container.addView(dav);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE))) {
            DialogImageResourceView div = new DialogImageResourceView(context);
            container.addView(div);
        } else if (io.getType().typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
            dcv = new DialogContactView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dcv.setLayoutParams(lp);
            container.addView(dcv);
        } else {
            // Use the generic one
            DialogIOView div = new DialogIOView(context, io);
            container.addView(div);
        }

        setView(v);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });

        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put this back
                // Shouldn't this be clearing the value that is set?
                item.clearValue();
                registry.updateComposite(registry.getCurrent());
                cancel();
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    //  http://code.tutsplus.com/tutorials/android-essentials-using-the-contact-picker--mobile-2017
    public void setContact(Intent data) {

        if (dcv == null)
            return;

        Uri result = data.getData();
        Log.v(TAG, "Got a result: "
                + result.toString());
        String id = result.getLastPathSegment();

        String whereName = ContactsContract.Data.CONTACT_ID + " = ?";
        String[] whereNameParams = new String[] { "" + id };
        Cursor c = activity.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams,
                                                       ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
        c.moveToFirst();
        String name = c.getString(c.getColumnIndex("display_name"));

        dcv.sampleButton.setText(name);
        dcv.sampleRadio.setChecked(true);
    }

    private class DialogContactView extends LinearLayout {

        private RadioGroup radioGroup;
        private RadioButton sampleRadio;
        private RadioButton manualRadio;

        private Button sampleButton;
        private EditText manualText;

        public DialogContactView(Context context, ServiceIO io) {
            super(context);
            create(context);
        }
        public DialogContactView(Context context) {
            super(context);
            create(context);
        }
        public DialogContactView(Context context, AttributeSet attrs) {
            super(context, attrs);
            create(context);
        }
        public DialogContactView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            create(context);
        }

        private void create(Context context) {
            View v = View.inflate(context, R.layout.dialog_io_contact, null);
            this.addView(v);

            manualRadio = (RadioButton) v.findViewById(R.id.io_radio_text);
            sampleRadio = (RadioButton) v.findViewById(R.id.io_radio_spinner);
            manualText = (EditText) v.findViewById(R.id.io_value_text);
            sampleButton = (Button) v.findViewById(R.id.io_value_button);

            final IOType type = description.getType();
            manualText.setInputType(InputType.TYPE_CLASS_NUMBER);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the value they entered - not sure what happens
//                    if (manualRadio.isChecked()) {
//                        // Then look up the text value
//                        Object objectValue = description.getType().fromString(manualText.getText().toString());
//                        IOValue value = new IOValue(FilterFactory.NONE, objectValue, io);
//                        item.setValue(value);
//                    } else if (sampleRadio.isChecked()) {
//                        // Then look up the index of the spinner that's selected - shouldn't need to worry about data types
//                        SampleValue sampleValue = (SampleValue) sampleSpinner.getSelectedItem();
//                        IOValue value = new IOValue(FilterFactory.NONE, sampleValue, io);
//                        item.setValue(value);
//                    }

                    // The setting of the list values needs to move to the creating of the list. Do an invalidate
                    registry.updateComposite(activity.getComposite());
                    DialogIO.this.activity.redraw();
                    DialogIO.this.activity.setIODialog(null);
                    dismiss();
                }
            });

            sampleRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        sampleButton.setEnabled(true);
                        manualText.setEnabled(false);
                        manualRadio.setChecked(false);
                    }
                }
            });

            manualRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        sampleButton.setEnabled(false);
                        manualText.setEnabled(true);
                        sampleRadio.setChecked(false);
                    }
                }
            });

            sampleButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Look up the contact
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);
                    activity.startActivityForResult(contactPickerIntent, ActivityWiring.CONTACT_PICKER_RESULT);
                }
            });

//            // TODO Loading saved values
//            if (item.hasValue()) {
//
//                IOValue value = item.getValue();
//
//                if (value.getFilterState() == IOValue.MANUAL) {
//
//                    // Load the value from the item and Set the manual one to be checked
//                    manualText.setText(type.toString(value.getManualValue()));
//                    manualRadio.setChecked(true);
//
//                } else if (value.getFilterState() == IOValue.SAMPLE) {
//
//                    ((FilterSampleAdapter) sampleSpinner.getAdapter()).getPosition(value.getSampleValue());
//                    sampleRadio.setChecked(true);
//                    // Set the sameple one to be checked
//                }
//            }
        }
    }


    // TODO Need to set an initial checked radio button

    private class DialogImageResourceView extends LinearLayout {

        private GridImageAdapter adapter;

        public DialogImageResourceView(Context context, ServiceIO io) {
            super(context);
            create(context);
        }

        public DialogImageResourceView(Context context) {
            super(context);
            create(context);
        }

        public DialogImageResourceView(Context context, AttributeSet attrs) {
            super(context, attrs);
            create(context);
        }

        public DialogImageResourceView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            create(context);
        }

        private void create(Context context) {

            View v = View.inflate(context, R.layout.dialog_io_app, null);
            this.addView(v);

            GridView imageGrid = (GridView) v.findViewById(R.id.app_grid);

            final Class<R.drawable> c = R.drawable.class;
            final Field[] fields = c.getDeclaredFields();
            ArrayList<Integer> resList = new ArrayList<Integer>();

            for (int i = 0, max = fields.length; i < max; i++) {
                try {
                    resList.add(fields[i].getInt(null));
                } catch (Exception e) {
                    continue;
                }
            }

            adapter = new GridImageAdapter(context, resList);
            imageGrid.setAdapter(adapter);

            // Loading previous ones
            if (item.hasValue()) {
                IOValue value = item.getValue();

                if (value.getFilterState() == IOValue.MANUAL) {
                    int resId = (Integer) value.getManualValue();

                    if (resId != -1) {
                        int position = adapter.getPosition(resId);
                        adapter.selectedIndex = position;
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            positiveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    int res = adapter.getSelected();

                    // Set the value to be the image that has been selected
                    if (item.hasValue()) {
                        item.getValue().setManualValue(res);

                    } else {
                        // Create one
                        IOValue value = new IOValue(item);
                        value.setManualValue(res);
                    }
                }
            });
        }

        private class GridImageAdapter extends ArrayAdapter<Integer> {

            private ArrayList<Integer> values;
            private int selectedIndex = -1;

            public GridImageAdapter(Context context, ArrayList<Integer> values) {
                super(context, R.layout.grid_item_app_selector, values);
                this.values = values;
            }

            public View getView(final int position, View v, ViewGroup parent) {
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.grid_item_app_selector, null);
                }

                if (position != selectedIndex) {
                    v.setBackgroundResource(R.drawable.textview_button);
                } else {
                    v.setBackgroundResource(R.drawable.textview_button_focused);
                }

                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // De-select everything
                        selectedIndex = selectedIndex == position ? -1 : position;
                        notifyDataSetChanged();
                    }
                });

                TextView appName = (TextView) v.findViewById(R.id.load_list_name);
                ImageView appIcon = (ImageView) v.findViewById(R.id.service_icon);

                final Integer item = values.get(position);

                // Load all the icons in the background?
                appName.setVisibility(View.GONE);
                appIcon.setImageDrawable(getContext().getResources().getDrawable(item));

                return v;
            }

            private int getSelected() {

                if (selectedIndex == -1) {
                    return -1;
                }

                return values.get(selectedIndex);
            }
        }
    }

    private class DialogAppView extends LinearLayout {

        private ServiceIO io;

        public DialogAppView(Context context, ServiceIO io) {
            super(context);
            this.io = io;
            create(context);
        }

        public DialogAppView(Context context) {
            super(context);
            create(context);
        }

        public DialogAppView(Context context, AttributeSet attrs) {
            super(context, attrs);
            create(context);
        }

        public DialogAppView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            create(context);
        }

        private void create(Context context) {
            View v = View.inflate(context, R.layout.dialog_io_app, null);
            this.addView(v);

            GridView appGrid = (GridView) v.findViewById(R.id.app_grid);

            // Get a list of the installed apps and then show them on something
            final PackageManager pm = activity.getPackageManager();
            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            final AppChooserAdapter adapter = new AppChooserAdapter(activity, packages, pm);

            appGrid.setAdapter(adapter);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter.selectedIndex == -1) {
                        if (LOG) Log.d(TAG, "No selected index");
                        cancel();
                        return;
                    }

                    // The app they want to load is selectedApp.packageName
                    ApplicationInfo selected = packages.get(adapter.selectedIndex);
                    if (selected == null) {
                        if (LOG) Log.d(TAG, "No selected app info");
                        cancel();
                        return;
                    }

                    if (LOG) Log.d(TAG, "Setting package name to " + selected.packageName);

                    IOValue value = new IOValue(FilterFactory.NONE, selected.packageName, item);
                    item.setValue(value);

                    registry.updateComposite(activity.getComposite());
                    activity.redraw();
                    dismiss();
                }
            });

            if (item.hasValue()) {
                IOValue value = item.getValue();

                // It must be a manual value to be an application
                if (value.getFilterState() == IOValue.MANUAL) {
                    String packageName = item.getType().toString(value.getManualValue());

                    // and now highlight the relevant one in the list
                    int index = adapter.findItem(packageName);
                    if (index == -1) {
                        Toast.makeText(context, "It looks like the application you chose last time has been removed, You'll have to choose another", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.setSelectedIndex(index);
                    }
                }
            }
        }

        private class AppChooserAdapter extends ArrayAdapter<ApplicationInfo> {

            private List<ApplicationInfo> values;
            private PackageManager pm;
            private int selectedIndex;

            public AppChooserAdapter(Context context, List<ApplicationInfo> values, PackageManager pm) {
                super(context, R.layout.grid_item_app_selector, values);

                this.pm = pm;
                this.values = values;
                this.selectedIndex = -1;
            }

            private int findItem(String packageName) {

                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).packageName.equals(packageName)) {
                        return i;
                    }
                }

                return -1;
            }

            private void setSelectedIndex(int index) {
                this.selectedIndex = index;
                notifyDataSetChanged();
            }

            public View getView(final int position, View v, ViewGroup parent) {
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.grid_item_app_selector, null);
                }

                if (position != selectedIndex) {
                    v.setBackgroundResource(R.drawable.textview_button);
                } else {
                    v.setBackgroundResource(R.drawable.textview_button_focused);
                }

                final ApplicationInfo app = values.get(position);

                TextView appName = (TextView) v.findViewById(R.id.load_list_name);
                ImageView appIcon = (ImageView) v.findViewById(R.id.service_icon);

                // Load all the icons in the background?
                appName.setText(app.loadLabel(pm));
                appIcon.setImageDrawable(app.loadIcon(pm));

                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // De-select everything
                        selectedIndex = selectedIndex == position ? -1 : position;
                        notifyDataSetChanged();
                    }
                });

                return v;
            }
        }
    }

    private class DialogIOView extends LinearLayout {

        private RadioGroup radioGroup;
        private RadioButton sampleRadio;
        private RadioButton manualRadio;

        private Spinner sampleSpinner;
        private EditText manualText;

        private ServiceIO io;

        public DialogIOView(Context context, ServiceIO io) {
            super(context);
            this.io = io;
            create(context);
        }

        public DialogIOView(Context context) {
            super(context);
            create(context);
        }

        public DialogIOView(Context context, AttributeSet attrs) {
            super(context, attrs);
            create(context);
        }

        public DialogIOView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            create(context);
        }

        private void create(Context context) {

            View v = View.inflate(context, R.layout.dialog_io_generic, null);
            this.addView(v);

            radioGroup = (RadioGroup) v.findViewById(R.id.io_radio);
            manualRadio = (RadioButton) v.findViewById(R.id.io_radio_text);
            sampleRadio = (RadioButton) v.findViewById(R.id.io_radio_spinner);
            manualText = (EditText) v.findViewById(R.id.io_value_text);
            sampleSpinner = (Spinner) v.findViewById(R.id.io_value_spinner);

            final IOType type = description.getType();
            ArrayList<SampleValue> values = description.getSampleValues();
            if (values == null)
                values = new ArrayList<SampleValue>();

            final boolean hasSamples = values.size() != 0;

            if (!hasSamples) {
                values = new ArrayList<SampleValue>();
                values.add(new SampleValue("No samples", ""));
            }

            if (!type.acceptsManualValues()) {
                manualText.setEnabled(false);
                manualRadio.setEnabled(false);
                sampleRadio.setChecked(true);
                sampleRadio.setEnabled(false);
                radioGroup.setEnabled(false);
            } else {
                // Set the type of the edit text depending on the type
                if (type.typeExtends(IOType.Factory.getType(IOType.Factory.NUMBER))) {
                    manualText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.PASSWORD))) {
                    manualText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }

            sampleSpinner.setAdapter(new FilterSampleAdapter(activity, values));

            if (!hasSamples) {
                sampleSpinner.setEnabled(false);
                manualRadio.setEnabled(false);
                manualRadio.setChecked(true);
                sampleRadio.setEnabled(false);
                radioGroup.setEnabled(false);
            }

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the value they entered - not sure what happens
                    if (manualRadio.isChecked()) {
                        // Then look up the text value
                        Object objectValue = description.getType().fromString(manualText.getText().toString());
                        IOValue value = new IOValue(FilterFactory.NONE, objectValue, io);
                        item.setValue(value);
                    } else if (sampleRadio.isChecked()) {
                        // Then look up the index of the spinner that's selected - shouldn't need to worry about data types
                        SampleValue sampleValue = (SampleValue) sampleSpinner.getSelectedItem();
                        IOValue value = new IOValue(FilterFactory.NONE, sampleValue, io);
                        item.setValue(value);
                    }

                    // The setting of the list values needs to move to the creating of the list. Do an invalidate
                    registry.updateComposite(activity.getComposite());
                    DialogIO.this.activity.redraw();
                    dismiss();
                }
            });

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    // enable or disable the relevant item
                    // Set the condition of the ValueNode
                    if (checkedId == R.id.filter_radio_manual) {
                        sampleSpinner.setEnabled(false);
                        manualText.setEnabled(true);
                    } else { // It must be filter_radio_sample
                        sampleSpinner.setEnabled(true);
                        manualText.setEnabled(false);
                    }
                }
            });

            // Loading saved values
            if (item.hasValue()) {

                IOValue value = item.getValue();

                if (value.getFilterState() == IOValue.MANUAL) {

                    // Load the value from the item and Set the manual one to be checked
                    manualText.setText(type.toString(value.getManualValue()));
                    manualRadio.setChecked(true);

                } else if (value.getFilterState() == IOValue.SAMPLE) {

                    ((FilterSampleAdapter) sampleSpinner.getAdapter()).getPosition(value.getSampleValue());
                    sampleRadio.setChecked(true);
                    // Set the sameple one to be checked
                }
            }
        }
    }
}