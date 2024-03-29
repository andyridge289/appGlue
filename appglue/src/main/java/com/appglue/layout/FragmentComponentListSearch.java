package com.appglue.layout;


import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.R;
import com.appglue.WiringActivity;
import com.appglue.description.ServiceDescription;
import com.appglue.layout.adapter.AdapterComponentList;
import com.appglue.layout.adapter.AdapterComponentListSearch;
import com.appglue.serviceregistry.Registry;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

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

        registry = Registry.getInstance(getActivity());

        ComponentLoaderTask bl = new ComponentLoaderTask();
        bl.execute();

        serviceListView.setOnItemClickListener((adapterView, v1, index, id) -> {
            if (!homeParent)
                ((WiringActivity) getActivity()).chooseItem(services.get(index).getClassName());
        });

        // Setup the search bar
        EditText searchEdit = (EditText) v.findViewById(R.id.component_search);
        searchEdit.setOnFocusChangeListener((v1, hasFocus) -> {
            EditText et = (EditText) v1;
            if (hasFocus) {
                if (et.getText().toString().equals("Search")) {
                    et.setText("");
                }
            } else {
                if (et.getText().toString().equals("")) {
                    et.setText("Search");
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
                   Logger.d("The local adapter is null.");
                    // Do we just need to create a new one?
                }
            }
        });

        return v;
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

//        serviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
//                Intent intent = new Intent(getActivity(), ActivityComponent.class);
//                intent.putExtra(SERVICE_TYPE, ServiceType.DEVICE.index);
//                intent.putExtra(CLASSNAME, services.get(position).getClassName());
//                intent.putExtra(JUST_A_LIST, justList);
//                getActivity().startActivityForResult(intent, SERVICE_REQUEST);
//                return true;
//            }
//        });
    }

    private class ComponentLoaderTask extends AsyncTask<Void, Void, ArrayList<ServiceDescription>> {

        @Override
        protected ArrayList<ServiceDescription> doInBackground(Void... params) {
            return registry.getAllServiceDescriptions();
        }

        @Override
        protected void onPostExecute(ArrayList<ServiceDescription> components) {
            // Need to set the components to be on this and get rid of the loading spinner
            services = components;

            loader.setVisibility(View.GONE);

            if (services.size() > 0) {
                if (getActivity() != null) {
                    serviceListView.setVisibility(View.VISIBLE);
                    AdapterComponentList adapter = new AdapterComponentListSearch(getParentFragment().getActivity(), services,
                            (FragmentComponentListPager) getParentFragment());
                    serviceListView.setAdapter(adapter);
                }
            } else
                noneFound.setVisibility(View.VISIBLE);

        }

    }
}