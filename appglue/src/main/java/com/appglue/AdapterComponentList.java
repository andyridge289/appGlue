package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;

class AdapterComponentList extends ArrayAdapter<ServiceDescription> {

    ArrayList<ServiceDescription> originalItems;
    private ArrayList<ServiceDescription> items;

    private Activity parent;

    private FragmentComponentListPager parentFragment;

    private final Object lock = new Object();

    public AdapterComponentList(Context context, ArrayList<ServiceDescription> items, FragmentComponentListPager parentFragment) {
        super(context, R.layout.component_list_item, items);

        LocalStorage localStorage = LocalStorage.getInstance();

        this.parent = (Activity) context;
        this.parentFragment = parentFragment;

        this.items = items;
        cloneItems();
    }

    private void cloneItems() {
        originalItems = new ArrayList<ServiceDescription>();

        for (ServiceDescription item : items) {
            originalItems.add(item);
        }
    }

    @Override
    public int getCount() {
        synchronized (lock) {
            return items != null ? items.size() : 0;
        }
    }

    @Override
    public ServiceDescription getItem(int item) {
        ServiceDescription gi;
        synchronized (lock) {
            gi = items != null ? items.get(item) : null;
        }
        return gi;
    }

    @Override
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View v = convertView;
        LayoutInflater vi = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (v == null)
            v = vi.inflate(R.layout.component_list_item, null);

        ServiceDescription sd;
        synchronized (lock) {
            sd = items.get(position);
        }

        if (sd == null)
            return v;

        ImageView appIcon = (ImageView) v.findViewById(R.id.component_app_icon);
        appIcon.setImageResource(R.drawable.icon); // TODO This needs to be cleverer

        // FIXME Component search list page is totally broken
        // TODO Set this if we can use it in the preferences
//        icon.setImageResource(R.drawable.ic_extension_black_48dp);
//        if (sd.getServiceType() == ServiceType.IN_APP)
//            icon.setImageResource(R.drawable.icon);
//        else if (sd.getServiceType() == ServiceType.LOCAL) {
//            if (sd.getApp() == null) {
//                icon.setImageResource(R.drawable.ic_lock_silent_mode_vibrate);
//            } else {
//                Drawable d = new BitmapDrawable(parent.getResources(), localStorage.readIcon(sd.getApp().iconLocation()));
//                icon.setImageDrawable(d);
//            }
//        } else {
//            icon.setImageResource(R.drawable.ic_menu_upload);
//        }
        TextView serviceName = (TextView) v.findViewById(R.id.service_name);
        serviceName.setText(sd.getName());

        LinearLayout flagContainer = (LinearLayout) v.findViewById(R.id.flag_container);
        flagContainer.removeAllViews();

        if (sd.hasFlag(ComposableService.FLAG_TRIGGER)) {

            ImageView iv = new ImageView(getContext());
            iv.setBackgroundResource(R.drawable.ic_trigger);
            flagContainer.addView(iv);

            TextView tv = new TextView(getContext());
            tv.setText(getContext().getResources().getString(R.string.trigger));
            flagContainer.addView(tv);
        }

        // TODO public static final int FLAG_MONEY = 0x2;
        // TODO public static final int FLAG_NETWORK = 0x4;
        // TODO public static final int FLAG_DELAY = 0x8;
        // TODO public static final int FLAG_LOCATION = 0x10;

        if (sd.hasInputs()) {
            LinearLayout inputs = (LinearLayout) v.findViewById(R.id.comp_item_inputs);
            inputs.setBackgroundResource(R.drawable.has_io);
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//            lp.weight = 1;
//            inputs.removeAllViews();
//            int num = Math.min(sd.getInputs().size(), 4);
//            for (int i = 0; i < num; i++) {
//                FloatingActionButton fab = new FloatingActionButton(getContext());
//                fab.setColor(getContext().getResources().getColor(R.color.material_cyan));
//                fab.setLayoutParams(lp);
//                inputs.addView(fab);
//            }
        } else {
            v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
        }

        if (sd.hasOutputs()) {
            LinearLayout outputs = (LinearLayout) v.findViewById(R.id.comp_item_outputs);
            outputs.setBackgroundResource(R.drawable.has_io);
//            for (int i = 0; i < sd.getOutputs().size(); i++) {
//                FloatingActionButton fab = new FloatingActionButton(getContext());
//                fab.setColor(getContext().getResources().getColor(R.color.material_amber));
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
//                lp.weight = 1;
//                fab.setLayoutParams(lp);
//                outputs.addView(fab);
//            }
        } else {
            v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);
        }

        final ServiceDescription sd2 = sd;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentFragment.isJustList()) {
                    // Tell the parent fragment to show the component page
                    parentFragment.showServiceDescription(sd2.getClassName());
                } else {
                    // Choose the component to go in the composite
                    if (parentFragment.getActivity() != null) {
                        ((ActivityWiring) parentFragment.getActivity()).chooseItem(sd2.getClassName());
                    }
                }

            }
        });

        return v;
    }
}
