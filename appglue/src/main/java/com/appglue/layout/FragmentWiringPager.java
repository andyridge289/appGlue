package com.appglue.layout;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
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
import android.widget.Toast;

import com.appglue.AppGlueFragment;
import com.appglue.R;
import com.appglue.WiringActivity;
import com.appglue.engine.model.ComponentService;
import com.appglue.engine.model.CompositeService;
import com.appglue.layout.view.FloatingActionButton;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.Tuple;
import com.appglue.serviceregistry.Registry;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import static com.appglue.Constants.POSITION;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

public class FragmentWiringPager extends Fragment implements ViewPager.OnPageChangeListener, AppGlueFragment {

    private ViewPager wiringPager;
    private WiringPagerAdapter adapter;

    private TextView csNameText;
    private EditText csNameEdit;
    private Button csNameSet;
    private TextView status;

    private FrameLayout overviewParent;
    private FrameLayout overviewContainer;
    private View currentPage;

    private int lastLeft = -1;
    private int overviewPosition = 0;
    private View overviewOverlay;

    private FloatingActionButton overlayLeft;
    private FloatingActionButton overlayRight;
    private FloatingActionButton overlayRemove;
    private int overviewSelectedIndex = -1;

    private ArrayList<Tuple<Integer, View>> componentPositions;
    private Tuple<Integer, Integer> moved;

    private ImageView pageLeft;
    private ImageView pageRight;

    private Registry registry;

    private int position = -1;

    private boolean longPress = false;

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

        overviewOverlay = root.findViewById(R.id.overview_overlay);
        overlayLeft = (FloatingActionButton) root.findViewById(R.id.overview_left);
        overlayRight = (FloatingActionButton) root.findViewById(R.id.overview_right);
        overlayRemove = (FloatingActionButton) root.findViewById(R.id.overview_remove);

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
                Logger.d("Setting position " + position);
            }
            long compositeId = getArguments().getLong(COMPOSITE_ID);
            if (compositeId != -1) {
                registry.setCurrent(compositeId);
            }
        } else {
            Logger.e("Arguments null");
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
            ((WiringActivity) getActivity()).chooseComponentFromList(true, 1);
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

        overlayLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Move the component right
                int first = cs.isMovable(overviewSelectedIndex - 1);
                int second = cs.isMovable(overviewSelectedIndex);

                if ((first == 0 && second == 0) || (first == CompositeService.FILTERS && second == CompositeService.FILTERS)) {
                    cs.swap(overviewSelectedIndex - 1, overviewSelectedIndex);
                    registry.updateCurrent();
                } else {
                    Toast.makeText(getActivity(), "This component or the one next to it has connections, you should remove these before swapping them over", Toast.LENGTH_SHORT).show();
                }

                moved = new Tuple<Integer, Integer>(overviewSelectedIndex - 1, overviewSelectedIndex);
                overviewOverlay.setVisibility(View.GONE);
                overviewSelectedIndex = -1;
                redrawAll();
            }
        });

        overlayRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Move the component right
                int first = cs.isMovable(overviewSelectedIndex);
                int second = cs.isMovable(overviewSelectedIndex + 1);

                if ((first == 0 && second == 0) || (first == CompositeService.FILTERS && second == CompositeService.FILTERS)) {
                    cs.swap(overviewSelectedIndex, overviewSelectedIndex + 1);
                    registry.updateCurrent();
                } else {
                    Toast.makeText(getActivity(), "This component or the one next to it has connections, you should remove these before swapping them over", Toast.LENGTH_SHORT).show();
                }

                moved = new Tuple<Integer, Integer>(overviewSelectedIndex, overviewSelectedIndex + 1);
                overviewOverlay.setVisibility(View.GONE);
                overviewSelectedIndex = -1;
                redrawAll();
            }
        });

        overlayRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Remove the component
                cs.remove(overviewSelectedIndex);
                registry.updateCurrent();
                overviewOverlay.setVisibility(View.GONE);
                moved = new Tuple<Integer, Integer>(overviewSelectedIndex, -1);
                overviewSelectedIndex = -1;
                if (cs.getComponents().size() == 0) {
                    getActivity().finish();
                } else if (FragmentWiringPager.this.position > cs.getComponents().size() - 1) {
                    FragmentWiringPager.this.position = cs.getComponents().size() - 1;
                }

                redrawAll();
            }
        });

        overviewOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overviewOverlay.setVisibility(View.GONE);
            }
        });
    }

    public void redrawAll() {

        if (registry == null) {
            // It hasn't called create yet. There's no point trying to do anything at all.
            registry = Registry.getInstance(getActivity());
        }

        registry.getCurrent(true);
        if (wiringPager != null) {

            for (int i = 0; i < adapter.getCount(); i++) {
                redraw(i, false);
            }
        }

    }

    public void redraw(int position, boolean overlay) {

        if (registry == null) {
            // It hasn't called create yet. There's no point trying to do anything at all.
            registry = Registry.getInstance(getActivity());
        }

        CompositeService cs = registry.getCurrent(true);

        // Tell all the fragments to redraw...
        if (wiringPager != null) {

            if (overlay) {
                overviewDraw();
            }

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
        final CompositeService composite = registry.getCurrent(false);
        int width = (w + m) * composite.getComponents().size();

        if (moved == null) {
            componentPositions = new ArrayList<Tuple<Integer, View>>();
            overviewContainer.removeAllViews();
            int componentOffset = w + m;

            overviewParent.setLayoutParams(new FrameLayout.LayoutParams(width + componentOffset, ViewGroup.LayoutParams.MATCH_PARENT));
            overviewContainer.setLayoutParams(new FrameLayout.LayoutParams(width + componentOffset, ViewGroup.LayoutParams.MATCH_PARENT));

            // We need to resize the parent based on how many components there are
            overviewParent.setMinimumWidth(width + w + m);
            overviewContainer.setMinimumWidth(width + w + m);
            int allLeft = w / 2 + m / 2;

            for (int i = 0; i < composite.getComponents().size(); i++) {

                TextView tv = new TextView(new ContextThemeWrapper(getActivity(), R.style.overview_component));
                tv.setText(composite.getComponent(i).getDescription().getShortName());

                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT);
                lp2.setMargins(m / 2, 0, 0, m / 2);
                tv.setLayoutParams(lp2);
                int left = allLeft + (i * componentOffset);
                tv.setX(left);

                final int index = i;

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wiringPager.setCurrentItem(index);
                    }
                });

                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        SparseArray<ComponentService> components = registry.getCurrent(false).getComponents();

                        if (index == 0 || (index == 1 && components.get(0).getDescription().isTrigger())) {
                            overlayLeft.setEnabled(false);
                        } else {
                            overlayLeft.setEnabled(true);
                        }

                        if (index == components.size() - 1 || components.get(index).getDescription().isTrigger()) {
                            overlayRight.setEnabled(false);
                        } else {
                            overlayRight.setEnabled(true);
                        }

                        overviewSelectedIndex = index;
                        overviewOverlay.setVisibility(View.VISIBLE);
                        return true;
                    }
                });

                overviewContainer.addView(tv);
                componentPositions.add(new Tuple<Integer, View>(left, tv));
            }

        } else {
            // They have moved something, so the values in componentPositions should be okay

            if (moved.b == -1) {
                // TODO Then they have deleted the thing at [a], so move everything higher than that to the position to the left
                Tuple<Integer, View> removed = componentPositions.get(moved.a);
                ((ViewGroup) removed.b.getParent()).removeView(removed.b);

                int index = componentPositions.size() - 2; // The second to last one

                while(index >= moved.a) {

                    Tuple<Integer, View> first = componentPositions.get(index);
                    Tuple<Integer, View> second = componentPositions.get(index + 1);

                    // Sort out the values in the arraylist so we can do this again in future
                    Tuple<Integer, View> third = new Tuple<Integer, View>(first.a, second.b);
                    componentPositions.remove(index + 1);
                    componentPositions.add(index + 1, third);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(third.b, "X", third.a);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.start();
//                    Logger.d("Moving " + index + " to " + third.a + "px");
//                    third.b.setX(third.a);
                    index--;
                }
            } else {
                // TODO Swap the things at a and b
                Tuple<Integer, View> first = componentPositions.get(moved.a);
                Tuple<Integer, View> second = componentPositions.get(moved.b);

                ObjectAnimator animator = ObjectAnimator.ofFloat(first.b, "X", second.a);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();

                ObjectAnimator animator2 = ObjectAnimator.ofFloat(second.b, "X", second.a);
                animator2.setInterpolator(new AccelerateDecelerateInterpolator());
                animator2.start();

                int temp = first.a;
                first = new Tuple<Integer, View>(second.a, first.b);
                second = new Tuple<Integer, View>(temp, second.b);

                componentPositions.remove((int) moved.b);
                componentPositions.remove((int) moved.a);
                componentPositions.add(moved.a, second);
                componentPositions.add(moved.b, first);
            }

            moved = null;
        }

        int offset = w / 2 + m / 2;
        int left;

        if (position == componentPositions.size()) {
            // Go right of the last one
            left = componentPositions.get(componentPositions.size() - 1).a + offset;

        } else {
            // Go left of the ith one
            left = componentPositions.get(position).a - offset;
        }

        // FIXME Need to do some tests for moving the components around and then updating them

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

                if (Build.VERSION.SDK_INT < 16) {
                    //noinspection deprecation
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

        ((WiringActivity) getActivity()).saveDialog(name, false);
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

    public FragmentWiring getCurrentFragment() {
        return adapter.getItem(position);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public String onCreateOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.wiring_group, true);
        return "Create composite";
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
