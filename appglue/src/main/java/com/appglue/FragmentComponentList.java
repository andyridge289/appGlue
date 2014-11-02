package com.appglue;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.JUST_A_LIST;

public class FragmentComponentList extends Fragment {
    protected ListView serviceListView;
    protected TextView noneFound;
    protected ImageView loader;

    protected boolean homeParent;
    protected boolean justList;

    protected Registry registry;

    protected String name;

    protected ArrayList<ServiceDescription> services;
    protected ArrayList<ServiceDescription> renderServices;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        homeParent = getActivity() instanceof ActivityAppGlue;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = inflater.inflate(R.layout.fragment_component_list, container, false);

        serviceListView = (ListView) v.findViewById(R.id.simple_list);
        serviceListView.setDivider(null);
        serviceListView.setDividerHeight(0);

        Bundle args = getArguments();
        justList = args.getBoolean(JUST_A_LIST, false);

        loader = (ImageView) v.findViewById(R.id.loading_spinner);
        noneFound = (TextView) v.findViewById(R.id.simple_list_none);

        AnimationDrawable ad = (AnimationDrawable) loader.getBackground();
        ad.start();

        return v;
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        renderServices = new ArrayList<ServiceDescription>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}