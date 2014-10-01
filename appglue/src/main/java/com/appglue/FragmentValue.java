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
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.dialog.DialogApp;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.FIRST;
import static com.appglue.library.AppGlueConstants.IO_ID;
import static com.appglue.library.AppGlueConstants.COMPONENT_ID;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;

public class FragmentValue extends Fragment {

    private Registry registry;

    private CompositeService composite;
    private ServiceIO io;

    public static Fragment create(ServiceIO io) {
        FragmentValue fragment = new FragmentValue();
        Bundle args = new Bundle();
        args.putLong(IO_ID, io.getID());
        args.putLong(COMPONENT_ID, io.getComponent().getID());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());
        composite = registry.getCurrent();

        if (getArguments() != null) {
            ComponentService component = composite.getComponent(getArguments().getLong(COMPONENT_ID));
            io = component.getIO(getArguments().getLong(IO_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_valuelist, container, false);

        TextView ioName = (TextView) root.findViewById(R.id.io_name);
        ioName.setText(io.getDescription().getFriendlyName());

        TextView ioType = (TextView) root.findViewById(R.id.io_type);
        ioType.setText(io.getType().getName());

//        TextView preName = (TextView) rootView.findViewById(R.id.pre_name);
//        TextView currentName = (TextView) rootView.findViewById(R.id.current_name);
//        TextView postName = (TextView) rootView.findViewById(R.id.post_name);
//
//        ImageView preIcon = (ImageView) rootView.findViewById(R.id.pre_icon);
//        ImageView postIcon = (ImageView) rootView.findViewById(R.id.post_icon);
//
//        RelativeLayout preContainer = (RelativeLayout) rootView.findViewById(R.id.value_pre);
//        RelativeLayout postContainer = (RelativeLayout) rootView.findViewById(R.id.value_post);
//
//        View outputContainer = rootView.findViewById(R.id.outputs);
//        View inputContainer = rootView.findViewById(R.id.inputs);
//        View noOutputs = rootView.findViewById(R.id.no_outputs);
//        View noInputs = rootView.findViewById(R.id.no_inputs);
//
//        ListView outputList = (ListView) rootView.findViewById(R.id.output_list);
//        outputList.setClickable(false);
//
//        ListView inputList = (ListView) rootView.findViewById(R.id.input_list);
//        inputList.setClickable(false);
//
//        ArrayList<ComponentService> components = ((ActivityWiring) getActivity()).getComponents();
//        ComponentService pre = position > 0 ? components.get(position - 1) : null;
//        ComponentService current = position >= 0 ?
//                (position < components.size() ? components.get(position) : null) :
//                null;
//        ComponentService post = position < components.size() - 1 ? components.get(position + 1) : null;
//
//        LocalStorage localStorage = LocalStorage.getInstance();
//
//        RotateAnimation ranim = (RotateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
//        ranim.setFillAfter(true); //For the textview to remain at the same place after the rotation
//
//
//        // Set the icon of either to be the big purple plus if there's not a component in that position
//        if (pre != null) {
//            preName.setText(pre.getDescription().getName());
////            preName.setAnimation(ranim);
//            preName.setTextColor(Color.BLACK);
//
//            try {
//                AppDescription firstApp = pre.getDescription().getApp();
//                Bitmap b;
//
//                if (firstApp == null) {
//                    preIcon.setBackgroundResource(R.drawable.icon);
//                } else {
//                    String iconLocation = pre.getDescription().getApp().iconLocation();
//                    if (iconLocation.equals("")) {
//                        preIcon.setBackgroundResource(R.drawable.icon);
//                    }
//                    b = localStorage.readIcon(iconLocation);
//                    preIcon.setImageBitmap(b);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            preContainer.setBackgroundResource(R.drawable.value_pre);
//            preContainer.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    // Clicking on the first container should let you move/rename it
//                    return false;
//                }
//            });
//        } else {
//            preName.setText("Add");
//            preName.setTextColor(getResources().getColor(R.color.colorPrimary));
//            preIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
//            preContainer.setBackgroundResource(R.drawable.wiring_add_pre);
//
//            // Make it add at this position when we click it
//            preContainer.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent i = new Intent(getActivity(), ActivityComponentList.class);
//                    i.putExtra(POSITION, position);
//                    i.putExtra(FIRST, true);
//                    getActivity().startActivityForResult(i, SERVICE_REQUEST);
//                }
//            });
//        }
//
//        // Make the right icon be the left half, Make the left icon be the right half
//
//        if (post != null) {
//            postName.setText(post.getDescription().getName());
//            postName.setTextColor(Color.BLACK);
////            postName.setAnimation(ranim);
//
//            postContainer.setBackgroundResource(R.drawable.value_post);
//
//            try {
//                AppDescription firstApp = post.getDescription().getApp();
//                Bitmap b;
//
//                if (firstApp == null) {
//                    postIcon.setBackgroundResource(R.drawable.icon);
//                } else {
//                    String iconLocation = post.getDescription().getApp().iconLocation();
//                    if (iconLocation.equals("")) {
//                        postIcon.setBackgroundResource(R.drawable.icon);
//                    }
//                    b = localStorage.readIcon(iconLocation);
//                    postIcon.setImageBitmap(b);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            postContainer.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    // Clicking on the second container should let you move/rename it
//                    return false;
//                }
//            });
//        } else {
//            postName.setText("Add");
//            postName.setTextColor(getResources().getColor(R.color.colorPrimary));
//            postContainer.setBackgroundResource(R.drawable.wiring_add_post);
//            postIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
//
//            // Make it add at this position when we click it
//            postContainer.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent i = new Intent(getActivity(), ActivityComponentList.class);
//                    i.putExtra(POSITION, position + 1); // Plus one because we need to add to the right of whatever we already have?
//                    Log.e(TAG, "Adding at position: " + position);
//                    i.putExtra(FIRST, false);
//                    getActivity().startActivityForResult(i, SERVICE_REQUEST);
//                }
//            });
//        }
//
//        if (current != null) {
//            currentName.setText(current.getDescription().getName());
//            currentName.setTextColor(Color.BLACK);
//
//            ArrayList<ServiceIO> inputs = current.getInputs();
//            ArrayList<ServiceIO> outputs = current.getOutputs();
//            if (outputs.size() > 0) {
//                // There are getOutputs, show the list, hide the none and the add
//                outputList.setAdapter(new OutputAdapter(getActivity(), outputs));
//                outputContainer.setVisibility(View.VISIBLE);
//                noOutputs.setVisibility(View.INVISIBLE);
//            } else {
//                // There are no getInputs, show the none, hide the list and the add
//                outputContainer.setVisibility(View.INVISIBLE);
//                noOutputs.setVisibility(View.VISIBLE);
//            }
//
//            if (inputs.size() > 0) {
//                // There are getOutputs, show the list, hide the none and the add
//                inputList.setAdapter(new InputAdapter(getActivity(), inputs));
//                inputContainer.setVisibility(View.VISIBLE);
//                noInputs.setVisibility(View.INVISIBLE);
//            } else {
//                // There are no getInputs, show the none, hide the list and the add
//                inputContainer.setVisibility(View.INVISIBLE);
//                noInputs.setVisibility(View.VISIBLE);
//            }
//        }

        return root;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    private class ValueAdapter extends ArrayAdapter<IOValue> {

        private ArrayList<IOValue> items;

        public ValueAdapter(Context context, ArrayList<IOValue> items) {
            super(context, R.layout.dialog_filter_value);
            this.items = items;
        }
    }
}