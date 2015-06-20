package com.appglue.layout;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appglue.AppGlueFragment;
import com.appglue.MainActivity;
import com.appglue.R;
import com.appglue.WiringActivity;
import com.appglue.engine.model.CompositeService;
import com.appglue.layout.adapter.CompositeListAdapter;
import com.appglue.layout.view.FloatingActionButton;
import com.appglue.library.Assert;
import com.appglue.serviceregistry.Registry;
import com.appglue.serviceregistry.RegistryService;
import com.appglue.services.factory.ServiceFactory;
import com.orhanobut.logger.Logger;

import org.json.JSONException;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;

public class FragmentCompositeList extends Fragment implements AppGlueFragment {

    private RecyclerView mCompositeList;
    private CompositeListAdapter mListAdapter;

    private ImageView loader;
    private View noComposites;
    private FloatingActionButton addFab;
    private LinearLayout contextToolbar;

    private Registry registry;

    private View run;
    private View schedule;
    private View shortcut;

    private ArrayList<CompositeService> mComposites;

    public static Fragment create() {
        return new FragmentCompositeList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());
        mComposites = new ArrayList<CompositeService>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_composite_list, container, false);

        noComposites = root.findViewById(R.id.no_composites);
        contextToolbar = (LinearLayout) root.findViewById(R.id.context_toolbar);
        contextToolbar.setVisibility(View.GONE);

        mCompositeList = (RecyclerView) root.findViewById(R.id.composite_list);
        mCompositeList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListAdapter = new CompositeListAdapter(this, mComposites);
        mCompositeList.setAdapter(mListAdapter);

        addFab = (FloatingActionButton) root.findViewById(R.id.fab_add);
        if (addFab != null) {
            addFab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WiringActivity.class);
                    intent.putExtra(EDIT_EXISTING, false);
                    intent.putExtra(CREATE_NEW, true);
                    startActivity(intent);
                }
            });
        }

        final MainActivity aag = ((MainActivity) getActivity());

        run = root.findViewById(R.id.composite_run);
        run.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                aag.run(mListAdapter.getCurrentComposite());
                hideToolbar();
            }
        });

        schedule = root.findViewById(R.id.composite_schedule);
        schedule.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                aag.schedule(mListAdapter.getCurrentComposite());
                hideToolbar();
            }
        });

        View edit = root.findViewById(R.id.composite_edit);
        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                aag.edit(mListAdapter.getCurrentComposite());
                hideToolbar();
            }
        });

        shortcut = root.findViewById(R.id.composite_shortcut);
        shortcut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                aag.createShortcut(mListAdapter.getCurrentComposite());
                hideToolbar();
            }
        });

        View delete = root.findViewById(R.id.composite_delete);
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                final CompositeService cs = mListAdapter.getCurrentComposite();
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete")
//                        .setMessage(String.format("Are you sure you want to delete %s?", cs.getName()))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                if (registry.delete(cs)) {
//                                    Toast.makeText(getActivity(), String.format("\"%s\" deleted successfully", cs.getName()), Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(getActivity(), String.format("Failed to delete \"%s\"", cs.getName()), Toast.LENGTH_SHORT).show();
//                                }
//
//                                mListAdapter.remove(cs);
//                                mListAdapter.notifyDataSetChanged();
                                hideToolbar();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hideToolbar();
                            }
                        }).show();
            }
        });

        loader = (ImageView) root.findViewById(R.id.loading_spinner);

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
        BackgroundCompositeLoader bcl = new BackgroundCompositeLoader(this);
        bcl.execute();
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
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

    public void redraw() {
        if (registry == null) {
            return;
        }

        mComposites = registry.getComposites();

        addFab.hide(false);
        contextToolbar.setVisibility(View.GONE);

        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
//            mListAdapter.setSelectedIndex(-1);
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public String onCreateOptionsMenu(Menu menu) {
        return "appGlue";
    }

    public CompositeService getComposite(int position) {
        if (Assert.exists(mComposites, position)) {
            return null;
        }

        return mComposites.get(position);
    }


    public int showToolbar(int selectedIndex, int position, CompositeService composite) {
        if (selectedIndex == -1) {
            addFab.hide(true);
            selectedIndex = position;
            contextToolbar.setVisibility(View.VISIBLE);
            int color = composite.getColour(true);
            contextToolbar.setBackgroundResource(color);
        } else if (selectedIndex == position) {
            addFab.hide(false);
            contextToolbar.setVisibility(View.GONE);
            selectedIndex = -1;
        } else {
            selectedIndex = position;
            contextToolbar.setBackgroundResource(composite.getColour(true));
        }

        if (composite.containsTrigger()) {
            schedule.setVisibility(View.GONE);
            shortcut.setVisibility(View.GONE);
            run.setVisibility(View.GONE);
        } else {
            schedule.setVisibility(View.VISIBLE);
            shortcut.setVisibility(View.VISIBLE);
            run.setVisibility(View.VISIBLE);
        }

        return selectedIndex;
    }

    private void hideToolbar() {
        contextToolbar.setVisibility(View.GONE);
        addFab.hide(false);
//        mListAdapter.setSelectedIndex(-1);
        mListAdapter.notifyDataSetChanged();
    }

    public static class BackgroundCompositeLoader extends AsyncTask<Void, CompositeService, Boolean> {

        private FragmentCompositeList mFragment;

        public BackgroundCompositeLoader(FragmentCompositeList fragment) {
            mFragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            Activity activity = mFragment.getActivity();
            Registry registry = Registry.getInstance(activity);

            try {

                ServiceFactory sf = ServiceFactory.getInstance(registry, activity);
                sf.setupServices();

            } catch (JSONException e) {
                Logger.e("JSONException - Failed to create services (CompositeListActivity) " + e.getMessage());
            }

            if (activity == null) {
                return false;
            }

            ActivityManager manager = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);

            if (manager != null) {
                boolean registryRunning = false;

                for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (RegistryService.class.getCanonicalName().equals(service.service.getClassName())) {
                        registryRunning = true;
                    }
                }

                if (!registryRunning) {
                    Intent registryIntent = new Intent(activity, RegistryService.class);
                    activity.startService(registryIntent);
                }
            }

            ArrayList<Long> ids = registry.getCompositeIds();

            for (long id :ids) {
                CompositeService composite = registry.getComposite(id);
                publishProgress(composite);
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(CompositeService... composites) {

            CompositeService composite = composites[0];
            if (composite != null) {
                return;
            }

            mFragment.mComposites.add(composite);
            mFragment.mListAdapter.notifyDataSetChanged();
            mFragment.loader.setVisibility(View.GONE);

//            if (composites.size() > 0) {
//                mFragment.mCompositeList.setVisibility(View.VISIBLE);
//                mFragment.noComposites.setVisibility(View.GONE);
//            } else {
//                mFragment.noComposites.setVisibility(View.VISIBLE);
//                mFragment.mCompositeList.setVisibility(View.GONE);
//            }
        }

        // TODO Sort out the showing and removing of the plus button
        // TODO sort out what happens if they close the dialog when they shouldn't, or stop them from doing this

        protected void onPostExecute(Boolean success) {
//            for (int i = 0; i < composites.size(); i++) {
//                Logger.d("Adding composite " + composites.get(i).getName());
//                mFragment.mComposites.add(composites.get(i));
//            }
//            if (success) {
//                Toast.makeText()
//            }
        }


    }
}