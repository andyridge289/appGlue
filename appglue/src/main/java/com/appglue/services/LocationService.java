package com.appglue.services;

import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.appglue.ComposableService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesClient;
//import com.google.android.gms.location.LocationClient;

public class LocationService extends ComposableService //implements GooglePlayServicesClient.ConnectionCallbacks,
														//		  GooglePlayServicesClient.OnConnectionFailedListener
{
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COUNTRY_NAME = "country_name";
	public static final String REGION_NAME = "region_name";
	public static final String LOCALITY_NAME = "locality_name";
	
//	private LocationClient lc;
	
	@Override
	public ArrayList<Bundle> performService(Bundle o, ArrayList<Bundle> parameters) 
	{
//		lc = new LocationClient(this, this, this);
//		lc.connect();
		
		wait = true;
		
		return null;
	}
	
	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) 
	{
		// This doesn't need to be performed for a list

		return null;
	}
	
	public void onConnected(Bundle bundle)
	{
//		Location loc = lc.getLastLocation();
		double latitude = -1;
		double longitude = -1;
		
//		if(loc != null)
//		{
//			latitude = loc.getLatitude();
//			longitude = loc.getLongitude();
//		}
		
		Bundle locationBundle = new Bundle();
		locationBundle.putDouble(LATITUDE, latitude);
		locationBundle.putDouble(LONGITUDE, longitude);	
		
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) 
		{
		    Toast.makeText(this, "Wifi is connected, this sometimes doens't work for looking up Lat long. You can ask google why", Toast.LENGTH_LONG).show();
		}
		
		Geocoder g = new Geocoder(this, Locale.getDefault());
		List<Address> addresses;
		try 
		{
			addresses = g.getFromLocation(latitude, longitude, 1);
			
			if(addresses == null || addresses.size() == 0)
			{
				fail = true;
				error = "I can't find your address, try again later";
//				lc.disconnect();
//				super.send  (null);
				return;
			}
			
			Address address = addresses.get(0);
			locationBundle.putString(COUNTRY_NAME, address.getCountryName());
			locationBundle.putString(REGION_NAME, address.getAdminArea());
//			locationBundle.putString(LOCALITY_NAME, address.getLocality());
		}
		catch (IOException e) 
		{
//			locationBundle.putString(COUNTRY_NAME, "Dno (" + latitude + ", " + longitude + ")");
//			locationBundle.putString(REGION_NAME, "Dno");
//			locationBundle.putString(LOCALITY_NAME, "Dno");
		}
		
		Bundle b = new Bundle();
		b.putBundle(INPUT, locationBundle);
		
//		lc.disconnect();
		
//		super.send  (b);
    }

//	@Override
//	public void onDisconnected() 
//	{
		// Shouldn't need to do anything
//	}

//	@Override
//	public void onConnectionFailed(ConnectionResult arg0) 
//	{
//		fail = true;
//		error = "I can't find my location, maybe GPS is off?";
//		super.send  (null);
//	}

}
