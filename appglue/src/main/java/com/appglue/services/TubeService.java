package com.appglue.services;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.appglue.ComposableService;
import com.appglue.library.Network;

import static com.appglue.Constants.*;

public class TubeService extends ComposableService
{	
	public static final String TAG_LINES = "lines";
	public static final String TAG_UPDATE = "update";
	public static final String TAG_NAME = "name";
	public static final String TAG_STATUS = "status";
	public static final String TAG_MESSAGES = "messages";
	
	public static final String GOOD_SERVICE = "good service";
	public static final String MINOR_DELAYS = "minor delays";
	public static final String SEVERE_DELAYS = "severe delays";
	public static final String PART_CLOSURE = "part closure";
	
	public static final String LINE_NAME = "line_name";
	public static final String LINE_STATUS = "line_status";
	public static final String LINE_MESSAGE = "line_message";
	public static final String LINE_URL = "line_url";
	
	private final String url = "http://people.bath.ac.uk/ar289/services/tube/tube_status.php";
	
	private String getFromURL(String url, ArrayList<Bundle> parameters) throws ClientProtocolException, IOException
	{		
		if(parameters == null)
		{
			return Network.httpGet(url);
		}
		else
		{			
			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		
			for(int i = 0; i < parameters.size(); i++)
			{
				Bundle b = parameters.get(i);
				String name = b.getString(NAME);
				String[] values = b.getStringArray(VALUE);
				String value = "";
				for(int j = 0; j < values.length; j++)
				{
					value += values[j];
				}
				
				postData.add(new BasicNameValuePair(name, value));
			}
			
			String postReturn = Network.httpPost(url, postData);
			return postReturn;
		}
	}
	
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters)
	{
		String output = "";
		boolean fail = false;
		
		try
		{
			output = getFromURL(url, parameters);
		}
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
			fail = true;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			fail = true;
		}
		
		if(fail)
		{
			ArrayList<Bundle> deadLines = new ArrayList<Bundle>();
			deadLines.add(this.makeBundle(-1, "No network!", "failure", null, ""));
			
			isList = true;
			
			return deadLines;
		}
		
		return processOutput(output, parameters);
	}

	public ArrayList<Bundle> processOutput(String s, ArrayList<Bundle> parameters) 
	{
		try 
		{
			JSONObject json = new JSONObject(s);
			JSONArray lines = json.getJSONArray(TAG_LINES);
			ArrayList<Bundle> deadLines = new ArrayList<Bundle>();
			
			for(int i = 0; i < lines.length(); i++)
			{
				JSONObject jsonLine = lines.getJSONObject(i);
				
				int lineNum = i;
				String lineName = jsonLine.getString(TAG_NAME);
				String status = jsonLine.getString(TAG_STATUS);
				
				JSONArray jsonMessages = jsonLine.getJSONArray(TAG_MESSAGES);
				
				String[] messages = new String[jsonMessages.length()];
				for(int j = 0; j < jsonMessages.length(); j++)
				{
					messages[j] = jsonMessages.getString(j);
				}
				
				Bundle lineBundle = this.makeBundle(lineNum, lineName, status, messages, "");
				
				if(!status.equals(GOOD_SERVICE))
					deadLines.add(lineBundle);
			}
			
			if(deadLines.size() == 0)
				deadLines.add(this.makeBundle(-1, "", "", null, ""));
			
			isList = true;
			
			return deadLines;
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private Bundle makeBundle(int num, String name, String status, String[] messages, String url) 
	{
		Bundle b = new Bundle();
		
		b.putString(LINE_NAME, name);
		b.putString(LINE_STATUS, status);
		b.putStringArray(LINE_MESSAGE, messages);
		b.putString(LINE_URL, url);
		
		return b;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) 
	{
		if(os.size() > 0)	
			return performService(os.get(0), parameters);
		else
			return null;
	}

}
