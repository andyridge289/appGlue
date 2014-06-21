package com.appglue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;

public class ActivityStoryParameters extends Activity {

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_story_parameters);

        Intent intent = this.getIntent();
        int position = intent.getIntExtra(POSITION, -1);
        Log.w(TAG, "Creating parameters for position " + position);

        FragmentStoryParameters parameterFragment = (FragmentStoryParameters) getFragmentManager().findFragmentById(R.id.story_fragment_parameters);
        parameterFragment.setData(position);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.story_params, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.story_params_ok) {
            // Then go to the page
            Intent intent = new Intent(ActivityStoryParameters.this, ActivityStoryComponents.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.story_params_back) {
            // Might need to remove the component from the component list
            finish();
        }

        return false;
    }
}
