package com.appglue;


import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.ServiceType;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;


public class FragmentComponentListSearch extends FragmentComponentList {

    public FragmentComponentListSearch() {
        super();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = inflater.inflate(R.layout.fragment_component_list_search, container, false);

        serviceListView = (ListView) v.findViewById(R.id.simple_list);
        serviceListView.setDivider(null);
        serviceListView.setDividerHeight(0);

        loader = (ImageView) v.findViewById(R.id.loading_spinner);
        noneFound = (TextView) v.findViewById(R.id.simple_list_none);

        AnimationDrawable ad = (AnimationDrawable) loader.getBackground();
        ad.start();

        ((TextView) v.findViewById(R.id.simple_list_none)).setText("No components on this device! (You shouldn't be seeing this.... What have you done!?)");

        registry = Registry.getInstance(parent);

        ComponentLoaderTask bl = new ComponentLoaderTask();
        bl.execute();

        serviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
                Intent intent = new Intent(parent, ActivityComponent.class);
                intent.putExtra(SERVICE_TYPE, ServiceType.DEVICE.index);
                intent.putExtra(CLASSNAME, services.get(position).className());
                intent.putExtra(JUST_A_LIST, parent.justAList());
                parent.startActivityForResult(intent, SERVICE_REQUEST);
                return true;
            }
        });

        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                parent.chosenItem(services.get(position).className());
            }
        });

        // Setup the search bar
        EditText searchEdit = (EditText) v.findViewById(R.id.component_search);
        searchEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText et = (EditText) v;
                if (hasFocus) {
                    if (et.getText().toString().equals("Search")) {
                        et.setText("");
                    }
                } else {
                    if (et.getText().toString().equals("")) {
                        et.setText("Search");
                    }
                }
            }
        });

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                AdapterComponentList localAdapter = (AdapterComponentList) serviceListView.getAdapter();
                if (localAdapter != null) {
                    localAdapter.getFilter().filter(s);
                    localAdapter.notifyDataSetChanged();
                } else {
                    if (LOG) Log.d(TAG, "The local adapter is null.");
                    // Do we just need to create a new one?
                }
            }
        });

        return v;
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }

    private class ComponentLoaderTask extends AsyncTask<Void, Void, ArrayList<ServiceDescription>> {

        @Override
        protected ArrayList<ServiceDescription> doInBackground(Void... params) {
            return registry.getComponents();
        }

        @Override
        protected void onPostExecute(ArrayList<ServiceDescription> components) {
            // Need to set the components to be on this and get rid of the loading spinner
            services = components;

            loader.setVisibility(View.GONE);

            if (services.size() > 0) {
                serviceListView.setVisibility(View.VISIBLE);
                AdapterComponentList adapter = new AdapterComponentListSearch(parent, services,
                        FragmentComponentListSearch.this);
                serviceListView.setAdapter(adapter);
            } else
                noneFound.setVisibility(View.VISIBLE);

        }

    }
}