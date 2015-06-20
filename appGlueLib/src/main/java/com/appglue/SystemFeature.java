package com.appglue;

import android.content.pm.PackageManager;
import android.os.Build;
import android.util.SparseArray;

import com.appgluelib.appgluelib.R;

import java.util.ArrayList;

public class SystemFeature {

    public int index;
    public String code;
    public String name;
    public int icon;

    private static TST<SystemFeature> codeSearch;
    private static SparseArray<SystemFeature> indexSearch;
    private static ArrayList<SystemFeature> features;

    public static String INTERNET = "INTERNET";

    private SystemFeature(int index, String code, String name, int icon) {
        this.index = index;
        this.code = code;
        this.name = name;
        this.icon = icon;
    }

    public static SystemFeature getFeature(String code) {

        if(codeSearch == null || indexSearch == null || features == null) {
            create();
        }

        return codeSearch.get(code);
    }

    public static SystemFeature getFeature(int index) {

        if(codeSearch == null || indexSearch == null || features == null) {
            create();
        }

        return indexSearch.get(index);
    }

    public static ArrayList<SystemFeature> listAllFeatures() {

        if(codeSearch == null || indexSearch == null || features == null) {
            create();
        }

        return features;
    }

    private static void create() {

        codeSearch = new TST<>();
        indexSearch = new SparseArray<>();

        features = new ArrayList<>();

        features.add(new SystemFeature(0x1, PackageManager.FEATURE_BLUETOOTH, "Bluetooth", R.drawable.ic_bluetooth_black_24dp));
        features.add(new SystemFeature(0x2, PackageManager.FEATURE_CAMERA, "Rear camera", R.drawable.ic_camera_alt_black_24dp));
        features.add(new SystemFeature(0x4, PackageManager.FEATURE_CAMERA_FRONT, "Front camera", R.drawable.ic_camera_front_black_24dp));
        features.add(new SystemFeature(0x8, PackageManager.FEATURE_CAMERA_FLASH, "LED flash", R.drawable.ic_flare_black_24dp));
        features.add(new SystemFeature(0x10, PackageManager.FEATURE_LOCATION, "Location", R.drawable.ic_gps_not_fixed_black_24dp));
        features.add(new SystemFeature(0x20, PackageManager.FEATURE_MICROPHONE, "Microphone", R.drawable.ic_mic_black_24dp));
        features.add(new SystemFeature(0x40, PackageManager.FEATURE_NFC, "NFC", R.drawable.ic_nfc_black_24dp));
        features.add(new SystemFeature(0x80, PackageManager.FEATURE_SENSOR_ACCELEROMETER, "Accelerometer", R.drawable.ic_gesture_black_24dp));
        features.add(new SystemFeature(0x100, PackageManager.FEATURE_SENSOR_BAROMETER, "Barometer", R.drawable.ic_help_black_24dp));
        features.add(new SystemFeature(0x200, PackageManager.FEATURE_SENSOR_COMPASS, "Compass", R.drawable.ic_explore_black_24dp));
        features.add(new SystemFeature(0x400, PackageManager.FEATURE_SENSOR_GYROSCOPE, "Gyroscope", R.drawable.ic_screen_rotation_black_24dp));
        features.add(new SystemFeature(0x800, PackageManager.FEATURE_SENSOR_LIGHT, "Light Sensor", R.drawable.ic_brightness_medium_black_24dp));
        features.add(new SystemFeature(0x1000, PackageManager.FEATURE_SENSOR_PROXIMITY, "Proximity Sensor", R.drawable.ic_track_changes_black_24dp));
        features.add(new SystemFeature(0x2000, PackageManager.FEATURE_TELEPHONY, "Telephone", R.drawable.ic_call_black_24dp));
        features.add(new SystemFeature(0x4000, PackageManager.FEATURE_WIFI, "WiFi", R.drawable.ic_network_wifi_black_24dp));

        // API 18
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN_MR2) {
            features.add(new SystemFeature(0x8000, PackageManager.FEATURE_BLUETOOTH_LE, "Bluetooth Low Energy", R.drawable.ic_bluetooth_black_24dp));
        } else {
            features.add(new SystemFeature(0x8000, "android.hardware.bluetooth_le", "Bluetooth Low Energy", R.drawable.ic_bluetooth_black_24dp));
        }

        // API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            features.add(new SystemFeature(0x10000, PackageManager.FEATURE_CONSUMER_IR, "Infra Red", R.drawable.ic_settings_remote_black_24dp));
            features.add(new SystemFeature(0x20000, PackageManager.FEATURE_SENSOR_STEP_COUNTER, "Step Counter", R.drawable.ic_directions_walk_black_24dp));
        } else {
            features.add(new SystemFeature(0x10000, "android.hardware.consumerir", "Infra Red", R.drawable.ic_settings_remote_black_24dp));
            features.add(new SystemFeature(0x20000, "android.hardware.sensor.stepcounter", "Step Counter", R.drawable.ic_directions_walk_black_24dp));
        }

        // API 20
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            features.add(new SystemFeature(0x40000, PackageManager.FEATURE_SENSOR_HEART_RATE, "Heart Rate Sensor", R.drawable.ic_favorite_black_24dp));
        } else {
            features.add(new SystemFeature(0x40000, "android.hardware.sensor.heartrate", "Heart Rate Sensor", R.drawable.ic_favorite_black_24dp));
        }

        // API 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            features.add(new SystemFeature(0x80000, PackageManager.FEATURE_AUDIO_OUTPUT, "Audio output", R.drawable.ic_speaker_black_24dp));
            features.add(new SystemFeature(0x100000, PackageManager.FEATURE_LEANBACK, "TV", R.drawable.ic_tv_black_24dp));
            features.add(new SystemFeature(0x200000, PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE, "Temperature sensor", R.drawable.ic_help_black_24dp));
            features.add(new SystemFeature(0x400000, PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY, "Humidity Sensor", R.drawable.ic_invert_colors_black_24dp));
            features.add(new SystemFeature(0x800000, PackageManager.FEATURE_WATCH, "Watch", R.drawable.ic_watch_black_24dp));
        } else {
            features.add(new SystemFeature(0x80000, "android.hardware.audio.output", "Audio output", R.drawable.ic_speaker_black_24dp));
            features.add(new SystemFeature(0x100000, "android.software.leanback", "TV", R.drawable.ic_tv_black_24dp));
            features.add(new SystemFeature(0x200000, "android.hardware.sensor.ambient_temperature", "Temperature sensor", R.drawable.ic_help_black_24dp));
            features.add(new SystemFeature(0x400000, "android.hardware.sensor.relative_humidity", "Humidity Sensor", R.drawable.ic_invert_colors_black_24dp));
            features.add(new SystemFeature(0x800000, "android.hardware.type.watch", "Watch", R.drawable.ic_watch_black_24dp));
        }

        features.add(new SystemFeature(0x1000000, INTERNET, "Internet", R.drawable.ic_favorite_black_24dp));

        for (SystemFeature f : features) {
            indexSearch.put(f.index, f);
            codeSearch.put(f.code, f);
        }
    }
}