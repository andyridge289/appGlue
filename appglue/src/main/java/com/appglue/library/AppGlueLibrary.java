package com.appglue.library;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.appglue.description.datatypes.IOType;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;

import org.json.JSONException;
import org.json.JSONObject;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class AppGlueLibrary {
    public static String createTableString(String tableName, String[][] cols) {
        StringBuilder createTable = new StringBuilder(String.format("CREATE TABLE %s (", tableName));

        int length = cols.length - 1;
        for (int i = 0; i < length; i++) {
            createTable.append(String.format("%s %s,", cols[i][0], cols[i][1]));
        }
        createTable.append(String.format("%s %s)", cols[length][0], cols[length][1]));

        return createTable.toString();
    }

    public static String createIndexString(String tableName, String indexName, String[] cols) {
        StringBuilder createIndex = new StringBuilder(String.format("CREATE INDEX IF NOT EXISTS %s ON %s (",
                indexName,
                tableName));

        for (int i = 0; i < cols.length; i++) {
            if (i > 0)
                createIndex.append(",");
            createIndex.append(cols[i]);
        }

        return createIndex.append(")").toString();
    }

    /**
     * Builds a string to get all of the given columns in the given table.
     *
     * @param table   The table to get everything out of
     * @param columns The columns of said table
     * @return A string of SQL
     */
    public static String buildGetAllString(String table, String[][] columns) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            out.append(String.format("%s.%s AS %s_%s", table, columns[i][0], table, columns[i][0])).append(i < columns.length - 1 ? ", " : " ");
        }

        return out.toString();
    }

    public static String bundleToJSON(Bundle data, ServiceDescription component, boolean input) {
        Set<String> keys = data.keySet();
        JSONObject json = new JSONObject();
        try {
            for (String key : keys) {
                IOType type = input ? component.getInput(key).getType() : component.getOutput(key).getType();
                String stringThing = type.toString(type.getFromBundle(data, key, "ARGH FAIL"));
                json.put(key, stringThing);
            }
        } catch(JSONException e) {
            Log.e(TAG, "bundle to JSON string Fail");
            // TODO Put something in the log maybe?
        }
        return json.toString();
    }

    /**
     * The positions should only go up as far as the size.
     * i.e. the indexes and the keys should be the same set but might be in a different order.
     *
     * @param sparse The sparse array to convert
     * @return The resultant ArrayList
     */
    public static ArrayList<ServiceDescription> sparseToList(SparseArray<ServiceDescription> sparse) {

        ArrayList<ServiceDescription> list = new ArrayList<ServiceDescription>();

        for(int i = 0; i < sparse.size(); i++) {
            list.add(sparse.get(i));
        }

        return list;
    }

    public static String bundleToString(Bundle bundle) {
        String string = "Bundle {";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " } ";
        return string;
    }

    public static boolean bundlesEqual(Bundle a, Bundle b) {

        Set<String> aKeys = a.keySet();
        Set<String> bKeys = b.keySet();

        if (!aKeys.containsAll(bKeys) || !bKeys.containsAll(aKeys)) {
            Log.d(TAG, "Bundle->equals: missing keys");
            return false;
        }

        for (String key : aKeys) {

            Object o = a.get(key);
            Object p = b.get(key);

            if(o instanceof Bundle) {
                if(!bundlesEqual((Bundle) o, (Bundle) p)) {
                    if(LOG) Log.d(TAG, "Bundle->equals: Bundles " + key + " not same");
                    return false;
                }
            }

            if(o instanceof ArrayList) {

                ArrayList al = (ArrayList) o;
                ArrayList bl = (ArrayList) p;

                if(al.size() != bl.size()) {
                    if(LOG) Log.d(TAG, "Bundle->equals: " + key + " not same size");
                    return false;
                }

                for(int i = 0; i < al.size(); i++) {

                    Object q = al.get(i);
                    Object r = bl.get(i);

                    if(q instanceof Bundle) {
                        if(!bundlesEqual((Bundle) q, (Bundle) r)) {
                            if(LOG) Log.d(TAG, "Bundle->equals: Bundles in ArrayList -- " + key + " not same");
                            return false;
                        }
                    } else {
                        if(!q.equals(r)) {
                            if(LOG) Log.d(TAG, "Bundle->equals: Objects in ArrayList -- " + key + "[" + i + "] not same");
                            return false;
                        }
                    }
                }

            }

            if (!a.get(key).equals(b.get(key))) {
                if(LOG) Log.d(TAG, "Bundle->equals: " + key + " not same");
                return false;
            }
        }

        return true;
    }


    public static Bundle JSONToBundle(String stringJSON, ServiceDescription component, boolean input) {

        Bundle b = new Bundle();

        try {
            JSONObject json = new JSONObject(stringJSON);
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                IOType type = input ? component.getInput(key).getType() : component.getOutput(key).getType();
                type.addToBundle(b, type.fromString(json.getString(key)), key);
            }

        } catch(JSONException e) {
            Log.e(TAG, "JSON string to bundle Fail FUCKSTICKS");
        }

        return b;
    }
}
