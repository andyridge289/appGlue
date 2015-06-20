package com.appglue;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appglue.layout.FragmentPrivacy;
import com.appglue.layout.FragmentTutorial;
import com.appglue.library.AppGlueLibrary;

import java.util.ArrayList;


public class TutorialActivity extends AppGlueActivity implements ViewPager.OnPageChangeListener {

    private ArrayList<Fragment> fragments;
    private ArrayList<View> navs;

    private int index;

    private ImageView next;
    private ImageView previous;

    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        fragments = new ArrayList<>();
        fragments.add(FragmentPrivacy.create());
        for(int i = 0; i < 6; i++) {
            fragments.add(FragmentTutorial.create(i));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundResource(R.color.hex444);
        toolbar.setTitle("Tutorial & Disclaimer");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        next = (ImageView) findViewById(R.id.tutorial_next);
        next.setOnClickListener(v -> {
            if (index < fragments.size() - 1) {
                setIndex(index + 1);
            }
        });
        previous = (ImageView) findViewById(R.id.tutorial_previous);
        previous.setOnClickListener(v -> {
            if (index > 0) {
                setIndex(index - 1);
            }
        });

        navs = new ArrayList<>();
        LinearLayout navContainer = (LinearLayout) findViewById(R.id.nav_container);
        //noinspection UnusedDeclaration
        for (Fragment fragment : fragments) {

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

        pager = (ViewPager) findViewById(R.id.tutorial_pager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        pager.setOnPageChangeListener(this);

        setIndex(0);
        onPageSelected(0);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int index) {

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
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void setIndex(int index) {

        pager.setCurrentItem(index);
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

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<Fragment> fragments;

        public PagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragments) {
            super(fragmentManager);

            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
