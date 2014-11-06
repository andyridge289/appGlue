package com.appglue;

import android.content.pm.PackageManager;
import android.os.Build;
import android.util.SparseArray;

import java.util.ArrayList;

public class SystemFeature {

    public int index;
    public String code;
    public String name;

    private static TST<SystemFeature> codeSearch;
    private static SparseArray<SystemFeature> indexSearch;
    private static ArrayList<SystemFeature> features;

    public static String INTERNET = "INTERNET";

    private SystemFeature(int index, String code, String name) {
        this.index = index;
        this.code = code;
        this.name = name;
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

        codeSearch = new TST<SystemFeature>();
        indexSearch = new SparseArray<SystemFeature>();

        features = new ArrayList<SystemFeature>();

        features.add(new SystemFeature(0x1, PackageManager.FEATURE_BLUETOOTH, "Bluetooth"));
        features.add(new SystemFeature(0x2, PackageManager.FEATURE_CAMERA, "Rear camera"));
        features.add(new SystemFeature(0x4, PackageManager.FEATURE_CAMERA_FRONT, "Front camera"));
        features.add(new SystemFeature(0x8, PackageManager.FEATURE_CAMERA_FLASH, "LED flash"));
        features.add(new SystemFeature(0x10, PackageManager.FEATURE_LOCATION, "Location"));
        features.add(new SystemFeature(0x20, PackageManager.FEATURE_MICROPHONE, "Microphone"));
        features.add(new SystemFeature(0x40, PackageManager.FEATURE_NFC, "NFC"));
        features.add(new SystemFeature(0x80, PackageManager.FEATURE_SENSOR_ACCELEROMETER, "Accelerometer"));
        features.add(new SystemFeature(0x100, PackageManager.FEATURE_SENSOR_BAROMETER, "Barometer"));
        features.add(new SystemFeature(0x200, PackageManager.FEATURE_SENSOR_COMPASS, "Compass"));
        features.add(new SystemFeature(0x400, PackageManager.FEATURE_SENSOR_GYROSCOPE, "Gyroscope"));
        features.add(new SystemFeature(0x800, PackageManager.FEATURE_SENSOR_LIGHT, "Light Sensor"));
        features.add(new SystemFeature(0x1000, PackageManager.FEATURE_SENSOR_PROXIMITY, "Proximity Sensor"));
        features.add(new SystemFeature(0x2000, PackageManager.FEATURE_TELEPHONY, "Telephone"));
        features.add(new SystemFeature(0x4000, PackageManager.FEATURE_WIFI, "WiFi"));

        // API 18
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN_MR2) {
            features.add(new SystemFeature(0x8000, PackageManager.FEATURE_BLUETOOTH_LE, "Bluetooth Low Energy"));
        } else {
            features.add(new SystemFeature(0x8000, "android.hardware.bluetooth_le", "Bluetooth Low Energy"));
        }

        // API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            features.add(new SystemFeature(0x10000, PackageManager.FEATURE_CONSUMER_IR, "Infra Red"));
            features.add(new SystemFeature(0x20000, PackageManager.FEATURE_SENSOR_STEP_COUNTER, "Step Counter"));
        } else {
            features.add(new SystemFeature(0x10000, "android.hardware.consumerir", "Infra Red"));
            features.add(new SystemFeature(0x20000, "android.hardware.sensor.stepcounter", "Step Counter"));
        }

        // API 20
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            features.add(new SystemFeature(0x40000, PackageManager.FEATURE_SENSOR_HEART_RATE, "Heart Rate Sensor"));
        } else {
            features.add(new SystemFeature(0x40000, "android.hardware.sensor.heartrate", "Heart Rate Sensor"));
        }

        // API 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            features.add(new SystemFeature(0x80000, PackageManager.FEATURE_AUDIO_OUTPUT, "Audio output"));
            features.add(new SystemFeature(0x100000, PackageManager.FEATURE_LEANBACK, "TV"));
            features.add(new SystemFeature(0x200000, PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE, "Temperature sensor"));
            features.add(new SystemFeature(0x400000, PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY, "Humidity Sensor"));
            features.add(new SystemFeature(0x800000, PackageManager.FEATURE_WATCH, "Watch"));
        } else {
            features.add(new SystemFeature(0x80000, "android.hardware.audio.output", "Audio output"));
            features.add(new SystemFeature(0x100000, "android.software.leanback", "TV"));
            features.add(new SystemFeature(0x200000, "android.hardware.sensor.ambient_temperature", "Temperature sensor"));
            features.add(new SystemFeature(0x400000, "android.hardware.sensor.relative_humidity", "Humidity Sensor"));
            features.add(new SystemFeature(0x800000, "android.hardware.type.watch", "Watch"));
        }

        features.add(new SystemFeature(0x1000000, INTERNET, "Internet"));

        for (SystemFeature f : features) {
            indexSearch.put(f.index, f);
            codeSearch.put(f.code, f);
        }
    }
}