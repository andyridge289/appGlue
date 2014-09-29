package com.appglue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appglue.description.AppDescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.dialog.DialogApp;
import com.appglue.layout.dialog.DialogFilter;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;

import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
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

        RotateAnimation ranim = (RotateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        ranim.setFillAfter(true); //For the textview to remain at the same place after the rotation


        // Set the icon of either to be the big purple plus if there's not a component in that position
        if (pre != null) {
            preName.setText(pre.getDescription().getName());
//            preName.setAnimation(ranim);
            preName.setTextColor(Color.BLACK);

            try {
                AppDescription firstApp = pre.getDescription().getApp();
                Bitmap b;

                if (firstApp == null) {
                    preIcon.setBackgroundResource(R.drawable.icon);
                } else {
                    String iconLocation = pre.getDescription().getApp().iconLocation();
                    if (iconLocation.equals("")) {
                        preIcon.setBackgroundResource(R.drawable.icon);
                    }
                    b = localStorage.readIcon(iconLocation);
                    preIcon.setImageBitmap(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            preContainer.setBackgroundResource(R.drawable.value_pre);
            preContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Clicking on the first container should let you move/rename it
                    return false;
                }
            });
        } else {
            preName.setText("Add");
            preName.setTextColor(getResources().getColor(R.color.colorPrimary));
            preIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
            preContainer.setBackgroundResource(R.drawable.wiring_add_pre);

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
//            postName.setAnimation(ranim);

            postContainer.setBackgroundResource(R.drawable.value_post);

            try {
                AppDescription firstApp = post.getDescription().getApp();
                Bitmap b;

                if (firstApp == null) {
                    postIcon.setBackgroundResource(R.drawable.icon);
                } else {
                    String iconLocation = post.getDescription().getApp().iconLocation();
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
            postName.setTextColor(getResources().getColor(R.color.colorPrimary));
            postContainer.setBackgroundResource(R.drawable.wiring_add_post);
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

        @SuppressLint("InflateParams")
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
            LinearLayout ioValueContainer = (LinearLayout) v.findViewById(R.id.io_value_container);
            TextView ioValue = (TextView) v.findViewById(R.id.io_value);

            int visibility = item.getDescription().isMandatory() ? View.VISIBLE : View.GONE;
            v.findViewById(R.id.mandatory_bar).setVisibility(visibility);

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View vv) {
                    if (item.getDescription().getType().equals(IOType.Factory.getType(IOType.Factory.APP)))
                        showAppDialog(item);
                    else
                        showIODialog(item);
                }
            });

            // TODO This also needs to show if there is a connection from an output

            ioType.setText(item.getDescription().getType().getName());

            // There can only be one input value
            if (!item.hasValues()) {
                ioValueContainer.setVisibility(View.GONE);
            } else {
                IOValue value = item.getValues().get(0);
                ioValueContainer.setVisibility(View.VISIBLE);
                if (value.getFilterState() == IOValue.MANUAL_FILTER) {
                    String strValue = item.getDescription().getType().toString(value.getManualValue());
                    ioValue.setText(strValue);
                } else if (value.getFilterState() == IOValue.SAMPLE_FILTER) {
                    // Need to look up what the value for this thing is, but return the friendly name not the other thing
                    ioValue.setText(value.getSampleValue().getName());
                }
            }

            return v;
        }
    }

    private class OutputValueAdapter extends ArrayAdapter<IOValue> {
        private ArrayList<IOValue> items;
        private ServiceIO io;

        public OutputValueAdapter(Context parent, ArrayList<IOValue> items, ServiceIO io) {
            super(parent, R.layout.list_item_value_out_item, items);
            this.items = items;
            this.io = io;
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_value_out_item, null);
            }

            final View v = convertView;

            //            if (item.getFilterState() == ServiceIO.UNFILTERED) {
//            } else {
////                IOFilter.FilterValue fv = IOFilter.filters.get(item.getCondition());
////
//                // This is for a manual one
//                if (item.getFilterState() == ServiceIO.MANUAL_FILTER) {
////                    String value = item.getDescription().getType().toString(item.getManualValue());
////                    ioValue.setText(value);
//                } else if (item.getFilterState() == ServiceIO.SAMPLE_FILTER) {
////                    // Need to look up what the value for this thing is, but return the friendly name not the other thing
////                    ioValue.setText(item.getChosenSampleValue().name);
//                }
//            }

            // TODO Output values need to show the value and the


            return v;
        }
    }

    private class OutputAdapter extends ArrayAdapter<ServiceIO> {

        public ArrayList<ServiceIO> items;

        public OutputAdapter(Context parent, ArrayList<ServiceIO> items) {
            super(parent, R.layout.list_item_value_out, items);
            this.items = items;
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_value_out, null);
            }

            final View v = convertView;
            final ServiceIO item = items.get(position);
            final IODescription description = item.getDescription();

            TextView ioName = (TextView) v.findViewById(R.id.io_name);
            ioName.setText(description.getFriendlyName());

            TextView ioType = (TextView) v.findViewById(R.id.io_type);
            ioType.setText(item.getDescription().getType().getName());

            // Clicking on the list item brings up the filter dialog
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View vv) {
                    showFilterDialog(item);
                }
            });

            ListView outputValueList = (ListView) v.findViewById(R.id.io_value_list);

            if (!item.hasValues()) {
                outputValueList.setVisibility(View.GONE);
//                values.add(item)
//                ioValue.setText("");
            } else {
                outputValueList.setAdapter(new OutputValueAdapter(getActivity(), item.getValues(), item));
            }

            return v;
        }
    }
}