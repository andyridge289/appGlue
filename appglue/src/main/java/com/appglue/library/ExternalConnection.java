package com.appglue.library;

import android.os.AsyncTask;

import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

public class ExternalConnection 
{
	private static ExternalConnection external = null;
	
	private ArrayList<ServiceDescription> externalServices;
	
	private ExternalConnection()
	{
		externalServices = new ArrayList<ServiceDescription>();
	}
	
	public ArrayList<ServiceDescription> getExternalServices()
	{
		return externalServices;
	}

	public class Async extends AsyncTask<String, Void, ArrayList<ServiceDescription>>
	{
		private final int TYPE_SEARCH = 0;
		private final int TYPE_LOOKUP = 1;
		
//		private String keyword;
//		private ServiceDescription description;
//		private ActivityComponentListOld list;
//		private int type;
//		
//		public Async(String keyword, ActivityComponentListOld list)
//		{
////			this.keyword = keyword;
//			this.list = list;
//			this.type = TYPE_SEARCH;
//		}
		
		public Async(ServiceDescription description)
		{
//	/		this.description = description;
//			this.keyword = description.getClassName();
//			this.type = TYPE_LOOKUP;
		}

		@Override
		protected ArrayList<ServiceDescription> doInBackground(String... params) 
		{
//			if(this.type == TYPE_SEARCH)
//				return serviceSearch();
//			else if(this.type == TYPE_LOOKUP)
//				return serviceLookup();
//			else
//				return null;
//		}
		
//		private ArrayList<ServiceDescription> serviceLookup()
//		{
//			ArrayList<ServiceDescription> returnedServices = new ArrayList<ServiceDescription>();
//			String serviceText = "";
//			
//			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
//			postData.add(new BasicNameValuePair(CLASSNAME, keyword));
//			
//			try
//			{
//				serviceText = Network.httpPost(URL_GET_SERVICE, postData);
//			}
//			catch(ClientProtocolException e)
//			{
//			}
//			catch (IOException e) 
//			{
//			}
//
//			try 
//			{
//				JSONObject json = new JSONObject(serviceText);
//				
//				if(json.has(JSON_ERROR))
//				{
//					JSONObject error = json.getJSONObject(JSON_ERROR);
//					
//					// Then it's an error, or there are no results so don't bother
//					int errno = error.getInt(JSON_ERRNO);
//					
//					if(errno == ERR_CLASSNAME)
//						return returnedServices; // The class name wasn't set, this is just a bit of a cock up
//					else if(errno == ERR_NOSERVICE)
//						addService();
//						
//				}
//				else if(!json.has(ID))
//				{
//					// This shouldn't happen anymore?
//					return returnedServices;
//				}
//				
//				returnedServices.addAll(ServiceDescription.parseRemoteJSON(serviceText));
//			}
//			catch (JSONException e) 
//			{
//			}
//			
//			return returnedServices;
			return new ArrayList<ServiceDescription>();
		}
		
//		private void addService()
//		{
//			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
//			
//			postData.add(new BasicNameValuePair(CLASSNAME, description.getClassName()));
//			postData.add(new BasicNameValuePair(PACKAGENAME, description.getPackageName()));
//			postData.add(new BasicNameValuePair(NAME, description.getName()));
//			
//			postData.add(new BasicNameValuePair(DESCRIPTION, description.getDescription()));
//			postData.add(new BasicNameValuePair(DEVELOPER, description.getDeveloper()));
//			postData.add(new BasicNameValuePair(PROCESS_TYPE, "" + description.getProcessType().index));
//			
//			if(description.hasInput())
//			{
//				ServiceIO input = description.getInput();
//				postData.add(new BasicNameValuePair(INPUT_NAME, input.getName()));
//				postData.add(new BasicNameValuePair(INPUT_DESCRIPTION, input.getDescription()));
//				postData.add(new BasicNameValuePair(INPUT_TYPE, input.getType().getName()));
//			}
//			else
//			{
//				postData.add(new BasicNameValuePair(INPUT_NAME, "Void"));
//				postData.add(new BasicNameValuePair(INPUT_DESCRIPTION, "None"));
//				postData.add(new BasicNameValuePair(INPUT_TYPE, "void"));
//			}
//			
//			if(description.hasOutput())
//			{
//				ServiceIO output = description.getOutput();
//				postData.add(new BasicNameValuePair(OUTPUT_NAME, output.getName()));
//				postData.add(new BasicNameValuePair(OUTPUT_DESCRIPTION, output.getDescription()));
//				postData.add(new BasicNameValuePair(OUTPUT_TYPE, output.getType().getName()));
//			}
//			else
//			{
//				postData.add(new BasicNameValuePair(OUTPUT_NAME, "Void"));
//				postData.add(new BasicNameValuePair(OUTPUT_DESCRIPTION, "None"));
//				postData.add(new BasicNameValuePair(OUTPUT_TYPE, "void"));
//			}
//			
//			try 
//			{
//				String result = Network.httpPost(URL_ADD_SERVICE, postData);
//				
//				JSONObject json = new JSONObject(result);
//				
//				if(json.has(JSON_SUCCESS))
//				{
//					// Then it has worked
//				}
//				else if(json.has(JSON_ERROR))
//				{
//					// Then the insert failed, find out why
//					Network.parseError(json.getJSONObject(JSON_ERROR));
//				}
//			}
//			catch (ClientProtocolException e) 
//			{
//				e.printStackTrace();
//			}
//			catch (IOException e) 
//			{
//				e.printStackTrace();
//			}
//			catch (JSONException e) 
//			{
//				e.printStackTrace();
//			}
//		}
		
		/*private ArrayList<ServiceDescription> serviceSearch()
		{
			ArrayList<ServiceDescription> returnedServices = new ArrayList<ServiceDescription>();
			String serviceText = "";
			
			try 
			{
				serviceText = Network.httpGet(URL_GET_SERVICES + "/?q=" + keyword);
			}
			catch (ClientProtocolException e) 
			{
			}
			catch (IOException e) 
			{
			}
			
			try 
			{
				returnedServices.addAll(ServiceDescription.parseRemoteJSON(serviceText));
			}
			catch (JSONException e) 
			{
			}
			
			externalServices = returnedServices;
			return returnedServices;
		}*/
		
		@Override
		protected void onPostExecute(ArrayList<ServiceDescription> services)
		{	
//			Registry registry = Registry.getInstance(null);
			
//			if(this.type == TYPE_SEARCH)
//			{
//				list.setRemoteServices(services);
//				registry.addRemotes(services);
//			}
//			else if(this.type == TYPE_LOOKUP)
//			{
//				if(services.size() > 1)
//				{
//					// Then something has gone awry
//					return;
//				}
//				else if(services.size() == 0)
//				{
//					// There are no results, this actually shouldn't happen, but I haven't put the new services in the database yet
//					return;
//				}
//				
//				ServiceDescription service = services.get(0);
//				
//				registry.updateServiceFromLookup(service);
//			}
		}
	}
	
//	public void getExternalServices(String keyword, ActivitySimpleServiceList list)
//	{
//		AsyncTask<String, Void, ArrayList<ServiceDescription>> async = new Async(keyword, list);
//		async.execute();
//	}
//	
//	public void getExternalService(ServiceDescription description)
//	{
//		AsyncTask<String, Void, ArrayList<ServiceDescription>> async = new Async(description);
//		async.execute();
//	}

	
	public static ExternalConnection getInstance()
	{
		if(external == null)
		{
			external = new ExternalConnection();
		}
		
		return external;
	}
}
