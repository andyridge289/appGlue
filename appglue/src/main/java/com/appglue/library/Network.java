package com.appglue.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import static com.appglue.Constants.*;

public class Network 
{
	public static String httpPost(String url, ArrayList<NameValuePair> postData) throws ClientProtocolException, IOException 
	{
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);

        // Add your data
        httppost.setEntity(new UrlEncodedFormEntity(postData));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);
        return getFromResponse(response);
	}
	
	public static String httpGet(String url) throws ClientProtocolException, IOException
	{
		DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        return getFromResponse(response);
	}
	
	public static void parseError(JSONObject error) throws JSONException
	{
		int errno = error.getInt(JSON_ERRNO);
		String errmsg = error.getString(JSON_ERRMSG);
		
		Log.e(TAG, String.format("Network Fail :: %s :: %s", "" + errno, errmsg));
	}
	
	private static String getFromResponse(HttpResponse response) throws IllegalStateException, IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer sb = new StringBuffer("");
        String line = "";
        String NL = System.getProperty("line.separator");
        while ((line = in.readLine()) != null) 
        {
            sb.append(line + NL);
        }
        in.close();
        return sb.toString();
	}
}
