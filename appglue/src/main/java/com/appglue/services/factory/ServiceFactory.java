package com.appglue.services.factory;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
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
import com.appglue.engine.model.CompositeService;
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
import com.appglue.services.triggers.DeviceStorageTrigger;
import com.appglue.services.triggers.DockedTrigger;
import com.appglue.services.triggers.HeadphoneTrigger;
import com.appglue.services.triggers.MobileConnectionTrigger;
import com.appglue.services.triggers.NFCTrigger;
import com.appglue.services.triggers.PowerTrigger;
import com.appglue.services.triggers.ReceiveSMSTrigger;
import com.appglue.services.triggers.RingerTrigger;
import com.appglue.services.triggers.ScreenStateTrigger;
import com.appglue.services.triggers.StartupTrigger;
import com.appglue.services.triggers.WifiTrigger;
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
import static com.appglue.library.AppGlueConstants.PREFS_HIDDEN;
import static com.appglue.library.AppGlueConstants.RUN_BEFORE;

import static com.appglue.description.Category.Factory.*;

public class ServiceFactory {

    private static ServiceFactory factory;
    private CompositeFactory compositeFactory;
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
        services.add(setupStartupTrigger());
        services.add(setupDockedTrigger());
        services.add(setupDeviceStorageTrigger());
        services.add(setupScreenStateTrigger());
        services.add(setupWifiTrigger());
        services.add(setupRingerStateTrigger());
        services.add(setupMobileConnectionTrigger());
        services.add(setupNFCTrigger());

        String all = setupServiceList(setupComposer(appDescription.iconLocation()), services);
        ArrayList<ServiceDescription> serviceList = ServiceDescription.parseServices(all, context, appDescription);

        for (ServiceDescription sd : serviceList) {
            ServiceDescription atomicId = registry.addServiceDescription(sd);

            if (atomicId == null) {
                Log.d(TAG, "The atomic ID is -1, apparently this is bad");
            }
        }

        SharedPreferences hiddenPrefs = context.getSharedPreferences(PREFS_HIDDEN, Context.MODE_PRIVATE);
        boolean first = !hiddenPrefs.getBoolean(RUN_BEFORE, false);

        if (first) {

            // Setup the initial set of composites that we need
            ArrayList<CompositeService> composites = CompositeFactory.createSampleComposites(context);
            for (CompositeService cs : composites) {
                registry.addComposite(cs);
            }
            hiddenPrefs.edit().putBoolean(RUN_BEFORE, true).apply();
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
        String[] categories = { MISC };

        int flags = 0;

        String helloJSON = Library.makeJSON(-1, "com.appglue", SayHelloService.class.getCanonicalName(),
                "Hello service", "Hello",
                "This service says hello.",
                flags,
                0,
                new ArrayList<IODescription>(), outputs, tags, categories, null);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, helloJSON);
    }

    private String setupLaunchAppService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType app = IOType.Factory.getType(IOType.Factory.APP);

        inputs.add(new IODescription(-1, LaunchAppService.APP_PACKAGE, "App", app, "The app that you want to launch.", false, null));

        String[] tags = {"App", "Run", "Launch"};
        String[] categories = {DEVICE_UTILS};

        int flags = 0;

        String appJSON = Library.makeJSON(-1, "com.appglue", LaunchAppService.class.getCanonicalName(),
                "Launch app", "App",
                "Launch an app of your choice.",
                flags,
                0,
                inputs, null, tags, categories, null);

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
        String[] categories = { NETWORK_UTILS };
        String[] features = { PackageManager.FEATURE_BLUETOOTH };

        int flags = 0;

        String btJSON = Library.makeJSON(-1, "com.appglue", BluetoothService.class.getCanonicalName(),
                "Bluetooth", "BT",
                "Turns bluetooth on or off!",
                flags,
                0,
                inputs, new ArrayList<IODescription>(), tags, categories, features);

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
        String[] categories = { NETWORK_UTILS };
        String[] features = { PackageManager.FEATURE_WIFI };

        int flags = 0;

        String wifiJSON = Library.makeJSON(-1, "com.appglue", WifiService.class.getCanonicalName(),
                "Set WiFi", "WiFi",
                "Turns wifi on or off!",
                flags,
                0,
                inputs, null, tags, categories, features);

        return String.format("{\"%s\": {\"%s\":%s} }", JSON_SERVICE, JSON_SERVICE_DATA, wifiJSON);
    }

    private String setupSendSMSService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType phoneNumber = IOType.Factory.getType(IOType.Factory.PHONE_NUMBER);

        inputs.add(new IODescription(-1, SendSMSService.SMS_NUMBER, "Phone number", phoneNumber, "The number that you want to send the SMS to", true, null));
        inputs.add(new IODescription(-1, SendSMSService.SMS_MESSAGE, "Message", text, "The message that you want to send (Max 160 characters)", true, null));

        String[] tags = new String[]{"Send SMS"};
        String[] categories = new String[] {DEVICE_UTILS};
        String[] features = { PackageManager.FEATURE_TELEPHONY };

        int flags = ComposableService.FLAG_MONEY;

        String sendSMSJSON = Library.makeJSON(-1, "com.appglue", SendSMSService.class.getCanonicalName(),
                "Send a SMS", "SMS", "Send a SMS to someone (limited to 160 characters)",
                flags, 0, inputs, null, tags, categories, features);

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
        String[] categories = { TRAVEL };

        int flags = ComposableService.FLAG_NETWORK | ComposableService.FLAG_DELAY;

        String tubeJSON = Library.makeJSON(-1, "com.appglue", TubeService.class.getCanonicalName(),
                "Tube Status lookup", "Tube",
                "This service returns information about problems that occur on the tube. It is accurate to within 10 minutes. It will return information about the line that is affected as well as the problem that is affecting it.",
                flags, 0,
                new ArrayList<IODescription>(), outputs, tags, categories, null);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, tubeJSON);
    }

    private String setupPebbleService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        inputs.add(new IODescription(-1, PebbleNotification.PEBBLE_TITLE, "Pebble Notification Title", text, "The title of the notification to display on the Pebble", true, null));
        inputs.add(new IODescription(-1, PebbleNotification.PEBBLE_NOTIFICATION, "Pebble notification message", text, "the contents of the pebble notification", false, null));

        String[] tags = {"Pebble", "Notification", "Watch", "Smart watch"};
        String[] cats = { WEARABLE };
        String[] features = { PackageManager.FEATURE_BLUETOOTH };

        int flags = 0;

        String pebbleJSON = Library.makeJSON(-1, "com.appglue", PebbleNotification.class.getCanonicalName(),
                "Pebble Notification", "Pebble",
                "Outputs notifications to your Pebble",
                flags, 0, inputs, null, tags, cats, features);

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

        String[] tags = {"Notification", "Notify"};
        String[] cats = {DEVICE_UTILS};

        int flags = 0;

        String notificationJSON = Library.makeJSON(-1, "com.appglue", NotificationService.class.getCanonicalName(),
                "Android Notification", "Notify",
                "Outputs Android Notifications into the Notification tray. They normally need a title and some text",
                flags, 0, inputs, null, tags, cats, null);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, notificationJSON);
    }

    private String setupToastService() {
        ArrayList<IODescription> inputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        inputs.add(new IODescription(-1, ToastService.TOAST_MESSAGE, "Message", text, "The text to be displayed", true, null));

        String[] tags = {"Message", "Notify", "Toast"};
        String[] cats = {DEVICE_UTILS};
        int flags = 0;

        String toastJSON = Library.makeJSON(-1, "com.appglue", ToastService.class.getCanonicalName(),
                "On screen message", "Popup",
                "Outputs some text to the screen",
                flags, 0, inputs, null, tags, cats, null);

        return String.format("{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, toastJSON);
    }

    private String setupLocationService() {
        ArrayList<IODescription> getOutputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType number = IOType.Factory.getType(IOType.Factory.NUMBER);

        getOutputs.add(new IODescription(-1, LocationService.COUNTRY_NAME, "Country", text, "The country you're in.", false, null));
        getOutputs.add(new IODescription(-1, LocationService.COUNTRY_CODE, "Country code", text, "The code of the country you're in", false, null));
        getOutputs.add(new IODescription(-1, LocationService.LOCALITY_NAME, "Locality", text, "The town/city you're in/near", false, null));
        getOutputs.add(new IODescription(-1, LocationService.ROAD_NAME, "Road name", text, "The name of the road you're on", false, null));
        getOutputs.add(new IODescription(-1, LocationService.LATITUDE, "Latitude", number, "The rough latitude of where you are", false, null));
        getOutputs.add(new IODescription(-1, LocationService.LONGITUDE, "Longitude", number, "The rough longitude of where you are", false, null));

        String[] tags = new String[]{"Location", "GPS"};
        String[] cats = {DEVICE_UTILS};
        String[] features = { PackageManager.FEATURE_LOCATION };

        int flags = ComposableService.FLAG_DELAY | ComposableService.FLAG_LOCATION;

        String locationJSON = Library.makeJSON(-1, "com.appglue", LocationService.class.getCanonicalName(),
                "Location lookup", "Location",
                "Returns your location",
                flags, 0, null, getOutputs, tags, cats, features);

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

        String[] tags = {"Airplane", "On", "Off"};
        int flags = ComposableService.FLAG_TRIGGER;
        String[] cats = {TRIGGERS, NETWORK_UTILS};

        String airplaneJSON = Library.makeJSON(-1, "com.appglue", AirplaneTrigger.class.getCanonicalName(),
                "Airplane mode Trigger", "Airplane",
                "Fires when airplane mode is turned on or off",
                flags, 0,
                null, outputs, tags, cats, null);

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
        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String powerJSON = Library.makeJSON(-1, "com.appglue", PowerTrigger.class.getCanonicalName(),
                "Power connection", "Power",
                "Fires when the power is connected or disconnected",
                flags, 0,
                null, outputs, tags, cats, null);

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

        String[] tags = {"Battery", "Low"};
        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String batteryTriggerJSON = Library.makeJSON(-1, "com.appglue", BatteryTrigger.class.getCanonicalName(),
                "Battery Trigger", "-> Battery",
                "Signals that the state of the battery has changed",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, null);

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
        String[] cats = {TRIGGERS, NETWORK_UTILS};
        String[] features = { PackageManager.FEATURE_BLUETOOTH };

        String bluetoothTriggerJSON = Library.makeJSON(-1, "com.appglue", BluetoothTrigger.class.getCanonicalName(),
                "Bluetooth Trigger", "-> BT",
                "Signals that the state of the bluetooth has changed",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, features);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, bluetoothTriggerJSON);

    }

    private String setupReceiveSMSTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);
        IOType phoneNumber = IOType.Factory.getType(IOType.Factory.PHONE_NUMBER);

        outputs.add(new IODescription(-1, ReceiveSMSTrigger.SMS_NUMBER, "Phone number", phoneNumber, "The number where the SMS came from", true, null));
        outputs.add(new IODescription(-1, ReceiveSMSTrigger.SMS_MESSAGE, "Message", text, "The contents of the SMS", true, null));

        String[] tags = {"SMS", "Text message", "Receive"};
        String[] cats = {TRIGGERS, DEVICE_UTILS};
        String[] features = { PackageManager.FEATURE_TELEPHONY};

        String receiveSMSJSON = Library.makeJSON(-1, "com.appglue", ReceiveSMSTrigger.class.getCanonicalName(),
                "Receive SMS", "-> SMS",
                "Signals a text has arrived",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, features);

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
        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String headphoneTriggerJSON = Library.makeJSON(-1, "com.appglue", HeadphoneTrigger.class.getCanonicalName(),
                "Headphone Trigger", "-> HP",
                "Activated when you plug or unplug the headphones",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, null);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, headphoneTriggerJSON);
    }

    private String setupStartupTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Startup", StartupTrigger.STARTUP));
        samples.add(new SampleValue("Shutdown", StartupTrigger.SHUTDOWN));
        samples.add(new SampleValue("Reboot", StartupTrigger.REBOOT));

        String[] tags = {"Startup", "Boot", "Shutdown", "Turn off", "Reboot"};
        outputs.add(new IODescription(-1, StartupTrigger.STATE, "Phone State", set, "Whether the phone has turned on, off, or rebooted", true, samples));

        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String startupTriggerJSON = Library.makeJSON(-1, "com.appglue", StartupTrigger.class.getCanonicalName(),
                "Startup Trigger", "-> Boot",
                "Activated when your phone has finished turning on, is about to turn off, or has rebooted",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, null);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, startupTriggerJSON);
    }

    private String setupDockedTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Undocked", Intent.EXTRA_DOCK_STATE_UNDOCKED));
        samples.add(new SampleValue("Car docked", Intent.EXTRA_DOCK_STATE_CAR));
        samples.add(new SampleValue("Desk docked", Intent.EXTRA_DOCK_STATE_DESK));

        outputs.add(new IODescription(-1, DockedTrigger.STATE, "Docked State", set, "The new state of dock", true, samples));
        String[] tags = {"Dock", "Car", "Desk", "Undock", "On", "Off"};
        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String dockTriggerJSON = Library.makeJSON(-1, "com.appglue", DockedTrigger.class.getCanonicalName(),
                "Docked Trigger", "-> Dock",
                "Signals that the phone has been docked or undocked",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, null);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, dockTriggerJSON);
    }

    private String setupDeviceStorageTrigger() {
        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Low storage", DeviceStorageTrigger.LOW_STORAGE));
        samples.add(new SampleValue("Okay storage", DeviceStorageTrigger.OKAY_STORAGE));

        outputs.add(new IODescription(-1, DeviceStorageTrigger.STATE, "Device storage state", set, "The new state of the storage in the device", true, samples));
        String[] tags = {"Device storage", "SD card"};
        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String storageTriggerJSON = Library.makeJSON(-1, "com.appglue", DeviceStorageTrigger.class.getCanonicalName(),
                "Storage Trigger", "-> Storage",
                "Signals that the storage in the phone has changed",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, null);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, storageTriggerJSON);
    }

    private String setupScreenStateTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Screen On", true));
        samples.add(new SampleValue("Screen Off", false));

        outputs.add(new IODescription(-1, ScreenStateTrigger.STATE, "Screen state", set, "Whether the screen is now on or off", true, samples));

        String[] tags = {"Screen", "Display", "On", "Off"};
        String[] cats = {TRIGGERS, DEVICE_UTILS};

        String screenTriggerJSON = Library.makeJSON(-1, "com.appglue", ScreenStateTrigger.class.getCanonicalName(),
                "Screen Trigger", "-> Screen",
                "Signals that the screen has gone on or off",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, null);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, screenTriggerJSON);
    }

    private String setupWifiTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);
        IOType text = IOType.Factory.getType(IOType.Factory.TEXT);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("On", WifiTrigger.WIFI_ON));
        samples.add(new SampleValue("Off", WifiTrigger.WIFI_OFF));
        samples.add(new SampleValue("Connected", WifiTrigger.WIFI_CONNECTED));
        samples.add(new SampleValue("Disconnected", WifiTrigger.WIFI_DISCONNECTED));

        outputs.add(new IODescription(-1, WifiTrigger.STATE, "WiFi state", set, "Whether WiFi is now on or off", true, samples));
        String[] tags = {"Wifi", "Trigger", "On", "Off"};

        outputs.add(new IODescription(-1, WifiTrigger.NETWORK_SSID, "SSID", text, "The name of the network you've connected to", true, null));
        String[] cats = {TRIGGERS, NETWORK_UTILS};
        String[] features = { PackageManager.FEATURE_WIFI };

        String wifiTriggerJSON = Library.makeJSON(-1, "com.appglue", WifiTrigger.class.getCanonicalName(),
                "WiFi Trigger", "-> WiFi",
                "Signals that wifi has gone on or off",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, features);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, wifiTriggerJSON);
    }

    private String setupRingerStateTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.SET);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Loud", AudioManager.RINGER_MODE_NORMAL));
        samples.add(new SampleValue("Silent", AudioManager.RINGER_MODE_SILENT));
        samples.add(new SampleValue("Vibrate", AudioManager.RINGER_MODE_VIBRATE));

        outputs.add(new IODescription(-1, RingerTrigger.STATE, "Ringer state", set, "The new state of your ringer - silent, loud, or vibrate", true, samples));

        String[] tags = { "Ringer", "Vibrate", "Silent" };
        String[] cats = { TRIGGERS, DEVICE_UTILS };
        String[] features = { PackageManager.FEATURE_TELEPHONY };

        String ringerJSON = Library.makeJSON(-1, "com.appglue", RingerTrigger.class.getCanonicalName(),
                "Ringer Trigger", "-> Ringer",
                "Signals that the ringer has changed",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, features);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, ringerJSON);
    }

    private String setupMobileConnectionTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("Connected", true));
        samples.add(new SampleValue("Disconnected", false));

        outputs.add(new IODescription(-1, MobileConnectionTrigger.STATE, "Mobile network state", set, "The new state of the mobile network.", true, samples));

        String[] tags = { "Mobile", "Network", "On", "Off" };
        String[] cats = { TRIGGERS, NETWORK_UTILS };
        String[] features = { PackageManager.FEATURE_TELEPHONY };

        String mobileJSON = Library.makeJSON(-1, "com.appglue", MobileConnectionTrigger.class.getCanonicalName(),
                "Mobile Network Trigger", "-> Network",
                "Signals that the mobile network has changed",
                ComposableService.FLAG_TRIGGER,
                0, null, outputs, tags, cats, features);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, mobileJSON);
    }

    private String setupNFCTrigger() {

        ArrayList<IODescription> outputs = new ArrayList<IODescription>();
        IOType set = IOType.Factory.getType(IOType.Factory.BOOLEAN);

        ArrayList<SampleValue> samples = new ArrayList<SampleValue>();
        samples.add(new SampleValue("On", true));
        samples.add(new SampleValue("Off", false));

        outputs.add(new IODescription(-1, NFCTrigger.STATE, "NFC state", set, "The new state of the NFC thing.", true, samples));

        String[] tags = { "NFC", "On", "Off" };
        String[] cats = { TRIGGERS, DEVICE_UTILS };
        int version = Build.VERSION_CODES.JELLY_BEAN_MR2;
        String[] features = { PackageManager.FEATURE_NFC };

        String nfcJSON = Library.makeJSON(-1, "com.appglue", NFCTrigger.class.getCanonicalName(),
                "NFC Trigger", "-> NFC",
                "Signals that the NFC thing has changed",
                ComposableService.FLAG_TRIGGER,
                version, null, outputs, tags, cats, features);

        return String.format(Locale.US, "{\"%s\": {\"%s\":%s}}", JSON_SERVICE, JSON_SERVICE_DATA, nfcJSON);
    }

    // Activity recognition trigger -- Vehicle,Bicycle,On foot,Still,Tilting
    // Application launched or closed trigger
    // Bluetooth connected to... trigger
    // Call state trigger
    // Cell tower trigger
    // Location trigger
    // Sensor trigger -- Proximity, orientation, accelleration

    // Atooma
    // Internet
    // On/off
    // Time
    // Countdown alarm
    // Particular time
    // App installed
    // App uninstalled
    // Bluetooth
    // Device found
    // Data network
    // On/off
    // Standby
    // On/off
    // Camera -- Apparently this doesn't always work
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
