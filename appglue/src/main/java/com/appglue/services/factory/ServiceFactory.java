package com.appglue.services.factory;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appglue.ComposableService;
import com.appglue.IODescription;
import com.appglue.Library;
import com.appglue.R;
import com.appglue.description.AppDescription;
import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.LaunchAppService;
import com.appglue.services.LocationService;
import com.appglue.services.NotificationService;
import com.appglue.services.PebbleNotification;
import com.appglue.services.SayHelloService;
import com.appglue.services.SendSMSService;
import com.appglue.services.ToastService;
import com.appglue.services.TubeService;
import com.appglue.services.triggers.AirplaneTrigger;
import com.appglue.services.triggers.BatteryTrigger;
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
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.JSON_APP;
import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.JSON_SERVICE_LIST;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.TAG;

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
        services.add(setupSendSMSService());

        services.add(setupBluetoothService());
        services.add(setupLaunchAppService());
		services.add(setupLocationService());

        // Triggers
        services.add(setupReceiveSMSTrigger());
        services.add(setupPowerConnectedTrigger());
        services.add(setupBluetoothTrigger());
        services.add(setupHeadphoneTrigger());
        services.add(setupAirplaneTrigger());
        services.add(setupBatteryTrigger());

        String all = setupServiceList(setupComposer(appDescription.iconLocation()), services);
        ArrayList<ServiceDescription> serviceList = ServiceDescription.parseServices(all, context, appDescription);

        for (ServiceDescription sd : serviceList) {
            ServiceDescription atomicId = registry.addServiceDescription(sd);

            if (atomicId == null) {
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
                        {ICON, iconLocation}
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

        int flags = 0;

        String helloJSON = Library.makeJSON(-1, "com.appglue", SayHelloService.class.getCanonicalName(),
                "Hello service",
                "This service says hello.",
                flags,
                0,
                new ArrayList<IODescription>(), outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, helloJSON);
    }

    private String setupLaunchAppService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType app = IOType.Factory.getType(IOType.Factory.APP);

        inputs.add(new IODescription(-1, LaunchAppService.APP_PACKAGE, "App", app, "The app that you want to launch.", false, null));

        String[] tags = {"App", "Android", "Run", "Launch"};

        int flags = 0;

        String appJSON = Library.makeJSON(-1, "com.appglue", LaunchAppService.class.getCanonicalName(),
                "Launch app",
                "Launch an app of your choice.",
                flags,
                0,
                inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, appJSON);
    }


    private String setupBluetoothService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("On", true));
        samples.add(new SampleValue("Off", false));

        inputs.add(new IODescription(-1, BluetoothService.BLUETOOTH_STATE, "New state", bool, "The new state of Bluetooth", false, samples));

        String[] tags = {"Bluetooth", "Setting"};
        int flags = 0;

        String btJSON = Library.makeJSON(-1, "com.appglue", BluetoothService.class.getCanonicalName(),
                "Bluetooth",
                "Turns bluetooth on or off!",
                flags,
                0,
                inputs, new ArrayList<IODescription>(), tags);

        return String.format("{\"%s\": {\"%s\":%s} }", JSON_SERVICE, JSON_SERVICE_DATA, btJSON);
    }

    private String setupWifiService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("On", true));
        samples.add(new SampleValue("Off", false));

        inputs.add(new IODescription(-1, WifiService.WIFI_STATE, "New state", bool, "The new state of the wifi", false, samples));

        String[] tags = {"Wifi", "Setting"};
        int flags = 0;

        String wifiJSON = Library.makeJSON(-1, "com.appglue", WifiService.class.getCanonicalName(),
                "Set WiFi",
                "Turns wifi on or off!",
                flags,
                0,
                inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s} }", JSON_SERVICE, JSON_SERVICE_DATA, wifiJSON);
    }

    private String setupSendSMSService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType phoneNumber = IOType.Factory.getType(IOType.Factory.PHONE_NUMBER);

        inputs.add(new IODescription(-1, SendSMSService.SMS_NUMBER, "Phone number", phoneNumber, "The number that you want to send the SMS to", true, null));
        inputs.add(new IODescription(-1, SendSMSService.SMS_MESSAGE, "Message", text, "The message that you want to send (Max 160 characters)", true, null));

        String[] tags = new String[] { "Send SMS" };
        int flags = ComposableService.FLAG_MONEY;

        String sendSMSJSON = Library.makeJSON(-1, "com.appglue", SendSMSService.class.getCanonicalName(),
                "Send a SMS", "Send a SMS to someone (limited to 160 characters)",
                flags, 0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s} }", JSON_SERVICE, JSON_SERVICE_DATA, sendSMSJSON);
    }


    private String setupTubeService() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType url = IOType.Factory.getType(IOType.Factory.URL);
        IOType imageDrawable = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);

        ArrayList<SampleValue> sampleLines = new ArrayList<SampleValue>();
        sampleLines.add(new SampleValue(TubeService.BAKERLOO, "Bakerloo"));
        sampleLines.add(new SampleValue(TubeService.CENTRAL, "Central"));
        sampleLines.add(new SampleValue(TubeService.CIRCLE, "Circle"));
        sampleLines.add(new SampleValue(TubeService.DISTRICT, "District"));
        sampleLines.add(new SampleValue(TubeService.DLR, "Docklands"));
        sampleLines.add(new SampleValue(TubeService.HAMMERSMITH_CITY, "Hammersmith & City"));
        sampleLines.add(new SampleValue(TubeService.JUBILEE, "Jubilee"));
        sampleLines.add(new SampleValue(TubeService.METROPOLITAN, "Metropolitan"));
        sampleLines.add(new SampleValue(TubeService.NORTHERN, "Northern"));
        sampleLines.add(new SampleValue(TubeService.OVERGROUND, "Overground"));
        sampleLines.add(new SampleValue(TubeService.PICCADILLY, "Piccadilly"));
        sampleLines.add(new SampleValue(TubeService.VICTORIA, "Victoria"));
        sampleLines.add(new SampleValue(TubeService.WATERLOO_CITY, "Waterloo & City"));

        ArrayList<SampleValue> sampleStatuses = new ArrayList<SampleValue>();
        sampleStatuses.add(new SampleValue(TubeService.MINOR_DELAYS, "minor delays"));
        sampleStatuses.add(new SampleValue(TubeService.GOOD_SERVICE, "good service"));
        sampleStatuses.add(new SampleValue(TubeService.SEVERE_DELAYS, "severe delays"));
        sampleStatuses.add(new SampleValue(TubeService.PART_CLOSURE, "part closure"));

        outputs.add(new IODescription(-1, TubeService.LINE_NAME, "Line name", text, "The name of the line.", false, sampleLines));
        outputs.add(new IODescription(-1, TubeService.LINE_STATUS, "Status", text, "The status of the line.", false, sampleStatuses));
        outputs.add(new IODescription(-1, TubeService.LINE_MESSAGE, "Message", text, "The message associated with the line.", false, null));
        outputs.add(new IODescription(-1, TubeService.LINE_URL, "Support website", url, "The URL that links to the page with the problem", false, null));
        outputs.add(new IODescription(-1, TubeService.LINE_ICON, "Line icon", imageDrawable, "An icon representing the line", false, null));

        String[] tags = {"Tube", "London", "Underground", "Travel", "tfl"};
        int flags = ComposableService.FLAG_MONEY | ComposableService.FLAG_NETWORK | ComposableService.FLAG_DELAY;

        String tubeJSON = Library.makeJSON(-1, "com.appglue", TubeService.class.getCanonicalName(),
                "Tube Status lookup",
                "This service returns information about problems that occur on the tube. It is accurate to within 10 minutes. It will return information about the line that is affected as well as the problem that is affecting it.",
                flags, 0,
                new ArrayList<IODescription>(), outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, tubeJSON);
    }

    private String setupPebbleService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        inputs.add(new IODescription(-1, PebbleNotification.PEBBLE_TITLE, "Pebble Notification Title", text, "The title of the notification to display on the Pebble", true, null));
        inputs.add(new IODescription(-1, PebbleNotification.PEBBLE_NOTIFICATION, "Pebble notification message", text, "the contents of the pebble notification", false, null));

        String[] tags = {"Pebble", "Notification", "Watch", "Smart watch"};
        int flags = 0;

        String pebbleJSON = Library.makeJSON(-1, "com.appglue", PebbleNotification.class.getCanonicalName(),
                "Pebble Notification",
                "Outputs notifications to your Pebble",
                flags, 0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, pebbleJSON);
    }

    private String setupNotificationService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType url = IOType.Factory.getType(IOType.Factory.URL);
        IOType imageD = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> priorities = new ArrayList<SampleValue>();
        priorities.add(new SampleValue("Default", NotificationCompat.PRIORITY_DEFAULT));
        priorities.add(new SampleValue("Low", NotificationCompat.PRIORITY_LOW));
        priorities.add(new SampleValue("High", NotificationCompat.PRIORITY_HIGH));

        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_TITLE, "Title", text, "The title of the notification.", true, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_TEXT, "Notification Message", text, "The message in the notification.", false, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_URL, "URL", url, "The URL that the notification points to.", false, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_IMAGE, "Image", imageD, "The image to use for the notification", false, null));
        inputs.add(new IODescription(-1, NotificationService.NOTIFICATION_PRIORITY, "Priority", set, "The priority of the notification", false, priorities));

        String[] tags = {"Android", "Notification", "Notify"};
        int flags = 0;

        String notificationJSON = Library.makeJSON(-1, "com.appglue", NotificationService.class.getCanonicalName(),
                "Android Notification",
                "Outputs Android Notifications into the Notification tray. They normally need a title and some text",
                flags, 0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, notificationJSON);
    }

    private String setupToastService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        inputs.add(new IODescription(-1, ToastService.TOAST_MESSAGE, "Message", text, "The text to be displayed", true, null));

        String[] tags = {"Message", "Notify", "Toast"};
        int flags = 0;

        String toastJSON = Library.makeJSON(-1, "com.appglue", ToastService.class.getCanonicalName(),
                "On screen message",
                "Outputs some text to the screen",
                flags, 0, inputs, null, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, toastJSON);
    }

	private String setupLocationService()
	{
		ArrayList<IODescription> getOutputs = new ArrayList<IODescription>();
		IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
		IOType number = IOType.Factory.getType(IOType.Factory.NUMBER);

		getOutputs.add(new IODescription(-1, LocationService.COUNTRY_NAME, "Country", text, "The country you're in.", false, null));
        getOutputs.add(new IODescription(-1, LocationService.COUNTRY_CODE, "Country code", text, "The code of the country you're in", false, null));
		getOutputs.add(new IODescription(-1, LocationService.LOCALITY_NAME, "Locality", text, "The town/city you're in/near", false, null));
        getOutputs.add(new IODescription(-1, LocationService.ROAD_NAME, "Road name", text, "The name of the road you're on", false, null));
		getOutputs.add(new IODescription(-1, LocationService.LATITUDE, "Latitude", number, "The rough latitude of where you are", false, null));
		getOutputs.add(new IODescription(-1, LocationService.LONGITUDE, "Longitude", number, "The rough longitude of where you are", false, null));

        String[] tags = new String[] { "Location", "GPS" };
        int flags = ComposableService.FLAG_DELAY | ComposableService.FLAG_LOCATION;

		String locationJSON = Library.makeJSON(-1, "com.appglue", LocationService.class.getCanonicalName(),
				"Location lookup",
				"Returns your location",
				flags, 0, null, getOutputs, tags);

		return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, locationJSON);
	}

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

    private String setupAirplaneTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> sample = new ArrayList<SampleValue>();
        sample.add(new SampleValue("On", true));
        sample.add(new SampleValue("Off", false));

        outputs.add(new IODescription(-1, AirplaneTrigger.STATE, "State", bool, "The state that airplane mode has been set to", true, sample));

        String[] tags = { "Airplane", "On", "Off" };
        int flags = ComposableService.FLAG_TRIGGER;

        String airplaneJSON = Library.makeJSON(-1, "com.appglue", AirplaneTrigger.class.getCanonicalName(),
                "Airplane mode Trigger",
                "Fires when airplane mode is turned on or off",
                flags, 0,
                null, outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, airplaneJSON);
    }

    private String setupPowerConnectedTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> sample = new ArrayList<SampleValue>();
        sample.add(new SampleValue("Connected", true));
        sample.add(new SampleValue("Disconnected", false));

        outputs.add(new IODescription(-1, PowerTrigger.CONNECTED, "Connected", bool, "Power connected = true, power disconnected = false", true, sample));

        String[] tags = {"Power", "AC", "Connected", "Disconnected"};
        int flags = ComposableService.FLAG_TRIGGER;

        String powerJSON = Library.makeJSON(-1, "com.appglue", PowerTrigger.class.getCanonicalName(),
                "Power connection",
                "Fires when the power is connected or disconnected",
                flags, 0,
                null, outputs, tags);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, powerJSON);
    }

    private String setupBatteryTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Battery Low", BatteryTrigger.STATE_LOW));
        samples.add(new SampleValue("Battery Okay", BatteryTrigger.STATE_OKAY));
        samples.add(new SampleValue("Battery Changed", BatteryTrigger.STATE_CHANGED));

        outputs.add(new IODescription(-1, BatteryTrigger.STATE, "Battery state", set, "The new state of the battery", true, samples));

        String[] tags = {"Battery", "Low" };

        String batteryTriggerJSON = Library.makeJSON(-1, "com.appglue", BatteryTrigger.class.getCanonicalName(),
                "Battery Trigger",
                "Signals that the state of the battery has changed",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags);

        // TODO It's going to take something special to test this one

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, batteryTriggerJSON);
    }

    private String setupBluetoothTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("On", BluetoothAdapter.STATE_OFF));
        samples.add(new SampleValue("Off", BluetoothAdapter.STATE_ON));
        samples.add(new SampleValue("Connected", BluetoothAdapter.STATE_CONNECTED));
        samples.add(new SampleValue("Disconnected", BluetoothAdapter.STATE_DISCONNECTED));

        outputs.add(new IODescription(-1, BluetoothTrigger.STATE, "Bluetooth State", set, "The new state of the bluetooth connection", true, samples));

        String[] tags = {"Bluetooth", "Connected", "Disconnected", "On", "Off"};

        String bluetoothTriggerJSON = Library.makeJSON(-1, "com.appglue", BluetoothTrigger.class.getCanonicalName(),
                "Bluetooth Trigger",
                "Signals that the state of the bluetooth has changed",
                ComposableService.FLAG_TRIGGER,
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
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, receiveSMSJSON);
    }

    private String setupHeadphoneTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType bool = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> plugged = new ArrayList<SampleValue>();
        plugged.add(new SampleValue("Plugged", true));
        plugged.add(new SampleValue("Unplugged", false));
        outputs.add(new IODescription(-1, HeadphoneTrigger.STATE, "Headphone State", bool, "The new state of the headphones", true, plugged));

        ArrayList<SampleValue> mic = new ArrayList<SampleValue>();
        mic.add(new SampleValue("Microphone", true));
        mic.add(new SampleValue("No microphone", false));
        outputs.add(new IODescription(-1, HeadphoneTrigger.MICROPHONE, "Microphone", bool, "Whether the headphones have a microphone", true, mic));

        String[] tags = {"Headphone", "Headset", "Plugged", "Unplugged", "Connected", "Disconnected"};

        String headphoneTriggerJSON = Library.makeJSON(-1, "com.appglue", HeadphoneTrigger.class.getCanonicalName(),
                "Headphone Trigger",
                "Activated when you plug or unplug the headphones",
                ComposableService.FLAG_TRIGGER,
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

    // Storage
    //	android.intent.action.DEVICE_STORAGE_LOW
    //	android.intent.action.DEVICE_STORAGE_OK

    // Docked/Dreaming
    //	android.intent.action.DOCK_EVENT

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

    // http://developers.google.com/drive/android-quickstart


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