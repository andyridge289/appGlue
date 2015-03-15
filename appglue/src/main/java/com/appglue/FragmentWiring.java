package com.appglue;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.appglue.description.AppDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.layout.FloatingActionButton;
import com.appglue.layout.VerticalTextView;
import com.appglue.layout.WiringMap;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.HashMap;

import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.TAG;

public class FragmentWiring extends Fragment {
    private int position;

    private WiringMap wiringMap;

    private ComponentService first;
    private ComponentService second;

    private Button wiringButton;
    private Button filterButton;
    private Button valueButton;

    private Registry registry;

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

        if (getArguments() != null) {
            position = getArguments().getInt(INDEX);
        }

        registry = Registry.getInstance(getActivity());
    }

    public int getPosition() {
        return position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wiring, container, false);

        hueMap = new HashMap<String, Integer>();
        wiringMap = (WiringMap) rootView.findViewById(R.id.firstWiringMap);
        wiringMap.setPosition(position);

        VerticalTextView firstName = (VerticalTextView) rootView.findViewById(R.id.first_name);
        VerticalTextView secondName = (VerticalTextView) rootView.findViewById(R.id.second_name);

        ImageView firstIcon = (ImageView) rootView.findViewById(R.id.first_icon);
        ImageView secondIcon = (ImageView) rootView.findViewById(R.id.second_icon);

        RelativeLayout firstContainer = (RelativeLayout) rootView.findViewById(R.id.wiring_first);
        RelativeLayout secondContainer = (RelativeLayout) rootView.findViewById(R.id.wiring_second);

        FloatingActionButton firstAdd = (FloatingActionButton) rootView.findViewById(R.id.first_add);
        FloatingActionButton secondAdd = (FloatingActionButton) rootView.findViewById(R.id.second_add);

        wiringButton = (Button) rootView.findViewById(R.id.wiring_button_all);
        filterButton = (Button) rootView.findViewById(R.id.filter_button_all);
        valueButton = (Button) rootView.findViewById(R.id.value_button_all);

        SparseArray<ComponentService> components = registry.getCurrent(false).getComponents();

        if (components.size() == 0) {
            first = null;
            second = null;
        } else {
            first = position > 0 ? components.get(position - 1) : null;
            second = position < components.size() ? components.get(position) : null;
        }

        setWiringMode(MODE_WIRING);
        wiringMap.set(first, second, wiringMode);

        LocalStorage localStorage = LocalStorage.getInstance();

        // FIXME IT doesn't appear to be very happy when you remove the first one, but I'm not sure that's anything to do with the moving that I've just added in for animation

        // Set the icon of either to be the big purple plus if there's not a component in that position
        if (first != null) {

            firstName.setText(first.getDescription().getName());

            firstName.setVisibility(View.VISIBLE);
            firstIcon.setVisibility(View.VISIBLE);
            firstAdd.setVisibility(View.GONE);

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
            firstName.setVisibility(View.GONE);
            firstIcon.setVisibility(View.GONE);
            firstAdd.setVisibility(View.VISIBLE);
            firstContainer.setBackgroundResource(R.drawable.wiring_add_default);

            OnClickListener firstClick = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityWiring a = (ActivityWiring) getActivity();
                    if(a != null) {
                        a.chooseComponentFromList(true, position);
                    } else {
                        Log.d(TAG, "ActivityWiring is dead for choosing component");
                    }
                }
            };

            // Make it add at this position when we click it
            firstContainer.setOnClickListener(firstClick);
            firstAdd.setOnClickListener(firstClick);
        }

        // Make the right icon be the left half, Make the left icon be the right half
        if (second != null) {
            secondName.setText(second.getDescription().getName());
            secondContainer.setBackgroundResource(R.drawable.wiring_component);

            secondName.setVisibility(View.VISIBLE);
            secondIcon.setVisibility(View.VISIBLE);
            secondAdd.setVisibility(View.GONE);

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
            secondContainer.setBackgroundResource(R.drawable.wiring_add_default);

            secondName.setVisibility(View.GONE);
            secondIcon.setVisibility(View.GONE);
            secondAdd.setVisibility(View.VISIBLE);

            // Make it add at this position when we click it
            OnClickListener secondClick = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityWiring a = (ActivityWiring) getActivity();
                    if(a != null) {
                        a.chooseComponentFromList(false, position);
                    } else {
                        Log.d(TAG, "ActivityWiring is dead for choosing component");
                    }
                }
            };

            secondContainer.setOnClickListener(secondClick);
            secondAdd.setOnClickListener(secondClick);
        }

        if (first == null) {
            wiringButton.setEnabled(false);
            filterButton.setEnabled(false);
            setWiringMode(MODE_VALUE);
        } else if (!first.hasOutputs()) {
            setWiringMode(MODE_VALUE);
        }

        if (second == null) {
            wiringButton.setEnabled(false);
            valueButton.setEnabled(false);
            setWiringMode(MODE_FILTER);
        } else if (!second.hasInputs()) {
            setWiringMode(MODE_FILTER);
        }

        wiringButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setWiringMode(MODE_WIRING);
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWiringMode(MODE_FILTER);
            }
        });

        valueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWiringMode(MODE_VALUE);
            }
        });

        return rootView;
    }

    public void autoConnect() {
        wiringMap.autoConnect();
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

        if (wiringMap != null) {
            wiringMap.setWiringMode(wiringMode);
        }
    }

    public int getWiringMode() {
        return wiringMode;
    }

    public void setWiringMode(int wiringMode) {
        this.wiringMode = wiringMode;

        switch(wiringMode) {
            case MODE_VALUE:
                wiringButton.setSelected(false);
                filterButton.setSelected(false);
                valueButton.setSelected(true);
                break;

            case MODE_FILTER:
                wiringButton.setSelected(false);
                filterButton.setSelected(true);
                valueButton.setSelected(false);
                break;

            default:
                wiringButton.setSelected(true);
                filterButton.setSelected(false);
                valueButton.setSelected(false);
                break;
        }
        redraw();
    }

    public void removeConnections() {
        wiringMap.removeConnections();
    }
}
