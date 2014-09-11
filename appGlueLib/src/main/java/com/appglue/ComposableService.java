package com.appglue;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.appglue.Constants.ProcessType;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;

public abstract class ComposableService extends Service 
{		
	// Register and unregister a single service
	public static final int MSG_REGISTER = 0;
	public static final int MSG_UNREGISTER = 1;
	
	public static final int MSG_NOTHING = 2;
	public static final int MSG_OBJECT = 3;
	public static final int MSG_LIST = 4;
	public static final int MSG_WAIT = 5;
	public static final int MSG_FAIL = 6;

	public static final String INPUT = "input";
	public static final String TEXT = "text";
	public static final String WAIT = "wait";
	public static final String DESCRIPTION = "description";
	public static final String ERROR = "error";
	
	protected String toastMessage = "";
	
    private Messenger messageReceiver;
    private Messenger messageSender;
    
    protected boolean isList = false;
    protected boolean wait = false;

    private boolean fail = false;
    private Bundle failureBundle = null;

    private static final boolean LOG = false;
    
    public static ProcessType processType = ProcessType.NORMAL;

    @Override
    public void onCreate() 
    {
    	if(LOG) Log.d(TAG, "Service Created " + this.getClass().getCanonicalName());
    	messageReceiver = new Messenger(new IncomingHandler(this));
    }

    @Override
    public void onDestroy() 
    {
    	if(LOG) Log.d(TAG, "Service destroyed " + this.getClass().getCanonicalName());
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) 
    {
    	if(LOG) Log.d(TAG, "Service bound " + this.getClass().getCanonicalName());
        return messageReceiver.getBinder();
    }
    
    // Both of these always return a list, which we then package up into a bundle and send back to the orchestrator
    public abstract ArrayList<Bundle> performService(Bundle o, ArrayList<Bundle> parameters);
    public abstract ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters);

    protected void fail(String message)
    {
        this.fail = true;
        failureBundle = new Bundle();
        failureBundle.putString(ERROR, message);
    }

    public void send(Bundle b)
    {
        Message returnMessage = isList ? Message.obtain(null, MSG_LIST, 0, 0) : Message.obtain(null, MSG_OBJECT, 0, 0);
        Bundle newMessageData = new Bundle();
        ArrayList<Bundle> o = new ArrayList<Bundle>();
        o.add(b);
        newMessageData.putParcelableArrayList(INPUT, o);
        returnMessage.setData(newMessageData);

        try {
            messageSender.send(returnMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to send data back");
        }
    }

    class Async extends AsyncTask<Message, Void, Message>
    {    	
    	private final int messageType;
    	private final Bundle messageData;
    	
    	public Async(Message message)
    	{
    		if(LOG) Log.d(TAG, "Hello Async");
    		messageType = message.what;
    		messageData = message.getData();
    	}
    	
		@Override
		protected Message doInBackground(Message... param) 
		{
			ArrayList<Bundle> o = null;
			
			if(LOG) Log.d(TAG, String.format("Received: %s", Library.printBundle(messageData)));
			
			
            switch (messageType) 
            {	
                case MSG_NOTHING:
                {
                	// Call the thing, return whatever it gives back
                	// There's no input, so we don't need to get that
                	
                	// Get the parameters
//                	Bundle paramBundle = messageData.getBundle(PARAMS);
                	ArrayList<Bundle> params = new ArrayList<Bundle>(); //paramBundle.getParcelableArrayList(PARAMS);
                	o = performService(null, params);
                	break;
                }
                	
                case MSG_OBJECT:
                {
                	// They have sent a single object, process it
                	
                	// Get the inputs
                	ArrayList<Bundle> inputs = messageData.getParcelableArrayList(INPUT);
                	Bundle input = inputs == null || inputs.size() == 0 ? new Bundle() : inputs.get(0); // Either get the first thing, or if there aren't any things, just make a new thing so that shit doesn't go down
                	
                	// It should only be the first one that is set if only one thing has been sent
                	// Get the parameters
//                	Bundle paramBundle = messageData.getBundle(PARAMS);
                	ArrayList<Bundle> params = new ArrayList<Bundle>(); //paramBundle.getParcelableArrayList(PARAMS);
                	
                	
                	o = performService(input, params);
                	break;
                }
                	
                case MSG_LIST:
                {
                	// Get the inputs
                	ArrayList<Bundle> inputs = messageData.getParcelableArrayList(INPUT);
                	
                	// Get the parameters
//                	Bundle paramBundle = messageData.getBundle(PARAMS);
                	ArrayList<Bundle> params = new ArrayList<Bundle>(); //paramBundle.getParcelableArrayList(PARAMS);
                	o = performList(inputs, params);
                	break;
                }
                
                case MSG_FAIL:
                {
                	// Might need to do something here, not sure it should even tell us if there's been a failure
                	break;
                }
                    
                default:
                    break;
            }
            
            Message returnMessage = isList ? Message.obtain(null, MSG_LIST, 0, 0) : Message.obtain(null, MSG_OBJECT, 0, 0);
            
            if(fail)
            {
            	Bundle b = new Bundle();
            	ArrayList<Bundle> bs = new ArrayList<Bundle>();
            	bs.add(failureBundle);
            	b.putParcelableArrayList(ComposableService.INPUT, bs);
            	returnMessage.setData(b);
            } 
            else if(wait)
            {
            	returnMessage.what = MSG_WAIT;
            	return returnMessage;
            }
            
            if(o == null)
            {
            	// If this is the case don't send the response back yet
            	
            	o = new ArrayList<Bundle>();
            	Bundle input = new Bundle();
            	input.putBundle(ComposableService.TEXT, new Bundle());
            	o.add(input);
            	
            	return returnMessage;
            }
            
            // At this point, o should be an array list of returned values
            
            Bundle newMessageData = new Bundle();
            newMessageData.putParcelableArrayList(INPUT, o);

            returnMessage.setData(newMessageData);

			return returnMessage;
		}
		
		@Override
		protected void onPostExecute(Message returnMessage)
		{
			if(!toastMessage.equals(""))
			{
				Toast.makeText(ComposableService.this, toastMessage, Toast.LENGTH_SHORT).show();
			}
			
			try 
    		{
    			if(messageSender == null)
    			{
    				Log.e(TAG, "Message sender is dead");
    				return;
    			}
    			
    			if(returnMessage.obj != null)
    			{
    				Test.isValidBundle(-1, -1, returnMessage.getData(), false);
    				if(LOG) Log.d(TAG, String.format("Sending back: %s", Library.printBundle(returnMessage.getData())));
    			}
    			
    			messageSender.send(returnMessage);
			}
    		catch (RemoteException e) 
    		{
				e.printStackTrace();
    		}
		}
    } 
   
    static class IncomingHandler extends Handler 
    {    	
    	private ComposableService cs;
    	
    	private IncomingHandler(ComposableService cs)
    	{
    		this.cs = cs;
    	}
    	
		@Override
        public void handleMessage(Message msg) 
        {		
			if(LOG) Log.d(TAG, "Handling message");
			cs.messageSender = msg.replyTo;
			Async monkey = cs.new Async(msg);
			monkey.execute();
        }
    }
}