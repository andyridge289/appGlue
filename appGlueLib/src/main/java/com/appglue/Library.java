package com.appglue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INPUTS;
import static com.appglue.Constants.INPUT_DESCRIPTION;
import static com.appglue.Constants.INPUT_NAME;
import static com.appglue.Constants.INPUT_TYPE;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.OUTPUTS;
import static com.appglue.Constants.OUTPUT_DESCRIPTION;
import static com.appglue.Constants.OUTPUT_NAME;
import static com.appglue.Constants.OUTPUT_TYPE;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.FLAGS;
import static com.appglue.Constants.SAMPLES;
import static com.appglue.Constants.SAMPLE_NAME;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SHORT_NAME;
import static com.appglue.Constants.TAGS;

public class Library {
    public static String makeJSON(int id, String packageName, String className, String name, String shortName, String description,
                                  int flags, int price, ArrayList<IODescription> inputList, ArrayList<IODescription> outputList, String[] tags) {

        String first = String.format(Locale.getDefault(), "{\"%s\": %d, \"%s\": \"%s\", " +
                        "\"%s\": \"%s\", \"%s\":\"%s\", \"%s\":\"%s\", " +
                        "\"%s\":\"%s\", \"%s\": %d, ",
                ID, id, PACKAGENAME, packageName,
                CLASSNAME, className, NAME, name, SHORT_NAME, shortName,
                DESCRIPTION, description, FLAGS, flags);

        inputList = inputList == null ? new ArrayList<IODescription>() : inputList;
        outputList = outputList == null ? new ArrayList<IODescription>() : outputList;

        StringBuilder inputBuilder = new StringBuilder();
        inputBuilder.append(String.format("\"%s\": [", INPUTS));
        for (int i = 0; i < inputList.size(); i++) {
            IODescription input = inputList.get(i);
            IOType type = input.getType();

            StringBuilder sampleBuilder = new StringBuilder();
            ArrayList<SampleValue> values = input.getSampleValues();
            sampleBuilder.append("[");

            if (values != null) {
                for (int j = 0; j < values.size(); j++) {
                    if (j > 0) sampleBuilder.append(",");

                    SampleValue value = values.get(j);
                    sampleBuilder.append(String.format("{ \"%s\": \"%s\", \"%s\":\"%s\"}", SAMPLE_NAME, value.getName(), SAMPLE_VALUE, value.getValue().toString()));
                }
            }

            sampleBuilder.append("]");

            inputBuilder.append(String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\": %b, \"%s\": %s}",
                    INPUT_NAME, input.getName(), FRIENDLY_NAME, input.getFriendlyName(), INPUT_TYPE, type.getName(), CLASSNAME, type.getClass().getCanonicalName(),
                    INPUT_DESCRIPTION, input.description(), MANDATORY, input.isMandatory(), SAMPLES, sampleBuilder.toString()));

            if (i < inputList.size() - 1)
                inputBuilder.append(",");
        }
        inputBuilder.append("],");
        String inputs = inputBuilder.toString();

        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(String.format("\"%s\": [", OUTPUTS));
        for (int i = 0; i < outputList.size(); i++) {
            IODescription output = outputList.get(i);
            IOType type = output.getType();

            StringBuilder sampleBuilder = new StringBuilder();
            ArrayList<SampleValue> values = output.getSampleValues();
            sampleBuilder.append("[");

            if (values != null) {
                for (int j = 0; j < values.size(); j++) {
                    if (j > 0) sampleBuilder.append(",");

                    SampleValue value = values.get(j);
                    sampleBuilder.append(String.format("{ \"%s\": \"%s\", \"%s\":\"%s\"}", SAMPLE_NAME, value.getName(), SAMPLE_VALUE, value.getValue().toString()));
                }
            }

            sampleBuilder.append("]");

            outputBuilder.append(String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\": %b, \"%s\": %s}",
                    OUTPUT_NAME, output.getName(), FRIENDLY_NAME, output.getFriendlyName(), OUTPUT_TYPE, type.getName(), CLASSNAME, type.getClass().getCanonicalName(), OUTPUT_DESCRIPTION, output.description(), MANDATORY, false, SAMPLES, sampleBuilder.toString()));

            if (i < outputList.size() - 1)
                outputBuilder.append(",");
        }
        outputBuilder.append("]");
        String outputs = outputBuilder.toString();

        StringBuilder tagBuilder = new StringBuilder(String.format(",\"%s\":[", TAGS));
        for (int i = 0; i < tags.length; i++) {
            if (i > 0)
                tagBuilder.append(",");

            tagBuilder.append(String.format("\"%s\"", tags[i]));
        }
        tagBuilder.append("]");

        return first + inputs + outputs + tagBuilder.toString() + "}";
    }

    public static String printBundle(Bundle bundle) {
        if (bundle == null)
            return "bundle null";

        StringBuilder bundleString = new StringBuilder();

        Set<String> keys = bundle.keySet();

        int count = 0;

        for (String key : keys) {

            count++;

            Object thing = bundle.get(key);

            if (thing == null)
                continue;

            Class<?> className = thing.getClass();

            if (className.equals(Bundle.class)) {
                bundleString.append(String.format("%s: {%s}    ", key, printBundle((Bundle) thing)));
            } else if (className.equals(ArrayList.class)) {
                bundleString.append(String.format("%s: [", key));

                @SuppressWarnings("unchecked")
                ArrayList<Object> things = (ArrayList<Object>) thing;

                for (int i = 0; i < things.size(); i++) {
                    bundleString.append(printBundle((Bundle) things.get(i)));
                    bundleString.append(i < things.size() - 1 ? "," : "");
                }
                bundleString.append("]");
            } else if (className.equals(String.class)) {
                bundleString.append(String.format("%s: %s    ", key, bundle.getString(key)));
            } else if (className.equals(Integer.class)) {
                bundleString.append(String.format("%s: %d    ", key, bundle.getInt(key)));
            } else {
                bundleString.append(String.format("%s: %s    ", key, className));
            }
        }

        if (count == 0) {
            bundleString.append("Bundle empty");
        }

        return bundleString.toString();
    }

    public static String implode(String[] data, String delimeter, boolean addQuotes) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            if (addQuotes)
                out.append("\"").append(data[i]).append("\"").append(i < data.length - 1 ? delimeter : "");
            else
                out.append(data[i]).append(i < data.length - 1 ? delimeter : "");
        }

        return out.toString();
    }

    public static String drawableToString(Drawable d) {
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] bytes = stream.toByteArray();
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) bytes[i];
        }

        return new String(chars);
    }

    public static Drawable stringToDrawable(Context context, String s) {
        char[] chars = s.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return new BitmapDrawable(context.getResources(), b);
    }

    public static Bitmap stringToBitmap(String s) {
        char[] chars = s.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
