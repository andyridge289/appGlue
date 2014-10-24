package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

public class FragmentComposite extends Fragment {

    private ImageView compositeIcon;

    private TextView compositeName;
    private TextView compositeDescription;

    private CheckBox activeCheck;

    private ListView componentList;

    private CompositeService composite;

    private Registry registry;
    private LocalStorage localStorage;

    public FragmentComposite() {
    }

    public static Fragment create() {
        return new FragmentComposite();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());
        localStorage = LocalStorage.getInstance();

        if (getArguments() != null) {
            composite = registry.getComposite(getArguments().getLong(COMPOSITE_ID));
        }

        if (icicle != null) {
            long compositeId = icicle.getLong(COMPOSITE_ID);
            this.composite = registry.getComposite(compositeId);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_composite, container, false);

        compositeIcon = (ImageView) root.findViewById(R.id.schedule_icon);

        compositeName = (TextView) root.findViewById(R.id.composite_name);
        compositeDescription = (TextView) root.findViewById(R.id.composite_description);

        activeCheck = (CheckBox) root.findViewById(R.id.composite_active);
        CheckBox runningCheck = (CheckBox) root.findViewById(R.id.composite_running);

        componentList = (ListView) root.findViewById(R.id.composite_component_list);

        if (composite != null) {
            updatePage();
        }

        return root;
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewStateRestored(Bundle in) {
        super.onViewStateRestored(in);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setData(long id) {
        if (registry == null)
            registry = Registry.getInstance(getActivity());

        composite = registry.getComposite(id);
    }

    private void updatePage() {

        if (composite == null) {
            return;
        }

        if (composite.getComponents().size() > 0) {
            AppDescription app = composite.getComponents().get(0).getDescription().getApp();
            if (app == null || app.iconLocation() == null) {
                compositeIcon.setBackgroundResource(R.drawable.icon);
            } else {
                String iconLocation = app.iconLocation();
                if (localStorage == null)
                    localStorage = LocalStorage.getInstance();
                Bitmap b = localStorage.readIcon(iconLocation);
                compositeIcon.setImageBitmap(b);
            }
        } else {
            compositeIcon.setBackgroundResource(R.drawable.icon);
        }

        compositeName.setText(composite.getName());
        compositeDescription.setText(composite.getDescription());

        activeCheck.setChecked(composite.isEnabled());
        activeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // FIXME Enable or disabled it, but check whether we can enabled it first - mandatory inputs
                // TODO Change active to say if it's enabled or not
            }
        });

        // TODO Need to add the bar at the bottom and enable all the buttons
        // TODO Add a message to direct them to edit it if it's disabled because of mandatoryness

//        runningCheck = (CheckBox) root.findViewById(R.id.composite_running);

        ArrayList<ComponentService> components = composite.getComponentsAL();
        componentList.setAdapter(new CompositeComponentAdapter(getActivity(), components));

        ((ActivityAppGlue) getActivity()).onSectionAttached(ActivityAppGlue.Page.HOME);
    }

    public String getName() {
        return composite.getName();
    }

    private class CompositeComponentAdapter extends ArrayAdapter<ComponentService> {
        private ArrayList<ComponentService> items;

        public CompositeComponentAdapter(Context context, ArrayList<ComponentService> items) {
            super(context, R.layout.li_component_in_composite, items);
            this.items = items;
        }

        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(R.layout.li_component_in_composite, null);
            }

            if (v == null)
                return null;

            final ServiceDescription item = items.get(position).getDescription();

            ImageView icon = (ImageView) v.findViewById(R.id.component_icon);
            TextView name = (TextView) v.findViewById(R.id.component_name);

            try {
                String iconLocation = composite.getComponents().get(0).getDescription().getApp().iconLocation();
                Bitmap b = localStorage.readIcon(iconLocation);

                if (b != null)
                    icon.setImageBitmap(b);
            } catch (Exception e) {
                icon.setImageResource(R.drawable.icon);
            }

            name.setText(item.getName());

            if (item.hasInputs())
                v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.has_io);
            else
                v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.composite_input);

            if (item.hasOutputs())
                v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.has_io);
            else
                v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.composite_output);

//            v.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    Intent intent = new Intent(ActivityComposite.this, ActividtyComponent.class);
//                    intent.putExtra(CLASSNAME, item.getClassName());
//                    startActivity(intent);
//                }
//
//            });

            return v;
        }

    }

    public CompositeService getComposite() {
        return composite;
    }
}
