package com.appglue.library;

import static com.appglue.Constants.DIR_ICON;
import static com.appglue.Constants.TAG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.appglue.Library;

public class LocalStorage
{
	private static LocalStorage localStorage = null;
	
	private String location;
	
	private HashMap<String, Bitmap> icons;
	
	private LocalStorage()
	{
		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		File f = new File(root + DIR_ICON);
		if(!f.isDirectory())
			f.mkdirs();
		
		location = root + DIR_ICON;
		icons = new HashMap<String, Bitmap>();
	}
	
	public static LocalStorage getInstance()
	{
		if(localStorage == null)
		{
			localStorage = new LocalStorage();
		}
		
		return localStorage;
	}
	
	public String writeIcon(String packageName, Bitmap img) throws IOException, FileNotFoundException
	{
		String filename = String.format("%sicon_%s.png", location, packageName);
		File f = new File(filename);
		if(f.exists())
			return filename;
		
		FileOutputStream fOut = new FileOutputStream(filename);
		
		if(img == null)
		{
			Log.e(TAG, "img null in Write Icon");
			return "";
		}
		
		if(!icons.containsKey(filename))
			icons.put(filename, img);
		
		img.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		fOut.flush();
		fOut.close();
		
		return filename;
	}
	
	public String writeIcon(String packageName, String iconString) throws IOException, FileNotFoundException
	{
		Bitmap img = Library.stringToBitmap(iconString);
		return writeIcon(packageName, img);
	}
	
	public Bitmap readIcon(String filename) throws IOException
	{
		if(icons.containsKey(filename))
			return icons.get(filename);
		
		Bitmap b =  BitmapFactory.decodeFile(filename);
		icons.put(filename, b);
		return b;
	}
	
}
