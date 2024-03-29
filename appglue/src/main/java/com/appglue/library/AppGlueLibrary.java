package com.appglue.library;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appglue.ComposableService;
import com.appglue.R;
import com.appglue.description.ServiceDescription;
import com.appglue.description.IOType;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppGlueLibrary {

    public static float dpToPx(Resources r, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static String createTableString(String tableName, String[][] cols, String[][] fk) {
        StringBuilder createTable = new StringBuilder(String.format("CREATE TABLE %s (", tableName));

        int length = cols.length - 1;
        for (int i = 0; i < length; i++) {
            createTable.append(String.format("%s %s,", cols[i][0], cols[i][1]));
        }
        createTable.append(String.format("%s %s", cols[length][0], cols[length][1]));

        if (fk != null) {

            for (String[] aFk : fk) {

                createTable.append(String.format(", FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE",
                        aFk[0], aFk[1], aFk[2]));
            }
        }

        createTable.append(")");

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
        } catch (JSONException e) {
            Logger.e("bundle to JSON string Fail");
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

        for (int i = 0; i < sparse.size(); i++) {
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
            Logger.d("Bundle->equals: missing keys");
            return false;
        }

        for (String key : aKeys) {

            Object o = a.get(key);
            Object p = b.get(key);

            if (o instanceof Bundle) {
                if (!bundlesEqual((Bundle) o, (Bundle) p)) {
                   Logger.d("Bundle->equals: Bundles " + key + " not same");
                    return false;
                }
            }

            if (o instanceof ArrayList) {

                ArrayList al = (ArrayList) o;
                ArrayList bl = (ArrayList) p;

                if (al.size() != bl.size()) {
                   Logger.d("Bundle->equals: " + key + " not same size");
                    return false;
                }

                for (int i = 0; i < al.size(); i++) {

                    Object q = al.get(i);
                    Object r = bl.get(i);

                    if (q instanceof Bundle) {
                        if (!bundlesEqual((Bundle) q, (Bundle) r)) {

                                Logger.d("Bundle->equals: Bundles in ArrayList -- " + key + " not same");
                            return false;
                        }
                    } else {
                        if (!q.equals(r)) {

                                Logger.d("Bundle->equals: Objects in ArrayList -- " + key + "[" + i + "] not same");
                            return false;
                        }
                    }
                }

            }

            if (!a.get(key).equals(b.get(key))) {
               Logger.d("Bundle->equals: " + key + " not same");
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
            while (keys.hasNext()) {
                String key = keys.next();
                IOType type = input ? component.getInput(key).getType() : component.getOutput(key).getType();
                type.addToBundle(b, type.fromString(json.getString(key)), key);
            }

        } catch (JSONException e) {
            Logger.e("JSON string to bundle Fail FUCKSTICKS");
        }

        return b;
    }

    private static String[] randomNames = new String[]{
            "Benjamin", //"Kira", "Worf", "Jadzia",
            "Julian", "Miles", "Jake", //"Quark", "Odo", "Jean-Luc",
            "William", //"Deanna",
            "Beverley", //"Data", "Geordi",
            "Katherine", // "Chakotay", "Tuvok", "B'Elanna",
            "Tom", "Harry", "Doctor", // "Neelix", "Kes", "Seven",
            "Jack", "Daniel", "Samantha", //"Teal'c",
            "George", "John", "Elizabeth", "Richard", "Rodney", "Mal", "Zoe", //"Hoban", "Inara",
            "Jayne", "Kaylee", "Simon", //"River", "Derrial", "Serena", "Blair", "Dan", "Nate", "Jenny", "Chuck", "Vanessa", "Ivy", "Lily", "Rufus"
    };

    public static String generateRandomName() {
        Random random = new Random(System.currentTimeMillis());
        int index = Math.abs(random.nextInt()) % randomNames.length;
        return randomNames[index];
    }

    public static Bitmap scaleBitmapDIP(Context context, Bitmap bmp, int maxWidth, int maxHeight) {

        int w = bmp.getWidth();
        int h = bmp.getHeight();

        float newWidth;
        float newHeight;

        if (w > h) {
            // w needs to be resized to maxWidth
            newWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidth, context.getResources().getDisplayMetrics());

            // h needs to be resized to the appropriate proportion
            int propHeight = (maxWidth / w) * h;
            newHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, propHeight, context.getResources().getDisplayMetrics());

        } else if (h > w) {
            // h needs to be resized to maxHeight
            newHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxHeight, context.getResources().getDisplayMetrics());

            // w needs to be resized to the appropriate proportion
            int propWidth = (maxHeight / w) * h;
            newWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, propWidth, context.getResources().getDisplayMetrics());

        } else {
            // w needs to be resized to maxWidth and h needs to be resized to maxHeight
            newWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidth, context.getResources().getDisplayMetrics());
            newHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxHeight, context.getResources().getDisplayMetrics());
        }

        return Bitmap.createScaledBitmap(bmp, (int) newWidth, (int) newHeight, false);

    }

    public static Pair<String, ArrayList<String>> getContact(Context context, Intent data) {
        Uri result = data.getData();
        String id = result.getLastPathSegment();

        String whereName = ContactsContract.Data.CONTACT_ID + " = ?";
        String[] whereNameParams = new String[]{"" + id};
        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
        c.moveToFirst();

        String name = null;
        ArrayList<String> numbers = new ArrayList<String>();

        do {
            if (name == null) {
                name = c.getString(c.getColumnIndex("display_name"));
            }


            String num = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (num != null && !num.equals("") && !numbers.contains(num)) {
                numbers.add(num);

            }
        } while (c.moveToNext());

        numbers = AppGlueLibrary.validatePhoneNumbers(numbers);

        for (String number : numbers)
            Logger.d(name + ", " + number);

        return new Pair<String, ArrayList<String>>(name, numbers);
    }

    public static ArrayList<String> validatePhoneNumbers(ArrayList<String> numbers) {
        for (int i = 0; i < numbers.size(); ) {

            if (validPhoneNumber(numbers.get(i)))
                i++;
            else
                numbers.remove(i);
        }

        return numbers;
    }

    // http://blog.stevenlevithan.com/archives/validate-phone-number#r4-3
    private static boolean validPhoneNumber(String number) {
        String expression = "^\\+(?:[0-9] ?){6,14}[0-9]$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    public static String getContactName(Context context, String phoneNumber) {
        String whereName = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
        String[] whereNameParams = new String[]{phoneNumber};
        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
        c.moveToFirst();

        String name = null;

        do {
            if (name == null) {
                name = c.getString(c.getColumnIndex("display_name"));
            }
        } while (c.moveToNext());

        return name;
    }

    private static View makeFlag(LayoutInflater vi, String name, int icon, boolean expand, boolean enabled) {
        View vv = vi.inflate(R.layout.component_attribute, null);

        View bg = vv.findViewById(R.id.attr_icon);
        if (enabled) {
            bg.setBackgroundResource(R.drawable.component_attribute_icon);
        } else {
            bg.setBackgroundResource(R.drawable.component_attribute_icon_off);
        }

        ImageView iv = (ImageView) vv.findViewById(R.id.component_attribute_icon);
        iv.setBackgroundResource(icon);

        TextView tv = (TextView) vv.findViewById(R.id.component_attribute_text);
        tv.setText(name);
        if (expand) {
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }

        return vv;
    }

    public static void addFlagsToLayout(LinearLayout flagContainer, ServiceDescription sd, LayoutInflater vi, boolean expand, boolean enabled) {

        flagContainer.removeAllViews();

        if (sd.hasFlag(ComposableService.FLAG_TRIGGER)) {
            View vv = makeFlag(vi, "Trigger", R.drawable.ic_exit_to_app_white_18dp, expand, enabled);
            flagContainer.addView(vv);
        }

        if (sd.hasFlag(ComposableService.FLAG_MONEY)) {
            View vv = makeFlag(vi, "Costs money", R.drawable.ic_money, expand, enabled);
            flagContainer.addView(vv);
        }

        if (sd.hasFlag(ComposableService.FLAG_NETWORK)) {
            View vv = makeFlag(vi, "Uses data", R.drawable.ic_settings_input_antenna_white_18dp, expand, enabled);
            flagContainer.addView(vv);
        }

        if (sd.hasFlag(ComposableService.FLAG_LOCATION)) {
            View vv = makeFlag(vi, "Uses GPS", R.drawable.ic_my_location_white_18dp, expand, enabled);
            flagContainer.addView(vv);
        }

        if (sd.hasFlag(ComposableService.FLAG_DELAY)) {
            View vv = makeFlag(vi, "Delay", R.drawable.ic_timelapse_white_18dp, expand, enabled);
            flagContainer.addView(vv);
        }

        if (sd.hasFlag(ComposableService.FLAG_STORAGE)) {
            View vv = makeFlag(vi, "Uses Storage", R.drawable.ic_save_white_18dp, expand, enabled);
            flagContainer.addView(vv);
        }
    }

    public static String getVersionName(int sdk) {

        switch(sdk) {
            case 1:
                return "1.0";
            case 2:
                return "1.1";
            case 3:
                return "1.5";
            case 4:
                return "1.6";
            case 5:
                return "2.0";
            case 6:
                return "2.0.1";
            case 7:
                return "2.1";
            case 8:
                return "2.2";
            case 9:
                return "2.3";
            case 10:
                return "2.3.3";
            case 11:
                return "3.0";
            case 12:
                return "3.1";
            case 13:
                return "3.2";
            case 14:
                return "4.0";
            case 15:
                return "4.0.3";
            case 16:
                return "4.1";
            case 17:
                return "4.2";
            case 18:
                return "4.3";
            case 19:
                return "4.4";
            default:
                return "5.0";
        }
    }
}
