package com.appglue.layout;

import static com.appglue.Constants.FULL_ALPHA;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_SET_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.ActivityWiring;
import com.appglue.IOValue;
import com.appglue.R;
import com.appglue.ServiceIO;
import com.appglue.datatypes.IOType;
import com.appglue.datatypes.Set;
import com.appglue.description.ServiceDescription;
import com.appglue.layout.dialog.DialogApp;
import com.appglue.layout.dialog.DialogConnection;
import com.appglue.layout.dialog.DialogFilter;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.library.IOFilter;
import com.appglue.library.IOFilter.FilterValue;
import com.appglue.serviceregistry.Registry;

public class WiringMap extends LinearLayout implements Comparator<ServiceIO>
{
	private ServiceDescription first;
	private ServiceDescription second;
	
	private ListView outputList;
	private ListView inputList;
	
	// I don'think we actually care what these are
	private View outputContainer;
	private View inputContainer;
	private View noOutputs;
	private View noInputs;
	private View addOutput;
	private View addInput;
	
	private static final int LOWLIGHT_ALPHA = 10;
	private static final int HIGHLIGHT_ALPHA = 5;
	private static final int BASE_ALPHA = 2;
	
	private ActivityWiring activity;
	
	private ServiceIO iSelected;
	private int iIndex;
	
	private ServiceIO oSelected;
	private int oIndex;
	
	HashMap<String, Integer> hueMap;
	private Registry registry;
	
	private ArrayList<Point> connections;

    public WiringMap(Context context)
	{
		super(context);
		
		create(context);
	}
	
	public WiringMap(Context context, AttributeSet attributes)
	{
		super(context, attributes);
		create(context);
	}
	
	public WiringMap(Context context, AttributeSet attributes, int defStyle)
	{
		super(context, attributes, defStyle);
		create(context);
	}
	
	public void create(Context context)
	{
		this.setWillNotDraw(false);
		this.activity = (ActivityWiring) context;
		this.addView(View.inflate(context, R.layout.wiring_map, null));
		registry = Registry.getInstance(context);
		
		outputList = (ListView) findViewById(R.id.output_list);
		outputList.setClickable(false);
		
		inputList = (ListView) findViewById(R.id.input_list);
		inputList.setClickable(false);
		
		outputContainer = findViewById(R.id.outputs);
		inputContainer = findViewById(R.id.inputs);
		
		noOutputs = findViewById(R.id.no_outputs);
		noInputs = findViewById(R.id.no_inputs);
		
		addOutput = findViewById(R.id.add_output);
		addInput = findViewById(R.id.add_input);
		
		
		connections = new ArrayList<Point>();
		
		iIndex = -1;
		oIndex = -1;
	}
	
//	public ArrayList<Point> getConnections()
//	{
//		return connections;
//	}
	
	public ServiceDescription getFirst() {
		return first;
	}

	public void setFirst(ServiceDescription first) {
		this.first = first;
	}

	public ServiceDescription getSecond() {
		return second;
	}

	public void setSecond(ServiceDescription second) {
		this.second = second;
	}

	public void removeConnection(Point p) 
	{
		for(int i = 0; i < connections.size(); i++)
		{
			Point q = connections.get(i);
			
			if(p.x == q.x && p.y == q.y)
			{
				// Remove it and decrement so that we carry on checking. Although we could just return...
				connections.remove(i);
				i--;
			}
		}
		
	}
	
	public ArrayList<Point> getConnectionsOut(int outputIndex)
	{
		ArrayList<Point> points = new ArrayList<Point>();

        for (Point connection : connections) {
            if (connection.x == outputIndex)
                points.add(connection);
        }
		
		return points;
	}
	
	public ArrayList<Point> getConnectionsIn(int inputIndex)
	{
		ArrayList<Point> points = new ArrayList<Point>();

        for (Point connection : connections) {
            if (connection.y == inputIndex)
                points.add(connection);
        }
		
		return points;
	}
	
	private boolean checkConnection(int oIndex, int iIndex) {

        for (Point p : connections) {
            if (p.x == oIndex && p.y == iIndex)
                return true;
        }
		
		return false;
	}
	
	private boolean inputConnection(int index) 
	{
        for (Point connection : connections) {
            if (connection.y == index)
                return true;
        }
		
		return false;
	}

	
	public void set(ServiceDescription first, ServiceDescription second)
	{
		this.first = first;
		if(first != null)
		{
			ArrayList<ServiceIO> outputs = first.getOutputs();
			if(outputs.size() > 0)
			{
				// There are outputs, show the list, hide the none and the add
				outputList.setAdapter(new OutputAdapter(activity, R.layout.list_item_wiring_out, outputs));
				outputContainer.setVisibility(View.VISIBLE);
				noOutputs.setVisibility(View.INVISIBLE);
				addOutput.setVisibility(View.INVISIBLE);
			}
			else
			{
				// There are no inputs, show the none, hide the list and the add
				outputContainer.setVisibility(View.INVISIBLE);
				noOutputs.setVisibility(View.VISIBLE);
				addOutput.setVisibility(View.INVISIBLE);
			}
		}
		else
		{
			// There isn't a service here. Hide the list and the text that says NONE
			outputContainer.setVisibility(View.INVISIBLE);
			noOutputs.setVisibility(View.INVISIBLE);
			addOutput.setVisibility(View.VISIBLE);
		}
		
		this.second = second;
		if(second != null)
		{
			ArrayList<ServiceIO> inputs = second.getInputs();
			if(inputs.size() > 0)
			{
				inputList.setAdapter(new InputAdapter(activity, R.layout.list_item_wiring_in, inputs));
				inputContainer.setVisibility(View.VISIBLE);
				noInputs.setVisibility(View.INVISIBLE);
				addInput.setVisibility(View.INVISIBLE);
			}
				
			else
			{
				inputContainer.setVisibility(View.INVISIBLE);
				noInputs.setVisibility(View.VISIBLE);
				addInput.setVisibility(View.INVISIBLE);
			}
		}
		else
		{
			inputContainer.setVisibility(View.INVISIBLE);
			noInputs.setVisibility(View.INVISIBLE);
			addInput.setVisibility(View.VISIBLE);
		}
		
		// Check if there are any set things.
		if(second != null && second.getInputs() != null)
		{
			ArrayList<ServiceIO> in = second.getInputs();
			for(int i = 0; i < in.size(); i++)
			{
				ServiceIO connection = in.get(i).getConnection();
				if(connection != null)
				{
					// It's connected to something so work out what position the other thing is in the ouputs
					connections.add(new Point(connection.getIndex(), i));
				}
			}
		}
		
		hueGeneration();
		
		// Try to force a re-draw
		this.postInvalidate();
	}
	
	private void hueGeneration()
	{
		// Count the number of distinct IO types that are available across both services
		ArrayList<ServiceIO> ios = new ArrayList<ServiceIO>();		
		if(second != null && second.getInputs() != null) ios.addAll(second.getInputs());
		if(first != null && first.getOutputs() != null) ios.addAll(first.getOutputs());
		
		ArrayList<IOType> distinctTypes = new ArrayList<IOType>();
		Collections.sort(ios, WiringMap.this);
		
		// Sort by IOType then add the different ones
		String previous = "";
		for(int i = 0; i < ios.size(); i++)
		{
			IOType type = ios.get(i).getType();

			if(type.getClass().getCanonicalName().equals(previous) && !type.getClass().getCanonicalName().equals(Set.class.getCanonicalName()))
				continue;
			
			previous = type.getClass().getCanonicalName();
			distinctTypes.add(type);
		}

		if(distinctTypes.size() == 0)
			return;
		
		// Create colours equally spaced around the Hue spectrum but drop the Saturation
		int gap = 360 / distinctTypes.size();
		hueMap = new HashMap<String, Integer>();
		for(int i = 0; i < distinctTypes.size(); i++)
		{
			hueMap.put(distinctTypes.get(i).getClass().getCanonicalName(), i * gap);
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) 
	{
        Paint paint = new Paint();
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
		float scale = activity.getResources().getDisplayMetrics().density;
//		int px = 0;//(int) ((24 + 5) * scale + 0.5); // Half the square plus the border
		
		paint.setStrokeWidth(4 * scale);
		
		ArrayList<PathColour> paths = getPaths();

        for (PathColour path : paths) {
            paint.setColor(path.colour);
            canvas.drawPath(path.p, paint);
        }
		
		super.dispatchDraw(canvas);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{		
		// Don't know if this actually does anything?
		super.onDraw(canvas);
		
		// Reset the paint incase something else has changed it
		
	}
	
	@Override
	public int compare(ServiceIO a, ServiceIO b) 
	{
		return a.getType().getClass().getCanonicalName().compareTo(b.getType().getClass().getCanonicalName());
	}
	
	private ArrayList<PathColour> getPaths()
	{
		ArrayList<PathColour> paths = new ArrayList<PathColour>();

        for (Point connection : connections)
        {
            Path path = new Path();
            // Need to get the class of one of the items


            Point p = connection;
            View selectedOutput = outputList.getChildAt(p.x);
            View selectedInput = inputList.getChildAt(p.y);

            String className = first.getOutputs().get(p.x).getType().getClassName();
            int col = Color.HSVToColor(FULL_ALPHA, new float[]{hueMap.get(className), 1, 1});

            // This should be half the width of the ``tab'' you click on
            int px = 0;//(int) ((24 + 5) * scale + 0.5); // Half the square plus the border

            // XXX Colour of connections?

            int[] layout = new int[2];
            this.getLocationOnScreen(layout);

            int[] outputTab = new int[2];
            selectedOutput.getLocationOnScreen(outputTab);

            // Move this left a bit to be in the middle of the input
            float startX = outputTab[0] - layout[0] + selectedOutput.getWidth() - px;
            float startY = outputTab[1] - layout[1] + (selectedOutput.getHeight() / 2);
            path.moveTo(startX, startY);

            int[] inputTab = new int[2];
            selectedInput.getLocationOnScreen(inputTab);

            float endX = inputTab[0] - layout[0] + px;
            float endY = inputTab[1] - layout[1] + (selectedInput.getHeight() / 2);
            path.lineTo(endX, endY);

            paths.add(new PathColour(path, col));
        }
		
		return paths;
	}
	
	private void setHighlight(IOType type, ViewGroup parent)
	{	
		// Set it so that the highlight isn't applied in the list in which the thing happened
		
		if(outputContainer.getVisibility() == View.VISIBLE)
		{
			setListAlpha(outputList, first.getOutputs(), 0, null, false);
			
			if(!parent.equals(outputList) && type != null)
				setListAlpha(outputList, first.getOutputs(), FULL_ALPHA / LOWLIGHT_ALPHA, type, false);
		}
		
		if(inputContainer.getVisibility() == View.VISIBLE)
		{
			setListAlpha(inputList, second.getInputs(), 0, null, true);
			
			if(!parent.equals(inputList) & type != null)
				setListAlpha(inputList, second.getInputs(), FULL_ALPHA / LOWLIGHT_ALPHA, type, true);
		}
	}
	
	private void setListAlpha(ListView lv, ArrayList<ServiceIO> ioList, int alpha, IOType type, boolean inputs)
	{
		if(ioList == null)
			return;
		
		if(type == null)
		{
			for(int i = 0; i < lv.getChildCount(); i++)
			{
				View vg = lv.getChildAt(i);
				vg.findViewById(R.id.endpoint).setBackgroundColor(Color.WHITE);
			}
		}
		else
		{
			String className = type.getClass().getCanonicalName();
			for(int i = 0; i < lv.getChildCount(); i++)
			{
				String itemClassName = ioList.get(i).getType().getClass().getCanonicalName();
				
				if(inputConnection(i) && inputs)
					continue;
				
				if(itemClassName.equals(className))
				{
					View vg = lv.getChildAt(i);
					vg.findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(alpha, new float[]{hueMap.get(itemClassName), 1, 1}));
				}
			}
		}
	}
	
	public void redraw()
	{
		this.postInvalidate();
		this.outputList.invalidateViews();
		this.inputList.invalidateViews();
	}
	
	private void showAppDialog(final ServiceIO item)
	{
		DialogApp da = new DialogApp(activity, this, item);
		da.show();
	}
	
	private void showFilterDialog(final ServiceIO item)
	{
		DialogFilter df = new DialogFilter(activity, this, item);
		df.show();
	}
	
	private void showIODialog(final ServiceIO item)
	{
		DialogIO di = new DialogIO(activity, this, item);
		di.show();
	}
	
	private class InputAdapter extends ArrayAdapter<ServiceIO>
	{
		public ArrayList<ServiceIO> items;
		
		public InputAdapter(Context parent, int textViewResourceId, ArrayList<ServiceIO> items)
		{
			super(parent, textViewResourceId, items);
			this.items = items;
		}
		
		public View getView(final int position, View convertView, final ViewGroup parent)
		{
			if(convertView == null)
			{
				LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);				
				convertView = vi.inflate(R.layout.list_item_wiring_in, null);
			}
			
			final View v = convertView;
			final ServiceIO item = items.get(position);
			final View endpoint =  v.findViewById(R.id.endpoint);
			final Drawable blob = v.findViewById(R.id.blob).getBackground();
			final Drawable stub = v.findViewById(R.id.stub).getBackground();
			
			int col = Color.HSVToColor( FULL_ALPHA / BASE_ALPHA,
					new float[]{ 
						hueMap.get(item.getType().getClass().getCanonicalName()), 
						1, 
						1 
					}
			);
			
			blob.setColorFilter(col, PorterDuff.Mode.ADD);
			stub.setColorFilter(col, PorterDuff.Mode.ADD);

			TextView ioName = (TextView) v.findViewById(R.id.io_name);
			ioName.setText(item.getFriendlyName());

			TextView ioType = (TextView) v.findViewById(R.id.io_type);
			TextView ioValue = (TextView) v.findViewById(R.id.io_value);

			int visibility = item.isMandatory() ? View.VISIBLE : View.GONE;
			v.findViewById(R.id.mandatory_bar).setVisibility(visibility);
			
			ImageView setButton = (ImageView) v.findViewById(R.id.set_button);
			setButton.bringToFront();
			setButton.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					if(item.getType().equals(IOType.Factory.getType(IOType.Factory.APP)))
						showAppDialog(item);
					else
						showIODialog(item);
				}
			});
			
			if(!item.hasValue())
			{
				ioType.setText(item.getType().getName());
				setButton.setImageResource(R.drawable.ic_add);
				ioValue.setText("");
			}
			else
			{
				ioType.setText(item.getType().getName() + ": ");
				ioValue.setText(item.getManualValue().toString());
				setButton.setImageResource(R.drawable.ic_add_on);
			}
			
			// If it's not unfiltered, then it's either manual or not
			if(item.isFiltered() == ServiceIO.UNFILTERED)
			{
				ioType.setText(item.getType().getName());
			}
			else
			{
	    		// This is for a manual one
	    		if(item.isFiltered() == ServiceIO.MANUAL_FILTER)
	    		{
	    			String value = item.getType().toString(item.getManualValue());
	    			ioValue.setText(value);
	    		}
	    		else if(item.isFiltered() == ServiceIO.SAMPLE_FILTER)
	    		{
	    			// Need to look up what the value for this thing is, but return the friendly name not the other thing
	    			ioValue.setText(item.getChosenSampleValue().name);
	    		}
			}
			
			if(activity.getMode() == ActivityWiring.MODE_SETTING)
			{
				endpoint.setVisibility(View.GONE);
				setButton.setVisibility(View.VISIBLE);
			}
			else
			{
				endpoint.setVisibility(View.VISIBLE);
				setButton.setVisibility(View.GONE);
			}
			
			int[] pos = new int[2];
			endpoint.getLocationOnScreen(pos);
			
			endpoint.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View b) 
				{
					if(inputConnection(position) || first == null || !first.hasOutputs())
					{

					}
					else if(iSelected == null && oSelected == null)
					{						
						// If they click an output first then just work normally and connect it for them
						setHighlight(item.getType(), parent);
						b.setBackgroundColor(
							Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
							new float[] {
								hueMap.get(item.getType().getClass().getCanonicalName()), 
								1, 
								1
							}
						));
						
						iSelected = item;
						iIndex = position;
						activity.setStatus("Selected " + item.getName());
					}
					else if(oSelected != null && oSelected.getType().equals(item.getType()) && iSelected == null)
					{	
						if(LOG) Log.d(TAG, "Input " + position  + " We have a match");
						
						// If the current input and output are in the connections list then do nothing. That would be stupid
						if(checkConnection(oIndex, position) || inputConnection(position))
							return;
						
						setHighlight(null, parent);
						
						iSelected = item;
						iIndex = position;
						activity.setStatus("Selected " + item.getName());
						
						// This one
						b.setBackgroundColor(
						Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							)
						);
						
						// The corresponding output needs to be put back
						outputList.getChildAt(oIndex).findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(oSelected.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							)
						);
						
						redraw();
						
						// Update the drawing a bit later...
						Handler h = new Handler();
						h.postDelayed(new RedrawRunnable(oIndex, iIndex), 200);
					}
					else if(iSelected != null && oSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position + " (output is null, input is not)");
						// This means that we need to unselect the current one?
						setHighlight(null, parent);
						
						if(iIndex == position)
						{
							// We don't have a new one
							iSelected = null;
							iIndex = -1;
						}
						else
						{
							setHighlight(item.getType(), parent);
							b.setBackgroundColor(
								Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							));
							
							iSelected = item;
							iIndex = position;
							activity.setStatus("Selected " + item.getName());
						}
						
						redraw();
					}
					else
					{
						setHighlight(item.getType(), parent);
						
						b.setBackgroundColor(
							Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							)
						);
						
						iSelected = item;
						iIndex = position;
						activity.setStatus("Selected " + item.getName());
						oSelected = null;
						oIndex = -1;
					}		
				}
			});
			
			endpoint.setOnLongClickListener(new OnLongClickListener() 
			{	
				@Override
				public boolean onLongClick(View v) 
				{
					if(item.getType().equals(IOType.Factory.getType(IOType.Factory.APP)))
						showAppDialog(item);
					else
						showIODialog(item);
					return false;
				}
			});
			
			return v;
		}
	}
	
	private class RedrawRunnable implements Runnable
	{
		private int outputIndex;
		private int inputIndex;
		
		private RedrawRunnable(int outputIndex, int inputIndex)
		{
			this.inputIndex = inputIndex;
			this.outputIndex = outputIndex;
		}

		@Override
		public void run() 
		{	
			View outElement = outputList.getChildAt(outputIndex);
			View inElement = inputList.getChildAt(inputIndex);
			
			ServiceIO out = first.getOutputs().get(outputIndex);
			ServiceIO in = second.getInputs().get(inputIndex);
			
			activity.setStatus("Connected " + out.getFriendlyName() + " to " + in.getFriendlyName());
			
			// Kill the backgrounds
			outElement.findViewById(R.id.endpoint).setBackgroundColor(Color.WHITE);
			inElement.findViewById(R.id.endpoint).setBackgroundColor(Color.WHITE);
			
			// Swap the images over
			outElement.findViewById(R.id.blob).setBackgroundResource(R.drawable.io_blob_on);
			outElement.findViewById(R.id.stub).setBackgroundResource(R.drawable.io_stub_on);
			inElement.findViewById(R.id.blob).setBackgroundResource(R.drawable.io_blob_on);
			inElement.findViewById(R.id.stub).setBackgroundResource(R.drawable.io_stub_on);
			
			// Add the connections
			connections.add(new Point(outputIndex, inputIndex));
			
			out.setConnection(in);
			in.setConnection(out);
			
			registry.updateCurrent();
			redraw();
		}
		
	}

	private class OutputAdapter extends ArrayAdapter<ServiceIO>
	{
		public ArrayList<ServiceIO> items;
		
		public OutputAdapter(Context parent, int textViewResourceId, ArrayList<ServiceIO> items)
		{
			super(parent, textViewResourceId, items);
			this.items = items;
		}
		
		public View getView(final int position, View convertView, final ViewGroup parent)
		{
			if(convertView == null)
			{
				LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_item_wiring_out, null);
			}
			
			final View v = convertView;
			final ServiceIO item = items.get(position);
			final LinearLayout endpoint =  (LinearLayout) v.findViewById(R.id.endpoint);
			final Drawable blob = v.findViewById(R.id.blob).getBackground();
			final Drawable stub = v.findViewById(R.id.stub).getBackground();
			
			int col = Color.HSVToColor( FULL_ALPHA / BASE_ALPHA,
									new float[]{ 
										hueMap.get(item.getType().getClass().getCanonicalName()), 
										1, 
										1 
									}
			);
			
			blob.setColorFilter(col, PorterDuff.Mode.ADD);
			stub.setColorFilter(col, PorterDuff.Mode.ADD);
			
			TextView ioName = (TextView) v.findViewById(R.id.io_name);
			ioName.setText(item.getFriendlyName());
			
			TextView ioType = (TextView) v.findViewById(R.id.io_type);
			TextView ioValue = (TextView) v.findViewById(R.id.io_value);

            ImageView filterButton = (ImageView) v.findViewById(R.id.filter_button);
			filterButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					showFilterDialog(item);	
				}
			});
			
			if(!item.hasValue())
			{
				ioType.setText(item.getType().getName());
				filterButton.setImageResource(R.drawable.filter_small);
				ioValue.setText("");
			}
			else
			{
				ioType.setText(item.getType().getName() + ": ");
				ioValue.setText(item.getManualValue().toString());
				filterButton.setImageResource(R.drawable.filter_small_on);
			}
			
			// If it's not unfiltered, then it's either manual or not
			if(item.isFiltered() == ServiceIO.UNFILTERED)
			{
				ioType.setText(item.getType().getName());
			}
			else
			{
				FilterValue fv = IOFilter.filters.get(item.getCondition());
				
				ioType.setText(item.getType().getName() + ": " + fv.text + " ");
	    		
	    		// This is for a manual one
	    		if(item.isFiltered() == ServiceIO.MANUAL_FILTER)
	    		{
	    			String value = item.getType().toString(item.getManualValue());
	    			ioValue.setText(value);
	    		}
	    		else if(item.isFiltered() == ServiceIO.SAMPLE_FILTER)
	    		{
	    			// Need to look up what the value for this thing is, but return the friendly name not the other thing
	    			ioValue.setText(item.getChosenSampleValue().name);
	    		}
			}
			
			// Change the filter button image if a filter is selected
			
			if(activity.getMode() == ActivityWiring.MODE_SETTING)
			{
				endpoint.setVisibility(View.GONE);
				filterButton.setVisibility(View.VISIBLE);
			}
			else
			{
				endpoint.setVisibility(View.VISIBLE);
				filterButton.setVisibility(View.GONE);
			}
			
			endpoint.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View b) 
				{
					if(LOG) Log.d(TAG, "Output " + position + " (" + oIndex + ", " + iIndex + ")");
					
					if(second == null || !second.hasInputs())
					{
						return;
					}
					if(iSelected == null && oSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position + " (Both null)");
						// If they click an output first then just work normally and connect it for them
						setHighlight(item.getType(), parent);
						b.setBackgroundColor(
							Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
							new float[] {
								hueMap.get(item.getType().getClass().getCanonicalName()), 
								1, 
								1
							}
						));
						
						oSelected = item;
						oIndex = position;
						activity.setStatus("Selected " + item.getName());
					}
					else if(iSelected != null && iSelected.getType().equals(item.getType()) && oSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position  + " We have a match");
						
						// If the current input and output are in the connections list then do nothing. That would be stupid
						if(checkConnection(oIndex, position))
							return;
						
						setHighlight(null, parent);
						
						oSelected = item;
						oIndex = position;
						activity.setStatus("Selected " + item.getName());
						
						// This one
						b.setBackgroundColor(
						Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							)
						);
						
						// The corresponding output needs to be put back
						inputList.getChildAt(iIndex).findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(oSelected.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							)
						);
						
						redraw();
						
						// Update the drawing a bit later...
						Handler h = new Handler();
						h.postDelayed(new RedrawRunnable(oIndex, iIndex), 200);
					}
					else if(oSelected != null && iSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position + " (input is null, output is not)");
						// This means that we need to unselect the current one?
						setHighlight(null, parent);
						
						if(oIndex == position)
						{
							// We don't have a new one
							oSelected = null;
							oIndex = -1;
						}
						else
						{
							setHighlight(item.getType(), parent);
							b.setBackgroundColor(
								Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							));
							
							oSelected = item;
							oIndex = position;
							activity.setStatus("Selected " + item.getName());
						}
						
						redraw();
					}
					else
					{
						if(LOG) Log.d(TAG, "Output " + position + " (else....)");
						setHighlight(item.getType(), parent);
						
						b.setBackgroundColor(
							Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.getType().getClass().getCanonicalName()), 
									1, 
									1
								}
							)
						);
						
						oSelected = item;
						oIndex = position;
						activity.setStatus("Selected " + item.getName());
						iSelected = null;
						iIndex = -1;
					}		
				}
			});
			
			endpoint.setOnLongClickListener(new OnLongClickListener() 
			{	
				@Override
				public boolean onLongClick(View v) 
				{
					// Show the dialog
					DialogConnection cd = new DialogConnection(activity, WiringMap.this, outputList, inputList, item, position);
					cd.show();
					return true;
				}
			});
			
			return v;
		}
	}
	
	private class PathColour
	{
		private Path p;
		private int colour;
		
		private PathColour(Path p, int colour)
		{
			this.p = p;
			this.colour = colour;
		}
	}
}