package com.appglue;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

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

    //    private HorizontalScrollView overviewScroll;
    private FrameLayout overviewParent;
    private FrameLayout overviewContainer;
    private View currentPage;
    private int lastLeft = -1;
    private int overviewPosition = 0;

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

        overviewParent = (FrameLayout) root.findViewById(R.id.overview_parent);
        overviewContainer = (FrameLayout) root.findViewById(R.id.overview_page_container);
        currentPage = root.findViewById(R.id.page_indicator);

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

        final CompositeService cs = registry.getCurrent(false);
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
            registry = Registry.getInstance(getActivity());
        }

        CompositeService cs = registry.getCurrent(true);

        // Tell all the fragments to redraw...
        if (wiringPager != null) {
            overviewDraw();

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

    private void overviewRedraw() {

        if (overviewContainer.getWidth() == 0) {
            overviewDraw();
            return;
        }

        if (getActivity() == null) {
            return;
        }

        int w = (int) getActivity().getResources().getDimension(R.dimen.wi_page_width);
        int m = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 2);

        ArrayList<Integer> components = new ArrayList<Integer>();
        final CompositeService composite = registry.getCurrent(false);

        int width = (w + m) * composite.getComponents().size();

        if (components.size() != composite.getComponents().size()) {

            overviewContainer.removeAllViews();
            int componentOffset = w + m;

            overviewParent.setLayoutParams(new LinearLayout.LayoutParams(width + componentOffset, ViewGroup.LayoutParams.MATCH_PARENT));
            overviewContainer.setLayoutParams(new FrameLayout.LayoutParams(width + componentOffset, ViewGroup.LayoutParams.MATCH_PARENT));

            // We need to resize the parent based on how many components there are
            overviewParent.setMinimumWidth(width + w + m);
            overviewContainer.setMinimumWidth(width + w + m);
//            Log.d(TAG, "Setting width " + (width + w + m));
            int allLeft = w / 2 + m / 2; //(overviewContainer.getWidth() / 2) - (width / 2) - overviewContainer.getLeft();

            for (int i = 0; i < composite.getComponents().size(); i++) {

                TextView tv = new TextView(new ContextThemeWrapper(getActivity(), R.style.overview_component));
                tv.setText(composite.getComponent(i).getDescription().getShortName());

                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT);
                lp2.setMargins(m / 2, 0, 0, m / 2);
                tv.setLayoutParams(lp2);
                int left = allLeft + (i * componentOffset);
                tv.setX(left);

                tv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                overviewContainer.addView(tv);
                components.add(left);
            }
        }

        int offset = w / 2 + m / 2;
        int left = 0;

        if (position == components.size()) {
            // Go right of the last one
            left = components.get(components.size() - 1) + offset;

        } else {
            // Go left of the ith one
            left = components.get(position) - offset;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(currentPage, "X", left);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int pageWidth = displaymetrics.widthPixels;

        int maxRight = 0;
        int maxLeft = 0 - (width + w + m - pageWidth);

        if (lastLeft > left) {
            // Then we are moving left, so we might need to move the container right
            if (overviewPosition + left < pageWidth / 4) {
                // Then the thing is on the left of the screen

                int newLeft = Math.min(maxRight, overviewPosition + 3 * (w + m));
                overviewParent.setX(newLeft);
                overviewPosition = newLeft;
            }

        } else {
            // We are moving right, we might need to move the container left
            if (overviewPosition + left > 3 * pageWidth / 4 - w - m) {
                // Then the thing is on the right of the screen
                int newLeft = Math.max(maxLeft, overviewPosition - 3 * (w + m));
                overviewParent.setX(newLeft);
                overviewPosition = newLeft;
            }
        }

        lastLeft = left;
    }

    private void overviewDraw() {

        ViewTreeObserver textViewTreeObserver = overviewContainer.getViewTreeObserver();
        textViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {

                // Put the things in
                overviewRedraw();

                //Do your operations here.
                if (Build.VERSION.SDK_INT < 16) {
                    overviewContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    overviewContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    public int getPosition() {
        return position;
    }

    public void saveDialog() {
        // Then it's the temp, we should save it
        String name = csNameEdit.getText().toString();
        CompositeService cs = registry.getCurrent(false);
        SparseArray<ComponentService> comps = cs.getComponents();

        if (name.equals("Temp name")) {
            String tempName = "";
            for (int i = 0; i < comps.size(); i++) {
                if (i > 0) tempName += " ->  ";
                tempName += comps.valueAt(i).getDescription().getName();
            }

            name = tempName;
        }

        ((ActivityWiring) getActivity()).saveDialog(name, false);
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

        overviewRedraw();
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
