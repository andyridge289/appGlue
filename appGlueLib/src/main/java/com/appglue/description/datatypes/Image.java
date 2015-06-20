package com.appglue.description.datatypes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Image extends IOType {

    private String location;

    // Each image should only be needed for a short time so keeping 20 of them should be ample
    private final int SIZE = 20;

    public Image() {
        super();

        this.name = "Image";
        this.className = Image.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
        this.acceptsManual = false;
        this.location = Environment.getExternalStorageDirectory().toString() + "/appGlue/imageTemp";
    }

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defaultValue) {

        String filename = bundle.getString(key);
        if (filename == null)
            filename = (String) defaultValue;

        return loadFile(filename);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {

        // Find a filename
        String filename = getFilename();

        if (!(o instanceof Bitmap)) {
            Logger.e("This is a problem");
        }

        // Write the bitmap to the file
        writeFile(filename, (Bitmap) o);

        // Add the filename to the bundle
        b.putString(key, filename);
    }

    private String getFilename() {

        String filename = location + "/imagetemp_";

        File f = new File(location);
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs.length < SIZE) {
                // think up a new one
                return filename + fs.length + ".png";
            } else {
                // Find the oldest one
                File min = fs[0];
                int index = 0;
                for (int i = 1; i < fs.length; i++) {
                    if (fs[i].lastModified() < min.lastModified()) {
                        min = fs[i];
                        index = i;
                    }
                }
                return filename + index + ".png";
            }

        } else {
            Logger.e("It isn't a directory");
            return filename + "0.png";
        }
    }


    private Bitmap loadFile(String filename) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, 1000, 1000);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    private void writeFile(String filename, Bitmap bitmap) {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public String toString(Object value) {

        // Check if it's actually a bitmap
        if (!(value instanceof Bitmap)) {
            return "";
        }

        // write it to a file
        String filename = getFilename();
        writeFile(filename, (Bitmap) value);
        return filename;
    }

    public Object fromString(String value) {
        // read it from a file
        return loadFile(value);
    }

    public boolean compare(Object a, Object b) {
        return a.equals(b);
    }
}
