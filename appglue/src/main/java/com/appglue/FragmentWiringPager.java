package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

public class FragmentWiringPager extends Fragment implements ViewPager.OnPageChangeListener {

    private ViewPager wiringPager;
    private WiringPagerAdapter adapter;

    private TextView csNameText;
    private EditText csNameEdit;
    private Button csNameSet;
    private TextView status;

    private ImageView pageLeft;
    private ImageView pageRight;

    private Registry registry;

    private int position = -1;

    public static Fragment create(long compositeId, int position) {

        FragmentWiringPager instance = new FragmentWiringPager();

        Bundle args = new Bundle();
        args.putLong(COMPOSITE_ID, compositeId);
        args.putInt(POSITION, position);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        registry = Registry.getInstance(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        View root = inflater.inflate(R.layout.fragment_wiring_pager, container, false);

        wiringPager = (ViewPager) root.findViewById(R.id.wiring_pager);

        csNameText = (TextView) root.findViewById(R.id.cs_name);
        csNameEdit = (EditText) root.findViewById(R.id.cs_name_edit);
        csNameSet = (Button) root.findViewById(R.id.cs_name_edit_button);

        status = (TextView) root.findViewById(R.id.status);

        pageLeft = (ImageView) root.findViewById(R.id.pager_left);
        pageRight = (ImageView) root.findViewById(R.id.pager_right);

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

        if (getArguments() != null) {
            if (position == -1) {
                position = getArguments().getInt(POSITION, -1);
                Log.d(TAG, "Setting position " + position);
            }
            long compositeId = getArguments().getLong(COMPOSITE_ID);
            if (compositeId != -1) {
                registry.setCurrent(compositeId);
            }
        } else {
            Log.e(TAG, "Arguments null");
            return;
        }

        finishWiringSetup();
    }

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

    private void finishWiringSetup() {

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        final CompositeService cs = registry.getCurrent();
        if (cs.getName().equals("")) {
            csNameText.setText("Temp name");
            csNameEdit.setText("Temp name");
        } else {
            csNameText.setText(cs.getName());
            csNameEdit.setText(cs.getName());
        }

        csNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csNameText.setVisibility(View.GONE);
                csNameEdit.setVisibility(View.VISIBLE);
                csNameSet.setVisibility(View.VISIBLE);
            }
        });

        csNameSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = csNameEdit.getText().toString();
                cs.setName(name);
                registry.updateComposite(cs);
                csNameText.setText(name);

                csNameText.setVisibility(View.VISIBLE);
                csNameEdit.setVisibility(View.GONE);
                csNameSet.setVisibility(View.INVISIBLE);
            }
        });

        adapter = new WiringPagerAdapter(getFragmentManager(), true, cs);
        adapter.notifyDataSetChanged();

        if (wiringPager.getAdapter() == null) {
            wiringPager.setAdapter(adapter);
            wiringPager.setOnPageChangeListener(this);
        }

        if (position != -1) {
            if (position == 0 && cs.getComponents().size() == 1 && !cs.getComponents().get(0).hasInputs()) {
                position = 1;
            }

            onPageSelected(position);
            wiringPager.setCurrentItem(position);
        } else {
            onPageSelected(0);
            wiringPager.setCurrentItem(0);
        }

        if (cs.getComponents().size() == 0) {
            ((ActivityWiring) getActivity()).chooseComponentFromList(true, 1);
        }

        pageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    wiringPager.setCurrentItem(position - 1);
                }
            }
        });

        pageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < adapter.getCount() - 1) {
                    wiringPager.setCurrentItem(position + 1);
                }
            }
        });
    }

    public void redraw(int position) {

        if (registry == null) {
            // It hasn't called create yet. There's no point trying to do anything at all.
            return;
        }

        CompositeService cs = registry.getCurrent();

        // Tell all the fragments to redraw...
        if (wiringPager != null) {
            adapter = new WiringPagerAdapter(getFragmentManager(), true, cs);
            adapter.notifyDataSetChanged();
            wiringPager.setAdapter(adapter);

            if (position != -1 && wiringPager != null) {

                if (position == 0 && cs.getComponents().size() == 1 && !cs.getComponents().get(0).hasInputs()) {
                    position = 1;
                }

                wiringPager.setCurrentItem(position);
                this.position = position;
            }
        }
    }

    public void saveDialog() {
        CompositeService cs = registry.getCurrent();

        if (cs.getID() == 1) {
            // Then it's the temp, we should save it
            String name = csNameEdit.getText().toString();

            SparseArray<ComponentService> comps = cs.getComponents();

            if (name.equals("Temp name")) {
                String tempName = "";
                for (int i = 0; i < comps.size(); i++) {
                    if (i > 0) tempName += " ->  ";
                    tempName += comps.valueAt(i).getDescription().getName();
                }

                name = tempName;
            }

            registry.saveTempAsComposite(name);
        } else if (cs.getID() == -1) {
            // It's not the temp, but we're still saving a new one (I'm not really sure how this has happened)
            if (LOG) Log.d(TAG, "the CS is -1, this might be bad.");
        } else {
            // We're just updating one that already exists
            registry.updateComposite(cs);
        }

        for (ServiceIO io : cs.getMandatoryInputs()) {
            if (!io.hasValueOrConnection()) {
                cs.setEnabled(false);
                Toast.makeText(getActivity(), "You've missed some of the mandatory values, so your composite has been disabled for now", Toast.LENGTH_LONG).show();
            }
        }

        getActivity().finish();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        this.position = position;

        if (position > 0) {
            pageLeft.setEnabled(true);
        } else {
            pageLeft.setEnabled(false);
        }

        if (position < adapter.getCount() - 1) {
            pageRight.setEnabled(true);
        } else {
            pageRight.setEnabled(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setWiringMode(int wiringMode) {
        FragmentWiring fw = adapter.getItem(wiringPager.getCurrentItem());
        if (fw == null) {
            Log.e(TAG, "Current fragment is dead");
        } else {
            fw.setWiringMode(wiringMode);
        }
    }

    public int getCurrentWiringMode() {
        if (adapter == null) {
            return FragmentWiring.MODE_DEAD;
        }

        FragmentWiring fw = adapter.getItem(wiringPager.getCurrentItem());
        if (fw == null) {
            return FragmentWiring.MODE_DEAD;
        } else {
            return fw.getWiringMode();
        }
    }

    public FragmentWiring getCurrentFragment() {
        return adapter.getItem(position);
    }

    public void setPageIndex(int pagerPosition) {
        wiringPager.setCurrentItem(pagerPosition);
    }

    private class WiringPagerAdapter extends FragmentStatePagerAdapter {

        private CompositeService cs;
        private FragmentWiring[] fragments;
        private boolean wiring;

        public WiringPagerAdapter(FragmentManager fragmentManager, boolean wiring, CompositeService cs) {
            super(fragmentManager);
            this.wiring = wiring;
            this.cs = cs;

            fragments = wiring ?
                    new FragmentWiring[cs.getComponents().size() + 1] :
                    new FragmentWiring[cs.getComponents().size()];
        }

        @Override
        public FragmentWiring getItem(int position) {
            if (fragments.length <= position) {
                fragments = new FragmentWiring[cs.getComponents().size() + 1];
            }

            if (fragments[position] == null)
                fragments[position] = FragmentWiring.create(position);

            FragmentWiring f = fragments[position];
            f.redraw();

            return fragments[position];
        }

        @Override
        public int getCount() {
            SparseArray<ComponentService> components = cs.getComponents();

            if (wiring)
                return components == null ? 0 : components.size() + 1;
            else
                return components == null ? 0 : components.size();
        }
    }
}
