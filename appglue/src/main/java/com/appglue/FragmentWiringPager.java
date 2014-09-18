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
import android.widget.TextView;

import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.layout.DepthPageTransformer;
import com.appglue.serviceregistry.Registry;

import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class FragmentWiringPager extends Fragment implements ViewPager.OnPageChangeListener {

    private static FragmentWiringPager instance;

    private CompositeService cs;

    private ViewPager wiringPager;
    private ViewPager valuePager;

    private WiringPagerAdapter wiringPagerAdapter;
    private WiringPagerAdapter valuePagerAdapter;

    private int mode;
    public static final int MODE_WIRING = 0;
    public static final int MODE_VALUE = 1;

    private TextView csNameText;
	private EditText csNameEdit;
	private Button csNameSet;
    private TextView pageIndexText;
    private TextView status;

    private Registry registry;

    private int position;

    public static Fragment create(long compositeId) {

        FragmentWiringPager instance = new FragmentWiringPager();

        Bundle args = new Bundle();
        args.putLong(COMPOSITE_ID, compositeId);
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
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        View root = inflater.inflate(R.layout.fragment_wiring_pager, container, false);

        wiringPager = (ViewPager) root.findViewById(R.id.wiring_pager);
        valuePager = (ViewPager) root.findViewById(R.id.value_pager);
        valuePager.setPageTransformer(true, new DepthPageTransformer());

        csNameText = (TextView) root.findViewById(R.id.cs_name);
        csNameEdit = (EditText) root.findViewById(R.id.cs_name_edit);
        csNameSet = (Button) root.findViewById(R.id.cs_name_edit_button);

        status = (TextView) root.findViewById(R.id.status);
        pageIndexText = (TextView) root.findViewById(R.id.page_index);

        registry = Registry.getInstance(getActivity());

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

        if(getArguments() != null) {
            position = getArguments().getInt(POSITION, -1);
            long compositeId = getArguments().getLong(COMPOSITE_ID);
            if(compositeId != -1) {
                cs = registry.getComposite(compositeId);
            }
        } else {
            Log.e(TAG, "Arguments null");
            return;
        }

        finishWiringSetup();
        setWiringMode(MODE_WIRING);

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

//        Bundle args = new Bundle();
//        args.putLong(COMPOSITE_ID, cs.getID());
//        this.setArguments(args);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void finishWiringSetup()
    {
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

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
				registry.updateComposite(cs);
				csNameText.setText(name);

				csNameText.setVisibility(View.VISIBLE);
				csNameEdit.setVisibility(View.GONE);
				csNameSet.setVisibility(View.INVISIBLE);
			}
		});

        wiringPagerAdapter = new WiringPagerAdapter(getFragmentManager(), true);
        valuePagerAdapter = new WiringPagerAdapter(getFragmentManager(), false);

        wiringPagerAdapter.notifyDataSetChanged();
        valuePagerAdapter.notifyDataSetChanged();

        wiringPager.setAdapter(wiringPagerAdapter);
        wiringPager.setOnPageChangeListener(this);

        valuePager.setAdapter(valuePagerAdapter);
        valuePager.setOnPageChangeListener(this);

        if (position != -1) {
            wiringPager.setCurrentItem(position);
            valuePager.setCurrentItem(position);
        }
	}

    int getWiringMode() {
        return mode;
    }

    public void setWiringMode(int mode) {
        this.mode = mode;

        ViewPager show;
        ViewPager hide;

        switch(mode) {
            case MODE_WIRING:
                show = wiringPager;
                hide = valuePager;
                break;

            case MODE_VALUE:
                show = valuePager;
                hide = wiringPager;
                break;

            default:
                show = wiringPager;
                hide = valuePager;
                break;
        }

        int current = hide.getCurrentItem();
        show.setCurrentItem(current);

        show.setVisibility(View.VISIBLE);
        hide.setVisibility(View.GONE);
        setPageIndex(current);

        redraw();
    }


    public void redraw()
    {
        WiringPagerAdapter adapter = mode == MODE_WIRING ? wiringPagerAdapter : valuePagerAdapter;

		// Tell all the fragments to redraw...
        for (int i = 0; i < adapter.getCount(); i++) {
            FragmentVW f = (FragmentVW) adapter.getItem(i);
            f.redraw();
		}
    }

    public void saveDialog()
    {
        if(cs.getID() == 1)
        {
            // Then it's the temp, we should save it
            String name = csNameEdit.getText().toString();

            SparseArray<ComponentService> comps = cs.getComponents();

            if(name.equals("Temp name"))
            {
                String tempName = "";
                for(int i = 0; i < comps.size(); i++) {
                    if(i > 0) tempName += " ->  ";
                    tempName += comps.valueAt(i).getDescription().getName();
                }

                name = tempName;
            }

            registry.saveTempAsComposite(name);
        }
        else if(cs.getID() == -1)
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

        getActivity().finish();
    }

    private void setPageIndex(int index) {
        if (mode == MODE_WIRING) {
            pageIndexText.setText(index + " - " + (index + 1) + " / " + (valuePagerAdapter.getCount() + 1));
        } else {
            pageIndexText.setText(index + " / " + valuePagerAdapter.getCount());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setPageIndex(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
            SparseArray<ComponentService> components = cs.getComponents();

            if (wiring)
                return components == null ? 0 : components.size() + 1;
            else
                return components == null ? 0 : components.size();
        }
    }
}
