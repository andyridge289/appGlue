package com.appglue.services;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.appglue.ComposableService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.appglue.Constants.TAG;

public class LocationService extends ComposableService implements GoogleApiClient.ConnectionCallbacks,
																  GoogleApiClient.OnConnectionFailedListener
{
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COUNTRY_NAME = "country_name";
    public static final String COUNTRY_CODE = "country_code";
	public static final String LOCALITY_NAME = "locality_name";
    public static final String ROAD_NAME = "road_name";
	
	private GoogleApiClient mApiClient;
	
	@Override
	public ArrayList<Bundle> performService(Bundle o)
	{
		mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


		mApiClient.connect();
		
		wait = true;
		
		return null;
	}
	
	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os)
	{
		// This doesn't need to be performed for a list

		return null;
	}
	
	public void onConnected(Bundle bundle)
	{
		Location loc = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
		double latitude = -1;
		double longitude = -1;
		
		if(loc != null)
		{
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
		}
		
		Bundle locationBundle = new Bundle();
		locationBundle.putDouble(LATITUDE, latitude);
        locationBundle.putDouble(LONGITUDE, longitude);

        Geocoder g = new Geocoder(this, Locale.getDefault());
		List<Address> addresses;
		try 
		{
			addresses = g.getFromLocation(latitude, longitude, 1);
			
			if(addresses == null || addresses.size() == 0)
			{
                super.fail("I can't find your address, try again later");
			    mApiClient.disconnect();
				super.send  (null);
				return;
			}
			
			Address address = addresses.get(0);
			locationBundle.putString(COUNTRY_NAME, address.getCountryName());
			locationBundle.putString(LOCALITY_NAME, address.getLocality());
            locationBundle.putString(COUNTRY_CODE, address.getCountryCode());
            locationBundle.putString(ROAD_NAME, address.getFeatureName());
		}
		catch (IOException e) 
		{
			locationBundle.putString(COUNTRY_NAME, "Unknown (" + latitude + ", " + longitude + ")");
			locationBundle.putString(LOCALITY_NAME, "Unknown");
            locationBundle.putString(COUNTRY_CODE, "Unknown");
            locationBundle.putString(ROAD_NAME, "Unknown");
		}
		
		mApiClient.disconnect();
		super.send(locationBundle);
    }

	@Override
	public void onConnectionFailed(ConnectionResult arg0)
	{
        super.fail("I can't find my location, maybe GPS is off?");
		super.send(null);
	}

    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection suspended");
    }

}
