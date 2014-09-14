package com.appglue;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appglue.description.datatypes.IOType;
import com.appglue.description.AppDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.dialog.DialogApp;
import com.appglue.layout.dialog.DialogFilter;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.library.IOFilter;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.library.AppGlueConstants.FIRST;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;

public class FragmentValue extends FragmentVW {
    private int position;

    public static Fragment create(int position) {
        FragmentValue fragment = new FragmentValue();
        Bundle args = new Bundle();
        args.putInt(INDEX, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        position = getArguments().getInt(INDEX);
    }

    public int getPosition() {
        return position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_value, container, false);

        TextView preName = (TextView) rootView.findViewById(R.id.pre_name);
        TextView currentName = (TextView) rootView.findViewById(R.id.current_name);
        TextView postName = (TextView) rootView.findViewById(R.id.post_name);

        ImageView preIcon = (ImageView) rootView.findViewById(R.id.pre_icon);
        ImageView postIcon = (ImageView) rootView.findViewById(R.id.post_icon);

        RelativeLayout preContainer = (RelativeLayout) rootView.findViewById(R.id.value_pre);
        RelativeLayout postContainer = (RelativeLayout) rootView.findViewById(R.id.value_post);

        View outputContainer = rootView.findViewById(R.id.outputs);
        View inputContainer = rootView.findViewById(R.id.inputs);
        View noOutputs = rootView.findViewById(R.id.no_outputs);
        View noInputs = rootView.findViewById(R.id.no_inputs);

        ListView outputList = (ListView) rootView.findViewById(R.id.output_list);
        outputList.setClickable(false);

        ListView inputList = (ListView) rootView.findViewById(R.id.input_list);
        inputList.setClickable(false);

        ArrayList<ComponentService> components = ((ActivityWiring) getActivity()).getComponents();
        ComponentService pre = position > 0 ? components.get(position - 1) : null;
        ComponentService current = position >= 0 ?
                (position < components.size() ? components.get(position) : null) :
                null;
        ComponentService post = position < components.size() - 1 ? components.get(position + 1) : null;

        LocalStorage localStorage = LocalStorage.getInstance();

        // Set the icon of either to be the big purple plus if there's not a component in that position
        if (pre != null) {
            preName.setText(pre.getDescription().getName());
            preName.setTextColor(Color.BLACK);

            try {
                AppDescription firstApp = pre.getDescription().app();
                Bitmap b;

                if (firstApp == null) {
                    preIcon.setBackgroundResource(R.drawable.icon);
                } else {
                    String iconLocation = pre.getDescription().app().iconLocation();
                    if (iconLocation.equals("")) {
                        preIcon.setBackgroundResource(R.drawable.icon);
                    }
                    b = localStorage.readIcon(iconLocation);
                    preIcon.setImageBitmap(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            preContainer.setBackgroundResource(R.drawable.wiring_output);
            preContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Clicking on the first container should let you move/rename it
                    return false;
                }
            });
        } else {
            preName.setText("Add");
            preName.setTextColor(getResources().getColor(R.color.android_purple));
            preIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
            preContainer.setBackgroundResource(R.drawable.wiring_add);

            // Make it add at this position when we click it
            preContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), ActivityComponentList.class);
                    i.putExtra(POSITION, position);
                    i.putExtra(FIRST, true);
                    getActivity().startActivityForResult(i, SERVICE_REQUEST);
                }
            });
        }

        // Make the right icon be the left half, Make the left icon be the right half

        if (post != null) {
            postName.setText(post.getDescription().getName());
            postName.setTextColor(Color.BLACK);
            postContainer.setBackgroundResource(R.drawable.wiring_input);

            try {
                AppDescription firstApp = post.getDescription().app();
                Bitmap b;

                if (firstApp == null) {
                    postIcon.setBackgroundResource(R.drawable.icon);
                } else {
                    String iconLocation = post.getDescription().app().iconLocation();
                    if (iconLocation.equals("")) {
                        postIcon.setBackgroundResource(R.drawable.icon);
                    }
                    b = localStorage.readIcon(iconLocation);
                    postIcon.setImageBitmap(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            postContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Clicking on the second container should let you move/rename it
                    return false;
                }
            });
        } else {
            postName.setText("Add");
            postName.setTextColor(getResources().getColor(R.color.android_purple));
            postContainer.setBackgroundResource(R.drawable.wiring_add);
            postIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));

            // Make it add at this position when we click it
            postContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), ActivityComponentList.class);
                    i.putExtra(POSITION, position + 1); // Plus one because we need to add to the right of whatever we already have?
                    Log.e(TAG, "Adding at position: " + position);
                    i.putExtra(FIRST, false);
                    getActivity().startActivityForResult(i, SERVICE_REQUEST);
                }
            });
        }

        if (current != null) {
            currentName.setText(current.getDescription().getName());
            currentName.setTextColor(Color.BLACK);

            ArrayList<ServiceIO> inputs = current.getInputs();
            ArrayList<ServiceIO> outputs = current.getOutputs();
            if (outputs.size() > 0) {
                // There are getOutputs, show the list, hide the none and the add
                outputList.setAdapter(new OutputAdapter(getActivity(), outputs));
                outputContainer.setVisibility(View.VISIBLE);
                noOutputs.setVisibility(View.INVISIBLE);
            } else {
                // There are no getInputs, show the none, hide the list and the add
                outputContainer.setVisibility(View.INVISIBLE);
                noOutputs.setVisibility(View.VISIBLE);
            }

            if (inputs.size() > 0) {
                // There are getOutputs, show the list, hide the none and the add
                inputList.setAdapter(new InputAdapter(getActivity(), inputs));
                inputContainer.setVisibility(View.VISIBLE);
                noInputs.setVisibility(View.INVISIBLE);
            } else {
                // There are no getInputs, show the none, hide the list and the add
                inputContainer.setVisibility(View.INVISIBLE);
                noInputs.setVisibility(View.VISIBLE);
            }
        }

        return rootView;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    public void redraw() {
//        this.outputList.invalidateViews();
//        this.inputList.invalidateViews();
    }

    private void showFilterDialog(final ServiceIO item) {
        DialogFilter df = new DialogFilter((ActivityWiring) getActivity(), item);
        df.show();
    }

    private void showAppDialog(final ServiceIO item) {
        DialogApp da = new DialogApp((ActivityWiring) getActivity(), item);
        da.show();
    }


    private void showIODialog(final ServiceIO item) {
        DialogIO di = new DialogIO((ActivityWiring) getActivity(), item);
        di.show();
    }

    private class InputAdapter extends ArrayAdapter<ServiceIO> {
        public ArrayList<ServiceIO> items;

        public InputAdapter(Context parent, ArrayList<ServiceIO> items) {
            super(parent, R.layout.list_item_value_in, items);
            this.items = items;
        }

        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_value_in, null);
            }

            final View v = convertView;
            final ServiceIO item = items.get(position);
            final IODescription description = item.getDescription();

            TextView ioName = (TextView) v.findViewById(R.id.io_name);
            ioName.setText(item.getDescription().getFriendlyName());

            TextView ioType = (TextView) v.findViewById(R.id.io_type);
            TextView ioValue = (TextView) v.findViewById(R.id.io_value);

            int visibility = item.getDescription().isMandatory() ? View.VISIBLE : View.GONE;
            v.findViewById(R.id.mandatory_bar).setVisibility(visibility);

            ImageView setButton = (ImageView) v.findViewById(R.id.set_button);
            setButton.bringToFront();
            setButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.getDescription().getType().equals(IOType.Factory.getType(IOType.Factory.APP)))
                        showAppDialog(item);
                    else
                        showIODialog(item);
                }
            });

            if (!item.hasValue()) {
                ioType.setText(item.getDescription().getType().getName());
                setButton.setImageResource(R.drawable.ic_add);
                ioValue.setText("");
            } else {
                ioType.setText(item.getDescription().getType().getName() + ": ");
                ioValue.setText(item.getManualValue().toString());
                setButton.setImageResource(R.drawable.ic_add_on);
            }

            // If it's not unfiltered, then it's either manual or not
            if (item.getFilterState() == ServiceIO.UNFILTERED) {
                ioType.setText(item.getDescription().getType().getName());
            } else {
                // This is for a manual one
                if (item.getFilterState() == ServiceIO.MANUAL_FILTER) {
                    String value = item.getDescription().getType().toString(item.getManualValue());
                    ioValue.setText(value);
                } else if (item.getFilterState() == ServiceIO.SAMPLE_FILTER) {
                    // Need to look up what the value for this thing is, but return the friendly name not the other thing
                    ioValue.setText(item.getChosenSampleValue().name);
                }
            }

            setButton.setVisibility(View.VISIBLE);

            return v;
        }
    }

    private class OutputAdapter extends ArrayAdapter<ServiceIO> {
        public ArrayList<ServiceIO> items;

        public OutputAdapter(Context parent, ArrayList<ServiceIO> items) {
            super(parent, R.layout.list_item_value_out, items);
            this.items = items;
        }

        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_wiring_out, null);
            }

            final View v = convertView;
            final ServiceIO item = items.get(position);
            final IODescription description = item.getDescription();

            TextView ioName = (TextView) v.findViewById(R.id.io_name);
            ioName.setText(description.getFriendlyName());

            TextView ioType = (TextView) v.findViewById(R.id.io_type);
            TextView ioValue = (TextView) v.findViewById(R.id.io_value);

            ImageView filterButton = (ImageView) v.findViewById(R.id.filter_button);
            filterButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showFilterDialog(item);
                }
            });

            if (!item.hasValue()) {
                ioType.setText(item.getDescription().getType().getName());
                filterButton.setImageResource(R.drawable.filter_small);
                ioValue.setText("");
            } else {
                ioType.setText(item.getDescription().getType().getName() + ": ");
                ioValue.setText(item.getManualValue().toString());
                filterButton.setImageResource(R.drawable.filter_small_on);
            }

            if (item.getFilterState() == ServiceIO.UNFILTERED) {
                ioType.setText(item.getDescription().getType().getName());
            } else {
                IOFilter.FilterValue fv = IOFilter.filters.get(item.getCondition());

                ioType.setText(item.getDescription().getType().getName() + ": " + fv.text + " ");

                // This is for a manual one
                if (item.getFilterState() == ServiceIO.MANUAL_FILTER) {
                    String value = item.getDescription().getType().toString(item.getManualValue());
                    ioValue.setText(value);
                } else if (item.getFilterState() == ServiceIO.SAMPLE_FILTER) {
                    // Need to look up what the value for this thing is, but return the friendly name not the other thing
                    ioValue.setText(item.getChosenSampleValue().name);
                }
            }

            filterButton.setVisibility(View.VISIBLE);
            v.findViewById(R.id.endpoint).setVisibility(View.GONE);

            return v;
        }
    }


}
