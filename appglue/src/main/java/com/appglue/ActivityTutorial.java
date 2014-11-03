package com.appglue;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appglue.library.AppGlueLibrary;

import java.util.ArrayList;


public class ActivityTutorial extends ActionBarActivity {

    private ArrayList<Fragment> fragments;
    private ArrayList<View> navs;

    private Toolbar toolbar;
    private LinearLayout navContainer;

    private int index;

    private ImageView next;
    private ImageView previous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        fragments = new ArrayList<Fragment>();
        fragments.add(FragmentPrivacy.create());
        for(int i = 0; i < 5; i++) {
            fragments.add(FragmentTutorial.create(i));
        }

        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundResource(R.color.composite);
        toolbar.setTitle("Tutorial & Disclaimer");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        next = (ImageView) findViewById(R.id.tutorial_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index < fragments.size() - 1) {
                    setIndex(index + 1);
                }
            }
        });
        previous = (ImageView) findViewById(R.id.tutorial_previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index > 0) {
                    setIndex(index - 1);
                }
            }
        });

        navs = new ArrayList<View>();
        navContainer = (LinearLayout) findViewById(R.id.nav_container);
        for (int i = 0; i < fragments.size(); i++) {

            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.nav, null);
            int size = (int) AppGlueLibrary.dpToPx(getResources(), 8);
            int margin = (int) AppGlueLibrary.dpToPx(getResources(), 2);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, margin, margin, margin);
            v.setLayoutParams(lp);
            v.setEnabled(false);
            navContainer.addView(v);
            navs.add(v);
        }

        setIndex(0);
    }

    private void setIndex(int index) {

        navs.get(this.index).setEnabled(false);
        navs.get(index).setEnabled(true);

        // Enable/disable the buttons
        if (index > 0) {
            previous.setEnabled(true);
        } else {
            previous.setEnabled(false);
        }

        if (index < fragments.size() - 1) {
            next.setEnabled(true);
        } else {
            next.setEnabled(false);
        }

        this.index = index;

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, fragments.get(index)).commit();
//        .setCustomAnimations(slideIn, slideOut)

        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
