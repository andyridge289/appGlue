package com.appglue;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appglue.description.AppDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.layout.WiringMap;
import com.appglue.library.LocalStorage;

import java.util.ArrayList;
import java.util.HashMap;

import static com.appglue.Constants.INDEX;

public class FragmentWiring extends Fragment {
    private int position;

    private WiringMap wiringMap;

    private ComponentService first;
    private ComponentService second;

    private LinearLayout buttonBar;
    private Button filterButton;
    private Button valueButton;

    private int wiringMode;
    public static final int MODE_DEAD = -1;
    public static final int MODE_WIRING = 0;
    public static final int MODE_VALUE = 1;
    public static final int MODE_FILTER = 2;

    HashMap<String, Integer> hueMap;

    public static FragmentWiring create(int position) {
        FragmentWiring fragment = new FragmentWiring();
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
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wiring, container, false);

        hueMap = new HashMap<String, Integer>();

        wiringMap = (WiringMap) rootView.findViewById(R.id.firstWiringMap);

        TextView firstName = (TextView) rootView.findViewById(R.id.first_name);
        TextView secondName = (TextView) rootView.findViewById(R.id.second_name);

        ImageView firstIcon = (ImageView) rootView.findViewById(R.id.first_icon);
        ImageView secondIcon = (ImageView) rootView.findViewById(R.id.second_icon);

        RelativeLayout firstContainer = (RelativeLayout) rootView.findViewById(R.id.wiring_first);
        RelativeLayout secondContainer = (RelativeLayout) rootView.findViewById(R.id.wiring_second);

        buttonBar = (LinearLayout) rootView.findViewById(R.id.value_button_bar);
        filterButton = (Button) rootView.findViewById(R.id.filter_button_all);
        valueButton = (Button) rootView.findViewById(R.id.value_button_all);

        ArrayList<ComponentService> components = ((ActivityWiring) getActivity()).getComponents();

        // TODO Invalid index 0, size is 0

        first = position > 0 ? components.get(position - 1) : null;
        second = position < components.size() ? components.get(position) : null;

        wiringMap.set(first, second);

        LocalStorage localStorage = LocalStorage.getInstance();

        // Set the icon of either to be the big purple plus if there's not a component in that position
        if (first != null) {
            firstName.setText(first.getDescription().getName());
            firstName.setTextColor(Color.BLACK);

            try {
                AppDescription firstApp = first.getDescription().getApp();
                Bitmap b;

                if (firstApp == null) {
                    firstIcon.setBackgroundResource(R.drawable.icon);

                } else {
                    String iconLocation = first.getDescription().getApp().iconLocation();
                    if (iconLocation.equals("")) {
                        firstIcon.setBackgroundResource(R.drawable.icon);
                    }
                    b = localStorage.readIcon(iconLocation);
                    firstIcon.setImageBitmap(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            firstContainer.setBackgroundResource(R.drawable.wiring_component);
            firstContainer.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Clicking on the first container should let you move/rename it
                    return false;
                }
            });
        } else {
            firstName.setText("Add");
            firstName.setTextColor(getResources().getColor(R.color.colorPrimary));
            firstIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
            firstContainer.setBackgroundResource(R.drawable.wiring_add_default);

            // Make it add at this position when we click it
            firstContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FragmentWiringPager) getParentFragment()).chooseComponentFromList(position);

                }
            });
        }

        // Make the right icon be the left half, Make the left icon be the right half
        if (second != null) {
            secondName.setText(second.getDescription().getName());
            secondName.setTextColor(Color.BLACK);
            secondContainer.setBackgroundResource(R.drawable.wiring_component);

            try {
                AppDescription secondApp = second.getDescription().getApp();
                Bitmap b;

                if (secondApp == null) {
                    firstIcon.setBackgroundResource(R.drawable.icon);
                } else {
                    String iconLocation = secondApp.iconLocation();
                    if (iconLocation.equals("")) {
                      firstIcon.setBackgroundResource(R.drawable.icon);
                    }
                    b = localStorage.readIcon(iconLocation);
                    secondIcon.setImageBitmap(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            secondContainer.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Clicking on the second container should let you move/rename it
                    return false;
                }
            });
        } else {
            secondName.setText("Add");
            secondName.setTextColor(getResources().getColor(R.color.colorPrimary));
            secondContainer.setBackgroundResource(R.drawable.wiring_add_default);
            secondIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));

            // Make it add at this position when we click it
            secondContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ActivityWiring) getActivity()).chooseComponentFromList(position, position);
                }
            });
        }

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityWiring wiringActivity = (ActivityWiring) getActivity();
//                if (wiringActivity.getWiringMode() == MODE_FILTER) {
//                    wiringMap.normalMode();
//
//                    redraw();
//                } else {
//                    wiringMap.filterMode();
//                    wiringActivity.setWiringMode(MODE_FILTER);
//                }
            }
        });

        valueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mode == FragmentWiringPager.MODE_VALUE ) {
//                    wiringMap.normalMode();
//                } else {
//                    wiringMap.valueMode();
//                }
            }
        });

        return rootView;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    public ComponentService getFirst() {
        return first;
    }
    public ComponentService getSecond() {
        return second;
    }

    public void redraw() {

        if (buttonBar != null) {
            if (wiringMode == MODE_WIRING) {
                buttonBar.setVisibility(View.GONE);
            } else {
                buttonBar.setVisibility(View.VISIBLE);
            }
        }

        if (wiringMap != null) {
            wiringMap.redraw(true);
        }
    }

    public int getWiringMode() {
        return wiringMode;
    }

    public void setWiringMode(int wiringMode) {
        this.wiringMode = wiringMode;
        redraw();
    }
}
