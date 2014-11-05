package com.appglue;


import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.description.Category;
import com.appglue.description.ServiceDescription;
import com.appglue.library.Tuple;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.Collections;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;


public class FragmentComponentListCategory extends FragmentComponentList {
    
    int[] CAT_COLOURS = new int[] {
            R.color.material_indigo,
            R.color.material_blue,
            R.color.material_lightblue,
            R.color.material_cyan,
            R.color.material_cyan200,
            R.color.material_lightblue200,
            R.color.material_blue200,
            R.color.material_indigo200
    };

    private GridView categoryList;

    public FragmentComponentListCategory() {
        super();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = inflater.inflate(R.layout.fragment_component_list_category, container, false);

        serviceListView = (ListView) v.findViewById(R.id.component_list);
        serviceListView.setDivider(null);
        serviceListView.setDividerHeight(0);

        categoryList = (GridView) v.findViewById(R.id.category_list);

        loader = (ImageView) v.findViewById(R.id.loading_spinner);
        noneFound = (TextView) v.findViewById(R.id.simple_list_none);

        AnimationDrawable ad = (AnimationDrawable) loader.getBackground();
        ad.start();

        ((TextView) v.findViewById(R.id.simple_list_none)).setText("No components on this device! (You shouldn't be seeing this.... What have you done!?)");

        registry = Registry.getInstance(getActivity());

        ComponentLoaderTask bl = new ComponentLoaderTask();
        bl.execute();

        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int index, long id) {
                if (!homeParent)
                    ((ActivityWiring) getActivity()).chooseItem(services.get(index).getClassName());
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

    private class CategoryAdapter extends ArrayAdapter<Category> {

        private TST<ArrayList<ServiceDescription>> tst;

        public CategoryAdapter(Context context, ArrayList<Category> items, TST<ArrayList<ServiceDescription>> tst) {
            super(context, R.layout.list_item_category, items);
            this.tst = tst;
        }

        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(R.layout.list_item_category, null);
            }

            final Category item = getItem(position);

            v.findViewById(R.id.category_swatch).setBackgroundResource(CAT_COLOURS[position % CAT_COLOURS.length]);

            TextView catName = (TextView) v.findViewById(R.id.category_name);
            catName.setText(item.getName());

            TextView catCount = (TextView) v.findViewById(R.id.category_count);
            catCount.setText("" + item.count);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryList.setVisibility(View.GONE);
                    serviceListView.setAdapter(new AdapterComponentList(getContext(), tst.get(item.getName()), (FragmentComponentListPager) getParentFragment()));
                    serviceListView.setVisibility(View.VISIBLE);
                }
            });

            return v;
        }
    }

    public boolean onBackPressed() {
        if (serviceListView.getVisibility() == View.VISIBLE) {
            categoryList.setVisibility(View.VISIBLE);
            serviceListView.setVisibility(View.GONE);
            return true;
        }

        return false;
    }

    private class ComponentLoaderTask extends AsyncTask<Void, Void, Tuple<ArrayList<Category> ,TST<ArrayList<ServiceDescription>>>> {

        @Override
        protected Tuple<ArrayList<Category> ,TST<ArrayList<ServiceDescription>>> doInBackground(Void... params) {

            ArrayList<Category> cats = registry.getCategories();
            TST<ArrayList<ServiceDescription>> tst = registry.getSDsAcrossCategories();

            for (Category cat : cats) {
                if (tst.get(cat.getName()) != null) {
                    cat.count = tst.get(cat.getName()).size();
                }
            }

            Collections.sort(cats);

            return new Tuple<ArrayList<Category> ,TST<ArrayList<ServiceDescription>>>(cats, tst);
        }

        @Override
        protected void onPostExecute(Tuple<ArrayList<Category> ,TST<ArrayList<ServiceDescription>>> tuple) {

            loader.setVisibility(View.GONE);

            categoryList.setAdapter(new CategoryAdapter(getActivity(), tuple.a, tuple.b));
            categoryList.setVisibility(View.VISIBLE);
        }

    }
}