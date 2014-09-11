package com.appglue.services;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appglue.IODescription;
import com.appglue.description.IOValue;
import com.appglue.Library;
import com.appglue.R;
import com.appglue.description.datatypes.IOType;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.triggers.BluetoothTrigger;
import com.appglue.services.triggers.HeadphoneTrigger;
import com.appglue.services.triggers.PowerTrigger;
import com.appglue.services.triggers.ReceiveSMSTrigger;
import com.appglue.services.util.BluetoothService;
import com.appglue.services.util.WifiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.DEVELOPER;
import static com.appglue.Constants.JSON_APP;
import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.JSON_SERVICE_LIST;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.ProcessType;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.ICON;

public class ServiceFactory {

    private static ServiceFactory factory;
    private AppDescription appDescription;
    private Context context;
    private Registry registry;

    public static ServiceFactory getInstance(Registry registry, Context context) throws JSONException {
        if (factory == null) {
            factory = new ServiceFactory(registry, context);
        }

        return factory;
    }

    private ServiceFactory(Registry registry, Context context) throws JSONException {
        this.context = context;
        this.registry = registry;

        String appData = setupComposer("");
        JSONObject jsonApp = new JSONObject(appData);
        appDescription = AppDescription.parseFromJSON(jsonApp);

        String iconString = Library.drawableToString(context.getResources().getDrawable(R.drawable.icon));
        LocalStorage storage = LocalStorage.getInstance();
        try {
            String filename = storage.writeIcon(appDescription.getPackageName(), iconString);
            appDescription.setIconLocation(filename);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Icon creating - File not found!!!!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Icon creating - IOException other than file not found!!!!");
            e.printStackTrace();
        }
    }

    public void setupServices() throws JSONException {

        ArrayList<String> services = new ArrayList<String>();
        services.add(setupHelloService());
        services.add(setupTubeService());
        services.add(setupNotificationService());
        services.add(setupPebbleService());
        services.add(setupToastService());
        services.add(setupWifiService());

        services.add(setupBluetoothService());
        services.add(setupLaunchAppService());
//		services.add(setupLocationService());

        // Triggers
        services.add(setupReceiveSMSTrigger());
        services.add(setupPowerConnectedTrigger());
        services.add(setupBluetoothTrigger());
        services.add(setupHeadphoneTrigger());

        String all = setupServiceList(setupComposer(appDescription.iconLocation()), services);
        ArrayList<ServiceDescription> serviceList = ServiceDescription.parseServices(all, context, appDescription);

        for (ServiceDescription sd : serviceList) {
            long atomicId = registry.addServiceDescription(sd);

            if (atomicId == -1) {
                Log.d(TAG, "The atomic ID is -1, apparently this is bad");
            }
        }
    }

    public AppDescription getAppDescription() {
        return appDescription;
    }

    private String setupComposer(String iconLocation) {
        String[][] appData = new String[][]
                {
                        {NAME, "appGlue"},
                        {PACKAGENAME, "com.appglue",},
                        {DESCRIPTION, "This is the app that does all the gluing, or is it glueing?"},
                        {DEVELOPER, "Andy Ridge"},
                        {ICON, iconLocation }
                };

        return setupApp(appData);
    }

    private String setupApp(String[][] app) {
        StringBuilder json = new StringBuilder("{");

        for (int i = 0; i < app.length; i++) {
            json.append(String.format(
                    "\"%s\":\"%s\"", app[i][0], app[i][1]
            ));

            if (i < app.length - 1)
                json.append(",");
        }

        json.append("}");

        return json.toString();
    }

    private String setupHelloService() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        outputs.add(new IODescription(-1, SayHelloService.TAG_HELLO, "Hello text", text, "The service is saying hello.", false, null));

        String[] tags = {"Hello"};

        String helloJSON = Library.makeJSON(-1, "com.appglue", SayHelloService.class.getCanonicalName(),
                "Hello service",
                "This service says hello.",
                SayHelloService.processType.index,
                0,
                new ArrayList<IODescription>(), outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, helloJSON);
    }

    private String setupLaunchAppService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType app = IOType.Factory.getType(IOType.Factory.APP);

        inputs.add(new IODescription(-1, LaunchAppService.APP_PACKAGE, "App", app, "The app that you want to launch.", false, null));

        String[] tags = {"App", "Android", "Run", "Launch"};

        String appJSON = Library.makeJSON(-1, "com.appglue", LaunchAppService.class.getCanonicalName(),
                "Launch app",
                "Launch an app of your choice.",
                LaunchAppService.processType.index,
                0,
                inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, appJSON);
    }


    private String setupBluetoothService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<IOValue> samples = new ArrayList<IOValue>();
        samples.add(new IOValue("On", true));
        samples.add(new IOValue("Off", false));

        inputs.add(new IODescription(-1, BluetoothService.BLUETOOTH_STATE, "New state", bool, "The new state of Bluetooth", false, samples));

        String[] tags = {"Bluetooth", "Setting"};

        String btJSON = Library.makeJSON(-1, "com.appglue", BluetoothService.class.getCanonicalName(),
                "Bluetooth",
                "Turns bluetooth on or off!",
                BluetoothService.processType.index,
                0,
                inputs, new ArrayList<IODescription>(), tags);

        return String.format("{\"%s\": {\"%s\":%s} }", JSON_SERVICE, JSON_SERVICE_DATA, btJSON);
    }

    // TODO Need to make sure all the text is escaped

    private String setupWifiService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<IOValue> samples = new ArrayList<IOValue>();
        samples.add(new IOValue("On", true));
        samples.add(new IOValue("Off", false));

        inputs.add(new IODescription(-1, WifiService.WIFI_STATE, "New state", bool, "The new state of the wifi", false, samples));

        String[] tags = {"Wifi", "Setting"};

        String wifiJSON = Library.makeJSON(-1, "com.appglue", WifiService.class.getCanonicalName(),
                "Set WiFi",
                "Turns wifi on or off!",
                WifiService.processType.index,
                0,
                inputs, new ArrayList<IODescription>(), tags);

        return String.format("{\"%s\": {\"%s\":%s} }", JSON_SERVICE, JSON_SERVICE_DATA, wifiJSON);
    }


    private String setupTubeService() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType url = IOType.Factory.getType(IOType.Factory.URL);
        IOType imageDrawable = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);

        ArrayList<IOValue> sampleLines = new ArrayList<IOValue>();
        sampleLines.add(new IOValue("Bakerloo", "Bakerloo"));
        sampleLines.add(new IOValue("Central", "Central"));
        sampleLines.add(new IOValue("Circle", "Circle"));
        sampleLines.add(new IOValue("District", "District"));
        sampleLines.add(new IOValue("Docklands", "Docklands"));
        sampleLines.add(new IOValue("Hammersmith & City", "Hammersmith & City"));
        sampleLines.add(new IOValue("Jubilee", "Jubilee"));
        sampleLines.add(new IOValue("Metropolitan", "Metropolitan"));
        sampleLines.add(new IOValue("Northern", "Northern"));
        sampleLines.add(new IOValue("Overground", "Overground"));
        sampleLines.add(new IOValue("Picadilly", "Picadilly"));
        sampleLines.add(new IOValue("Victoria", "Victoria"));
        sampleLines.add(new IOValue("Waterloo & City", "Waterloo & City"));

        ArrayList<IOValue> sampleStatuses = new ArrayList<IOValue>();
        sampleStatuses.add(new IOValue("Minor delays", "minor delays"));
        sampleStatuses.add(new IOValue("Major delays", "major delays"));
        sampleStatuses.add(new IOValue("Good Service", "good service"));
        sampleStatuses.add(new IOValue("Severe delays", "severe delays"));
        sampleStatuses.add(new IOValue("Part Closure", "part closure"));

        outputs.add(new IODescription(-1, TubeService.LINE_NAME, "Line name", text, "The name of the line.", false, sampleLines));
        outputs.add(new IODescription(-1, TubeService.LINE_STATUS, "Status", text, "The status of the line.", false, sampleStatuses));
        outputs.add(new IODescription(-1, TubeService.LINE_MESSAGE, "Message", text, "The message associated with the line.", false, null));
        outputs.add(new IODescription(-1, TubeService.LINE_URL, "Support website", url, "The URL that links to the page with the problem", false, null));
        outputs.add(new IODescription(-1, TubeService.LINE_ICON, "Line icon", imageDrawable, "An icon representing the line", false, null));

        String[] tags = {"Tube", "London", "Underground", "Travel", "tfl"};

        String tubeJSON = Library.makeJSON(-1, "com.appglue", TubeService.class.getCanonicalName(),
                "Tube Status lookup",
                "This service returns information about problems that occur on the tube. It is accurate to within 10 minutes. It will return information about the line that is affected as well as the problem that is affecting it.",
                TubeService.processType.index,
                0,
                new ArrayList<IODescription>(), outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, tubeJSON);
    }

    private String setupPebbleService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        inputs.add(new IODescription(-1, PebbleNotification.PEBBLE_TITLE, "Pebble Notification Title", text, "The title of the notification to display on the Pebble", true, null));
        inputs.add(new IODescription(-1, PebbleNotification.PEBBLE_NOTIFICATION, "Pebble notification message", text, "the contents of the pebble notification", false, null));

        String[] tags = {"Pebble", "Notification", "Watch", "Smart watch"};

        String pebbleJSON = Library.makeJSON(-1, "com.appglue", PebbleNotification.class.getCanonicalName(),
                "Pebble Notification",
                "Outputs notifications to your Pebble",
                PebbleNotification.processType.index,
                0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, pebbleJSON);
    }

    private String setupNotificationService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType url = IOType.Factory.getType(IOType.Factory.URL);
        IOType imageD = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<IOValue> priorities = new ArrayList<IOValue>();
        priorities.add(new IOValue("Default", NotificationCompat.PRIORITY_DEFAULT));
        priorities.add(new IOValue("Low", NotificationCompat.PRIORITY_LOW));
        priorities.add(new IOValue("High", NotificationCompat.PRIORITY_HIGH));

        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_TITLE, "Title", text, "The title of the notification.", true, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_TEXT, "Notification Message", text, "The message in the notification.", false, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_URL, "URL", url, "The URL that the notification points to.", false, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_IMAGE, "Image", imageD, "The image to use for the notification", false, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_PRIORITY, "Priority", set, "The priority of the notification", false, priorities));

        String[] tags = {"Android", "Notification", "Notify"};

        String notificationJSON = Library.makeJSON(-1, "com.appglue", NotificationService.class.getCanonicalName(),
                "Android Notification",
                "Outputs Android Notifications into the Notification tray. They normally need a title and some text",
                NotificationService.processType.index,
                0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, notificationJSON);
    }

    private String setupToastService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        inputs.add(new IODescription(-1, ToastService.TOAST_MESSAGE, "Message", text, "The text to be displayed", true, null));

        String[] tags = {"Message", "Notify", "Toast"};

        String toastJSON = Library.makeJSON(-1, "com.appglue", ToastService.class.getCanonicalName(),
                "On screen message",
                "Outputs some text to the screen",
                ToastService.processType.index,
                0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, toastJSON);
    }

//	private String setupLocationService()
//	{
//		ArrayList<ServiceIO> outputs = new ArrayList<ServiceIO>();
//		IOType text = IOType.Factory.type(IOType.Factory.TEXT);
//		IOType number = IOType.Factory.type(IOType.Factory.NUMBER);
//		
//		outputs.add(new ServiceIO(LocationService.COUNTRY_NAME, "Country", text, "The country you're in.", false));
//		outputs.add(new ServiceIO(LocationService.REGION_NAME, "Region", text, "The state/county you're in.", false));
////		outputs.add(new ServiceIO(LocationService.LOCALITY_NAME, "Locality", text, "The town/city you're in/near"));
//		outputs.add(new ServiceIO(LocationService.LATITUDE, "Latitude", number, "The rough latitude of where you are", false));
//		outputs.add(new ServiceIO(LocationService.LONGITUDE, "Longitude", number, "The rough longitude of where you are", false));
//		
//		String locationJSON = Library.makeJSON(-1, "com.appglue", LocationService.class.getCanonicalName(), 
//				"Location lookup", 
//				"Returns your location",
//				TubeService.processType.index,
//				0, 
//				new ArrayList<ServiceIO>(), outputs);
//		
//		return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, locationJSON);
//		return "";
//	}

//		String[][] serviceData = new String[][]
//      	{
//  			{ ID, "-1" },
//  			{ PACKAGENAME, "com.appglue" },
//  			{ CLASSNAME, TubeService.class.getCanonicalName() },
//  			{ NAME, "Tube Status Lookup" },
//  			{ DESCRIPTION, "This service returns information about problems that occur on the tube. It is accurate to within 10 minutes. It will return information about the line that is affected as well as the problem that is affecting it." },
//  			{ DEVELOPER, "Andy Ridge" },
//  			{ PROCESS_TYPE, "" + TubeService.processType.index},
//  			{ PRICE, "0" },
//  			{ INPUT_NAME, "None" },
//  			{ INPUT_TYPE, "void" },
//  			{ INPUT_DESCRIPTION, "Nothing" },
//  			{ OUTPUT_NAME, "Tube Status" },
//			{ OUTPUT_TYPE, ""}, // TubeStatus.class.getCanonicalName() },
//			{ OUTPUT_DESCRIPTION, "A tube status" },
//      	};
//		
//		
//		
//		String[][][] paramData = new String[][][]
//        {
//			{
//				{ NAME, "lines" },
//				{ DESCRIPTION, "Choose the tube lines that you are interested in" },
//				{ PARAM_TYPE, "" + Param.MANY_SET.index },
//				{ PARAM_REQUIREDNESS, "" + Requiredness.OPTIONAL.index },
//				{ POSS_USER, Library.implode(userNames, ",", true) },
//				{ POSS_SYSTEM, Library.implode(systemNames, ",", true) }
//			}
//        };
//  		
//  		return setupService(serviceData, paramData);
//	}

    protected String setupServiceList(String appData, ArrayList<String> services) {
        StringBuilder json = new StringBuilder(String.format(
                "{ \"%s\":%s,",
                JSON_APP, appData));

        // Start the array of services
        json.append(String.format("\"%s\":[", JSON_SERVICE_LIST));

        for (int i = 0; i < services.size(); i++) {
            json.append(services.get(i));

            json.append(i == services.size() - 1 ? "" : ",");
        }

        // Close the service array
        json.append("]}");
        return json.toString();
    }


    ////////////////////
    // Triggers
    ///////////////////

    private String setupPowerConnectedTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<IOValue> sample = new ArrayList<IOValue>();
        sample.add(new IOValue("Connected", true));
        sample.add(new IOValue("Disconnected", false));

        outputs.add(new IODescription(-1, PowerTrigger.CONNECTED, "Connected", bool, "Power connected = true, power disconnected = false", true, sample));

        String[] tags = {"Power", "AC", "Connected", "Disconnected"};

        String powerJSON = Library.makeJSON(-1, "com.appglue", PowerTrigger.class.getCanonicalName(),
                "Power connection",
                "Fires when the power is connected or disconnected",
                ProcessType.TRIGGER.index, 0,
                null, outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, powerJSON);
    }

    private String setupBluetoothTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<IOValue> samples = new ArrayList<IOValue>();
        samples.add(new IOValue("On", BluetoothAdapter.STATE_OFF));
        samples.add(new IOValue("Off", BluetoothAdapter.STATE_ON));
//		samples.add(new IOValue("Turning on", BluetoothAdapter.STATE_TURNING_ON));
//		samples.add(new IOValue("Turning off", BluetoothAdapter.STATE_TURNING_OFF));
        samples.add(new IOValue("Connected", BluetoothAdapter.STATE_CONNECTED));
        samples.add(new IOValue("Disconnected", BluetoothAdapter.STATE_DISCONNECTED));
//		samples.add(new IOValue("Connecting", BluetoothAdapter.STATE_CONNECTING));
//		samples.add(new IOValue("Disconnecting", BluetoothAdapter.STATE_DISCONNECTING));

        outputs.add(new IODescription(-1, BluetoothTrigger.STATE, "Bluetooth State", set, "The new state of the bluetooth connection", true, samples));

        String[] tags = {"Bluetooth", "Connected", "Disconnected", "On", "Off"};

        String bluetoothTriggerJSON = Library.makeJSON(-1, "com.appglue", BluetoothTrigger.class.getCanonicalName(),
                "Bluetooth Trigger",
                "Signals that the state of the bluetooth has changed",
                BluetoothTrigger.processType.index,
                0, null, outputs, tags);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, bluetoothTriggerJSON);

    }

    private String setupReceiveSMSTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType phoneNumber = IOType.Factory.getType(IOType.Factory.PHONE_NUMBER);

        outputs.add(new IODescription(-1, ReceiveSMSTrigger.SMS_NUMBER, "Phone number", phoneNumber, "The number where the SMS came from", true, null));
        outputs.add(new IODescription(-1, ReceiveSMSTrigger.SMS_MESSAGE, "Message", text, "The contents of the SMS", true, null));

        String[] tags = {"SMS", "Text message", "Receive"};

        String receiveSMSJSON = Library.makeJSON(-1, "com.appglue", ReceiveSMSTrigger.class.getCanonicalName(),
                "Receive SMS",
                "Signals a text has arrived",
                ReceiveSMSTrigger.processType.index,
                0, null, outputs, tags);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, receiveSMSJSON);
    }

    private String setupHeadphoneTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<IOValue> plugged = new ArrayList<IOValue>();
        plugged.add(new IOValue("Plugged", true));
        plugged.add(new IOValue("Unplugged", false));
        outputs.add(new IODescription(-1, HeadphoneTrigger.STATE, "Headphone State", bool, "The new state of the headphones", true, plugged));

        ArrayList<IOValue> mic = new ArrayList<IOValue>();
        mic.add(new IOValue("Microphone", true));
        mic.add(new IOValue("No microphone", false));
        outputs.add(new IODescription(-1, HeadphoneTrigger.MICROPHONE, "Microphone", bool, "Whether the headphones have a microphone", true, mic));

        String[] tags = {"Headphone", "Headset", "Plugged", "Unplugged", "Connected", "Disconnected"};

        String headphoneTriggerJSON = Library.makeJSON(-1, "com.appglue", HeadphoneTrigger.class.getCanonicalName(),
                "Headphone Trigger",
                "Activated when you plug or unplug the headphones",
                HeadphoneTrigger.processType.index,
                0, null, outputs, tags);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, headphoneTriggerJSON);
    }

    // Android Triggers

    // Media taken Taken
    // android.hardware.action.NEW_PICTURE
    //	android.hardware.action.NEW_VIDEO

    // On shutdown/startup
    // android.intent.action.ACTION_SHUTDOWN
    //	android.intent.action.BOOT_COMPLETED
    //	android.intent.action.REBOOT

    // Airplane mode
    //	android.intent.action.AIRPLANE_MODE

    // Battery
    //	android.intent.action.BATTERY_CHANGED
    //	android.intent.action.BATTERY_LOW
    //	android.intent.action.BATTERY_OKAY

    // Storage
    //	android.intent.action.DEVICE_STORAGE_LOW
    //	android.intent.action.DEVICE_STORAGE_OK

    // Docked/Dreaming
    //	android.intent.action.DOCK_EVENT
    //	android.intent.action.DREAMING_STARTED
    //	android.intent.action.DREAMING_STOPPED

    // Google Talk
    //	android.intent.action.GTALK_CONNECTED
    //	android.intent.action.GTALK_DISCONNECTED

    // Media Stuff?
    //	android.intent.action.MEDIA_BAD_REMOVAL
    //	android.intent.action.MEDIA_BUTTON
    //	android.intent.action.MEDIA_CHECKING
    //	android.intent.action.MEDIA_EJECT
    //	android.intent.action.MEDIA_MOUNTED
    //	android.intent.action.MEDIA_NOFS
    //	android.intent.action.MEDIA_REMOVED
    //	android.intent.action.MEDIA_SCANNER_FINISHED
    //	android.intent.action.MEDIA_SCANNER_SCAN_FILE
    //	android.intent.action.MEDIA_SCANNER_STARTED
    //	android.intent.action.MEDIA_SHARED
    //	android.intent.action.MEDIA_UNMOUNTABLE
    //	android.intent.action.MEDIA_UNMOUNTED

    // Making call
    //	android.intent.action.NEW_OUTGOING_CALL
    //	android.intent.action.NEW_VOICEMAIL
    //	android.intent.action.PHONE_STATE

    // Screen on/off
    //	android.intent.action.SCREEN_OFF
    //	android.intent.action.SCREEN_ON

    // Time
    //	android.intent.action.TIMEZONE_CHANGED
    //	android.intent.action.TIME_SET
    //	android.intent.action.TIME_TICK

    // Wallpaper
    //	android.intent.action.WALLPAPER_CHANGED


    // Ringer mode
    //	android.media.RINGER_MODE_CHANGED
    //	android.media.VIBRATE_SETTING_CHANGED

    // Background data/network/whatever
    //	android.net.conn.BACKGROUND_DATA_SETTING_CHANGED
    //	android.net.conn.CONNECTIVITY_CHANGE
    //	android.net.nsd.STATE_CHANGED

    // Wifi
    //	android.net.wifi.NETWORK_IDS_CHANGED
    //	android.net.wifi.RSSI_CHANGED
    //	android.net.wifi.SCAN_RESULTS
    //	android.net.wifi.STATE_CHANGE
    //	android.net.wifi.WIFI_STATE_CHANGED

    // P2P Wifi
    //	android.net.wifi.p2p.CONNECTION_STATE_CHANGE
    //	android.net.wifi.p2p.DISCOVERY_STATE_CHANGE
    //	android.net.wifi.p2p.PEERS_CHANGED
    //	android.net.wifi.p2p.STATE_CHANGED
    //	android.net.wifi.p2p.THIS_DEVICE_CHANGED
    //	android.net.wifi.supplicant.CONNECTION_CHANGE
    //	android.net.wifi.supplicant.STATE_CHANGE

    // NFC
    //	android.nfc.action.ADAPTER_STATE_CHANGED

    // Atooma
    // Phone mode
    // Silent mode on/off
    // Wifi
    // Disconnected/connected
    // Internet
    // On/off
    // Time
    // Countdown alarm
    // Particular time
    // Battery
    // Level reached
    // unplugged/plugged
    // App launcher
    // App installed
    // App uninstalled
    // Bluetooth
    // Connected/disconnected
    // Device found
    // Headphone jack
    // Plugged
    // Data network
    // On/off
    // Standby
    // On/off
    // Camera
    // New photo
    // New video
    // Light sensor
    // Light On/off
    // Call
    // Ringing
    // Idle
    // In call
    // Signal strength
    // Location
    // Exit/enter area
    // Shake sensor
    // Horizontal shake
    // Vertical shake
    // GPS
    // On/off
    // Speed

    // Audio file
    // Added/removed
    // Any file
    // Added/removed
    // Directory
    // Added/removed
    // Photo
    // Added/removed
    // Video
    // Added/removed

    // Google Drive
    // Directory Added/removed
    // File Added/removed/modified
    // Space used
    // Facebook
    // Birthday
    // Status update
    // Twitter
    // Favourited
    // Message received
    // New tweet
    // Reply received
    // DropBox
    // Directory added/removed
    // File added/removed
    // 	Space used
    // Media uploaded


//	android.hardware.action.NEW_VIDEO
//	android.intent.action.ACTION_POWER_CONNECTED
//	android.intent.action.ACTION_POWER_DISCONNECTED
//	android.intent.action.ACTION_SHUTDOWN
//	android.intent.action.AIRPLANE_MODE
//	android.intent.action.BATTERY_LOW
//	android.intent.action.BATTERY_OKAY
//	android.intent.action.BOOT_COMPLETED
//	android.intent.action.DATA_SMS_RECEIVED
//	android.intent.action.DATE_CHANGED
//	android.intent.action.DEVICE_STORAGE_LOW
//	android.intent.action.DEVICE_STORAGE_OK
//	android.intent.action.DOCK_EVENT
//	android.intent.action.HEADSET_PLUG
//	android.intent.action.NEW_OUTGOING_CALL
//	android.intent.action.NEW_VOICEMAIL
//	android.intent.action.SCREEN_OFF
//	android.intent.action.SCREEN_ON
//	android.provider.Telephony.SMS_RECEIVED
//	android.provider.Telephony.SMS_REJECTED

    // Train prices in foreign country
    // Currency converter
    // Facebook status
    // Tweet
    // Foursquare check in
    // Send SMS
    // Send email
    // LastFM event lookup
    // Weather tomorrow is .. in ..
    // Traffic problems
    // Train problems
    // Output widget

    // Composition ideas
    // Train price -> converter -> widget?
    // Tweet -> Facebook status
    // Foursquare check in -> Tweet
    // Foursquare check in -> LastFM Event lookup -> Notification
    // Weather tomorrow -> Send SMS to me
    // Weather tomorrow -> Just notify
    // Traffic problems -> Notification
    // Train problems -> Notification
}
