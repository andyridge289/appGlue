package com.appglue.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.ActivityWiring;
import com.appglue.IODescription;
import com.appglue.R;
import com.appglue.description.datatypes.IOType;
import com.appglue.description.datatypes.Set;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.dialog.DialogConnection;
import com.appglue.library.IOFilter;
import com.appglue.library.IOFilter.FilterValue;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static com.appglue.Constants.FULL_ALPHA;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class WiringMap extends LinearLayout implements Comparator<IODescription>, AbsListView.OnScrollListener {

	private ComponentService first;
	private ComponentService second;
    private ServiceDescription firstDescription;
    private ServiceDescription secondDescription;
	
	private ListView outputList;
    private SparseIntArray outputPositions;
    private Queue<Integer> outputOffsets;

	private ListView inputList;
    private SparseIntArray inputPositions;
    private Queue<Integer> inputOffsets;

    // I don't think we actually care what these are
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
    private final Object lock = new Object();

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
        outputList.setOnScrollListener(this);

        inputList = (ListView) findViewById(R.id.input_list);
		inputList.setClickable(false);
        inputList.setOnScrollListener(this);

        outputContainer = findViewById(R.id.outputs);
		inputContainer = findViewById(R.id.inputs);
		
		noOutputs = findViewById(R.id.no_outputs);
		noInputs = findViewById(R.id.no_inputs);
		
		addOutput = findViewById(R.id.add_output);
		addInput = findViewById(R.id.add_input);

        connections = new ArrayList<Point>();

        inputOffsets = new LinkedList<Integer>();
        outputOffsets = new LinkedList<Integer>();

        iIndex = -1;
		oIndex = -1;
	}

    public ComponentService getFirst() {
		return first;
	}

	public void setFirst(ComponentService first) {
		this.first = first;
	}

	public ComponentService getSecond() {
		return second;
	}

	public void setSecond(ComponentService second) {
		this.second = second;
	}

    public void removeConnection(Point p) {
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

    private boolean inputConnection(int index) {
        for (Point connection : connections) {
            if (connection.y == index)
                return true;
        }

        return false;
	}


    public void set(ComponentService first, ComponentService second)
	{
		this.first = first;
		if(first != null)
		{
			ArrayList<ServiceIO> outputs = first.outputs();
			if(outputs.size() > 0)
			{
				// There are outputs, show the list, hide the none and the add
				outputList.setAdapter(new OutputAdapter(activity, outputs));
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
			ArrayList<ServiceIO> inputs = second.inputs();
			if(inputs.size() > 0)
			{
				inputList.setAdapter(new InputAdapter(activity, inputs));
				inputContainer.setVisibility(View.VISIBLE);
				noInputs.setVisibility(View.INVISIBLE);
				addInput.setVisibility(View.INVISIBLE);
			} else
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
		if(second != null && second.inputs() != null)
		{
			ArrayList<ServiceIO> in = second.inputs();
			for(int i = 0; i < in.size(); i++)
			{
				ServiceIO connection = in.get(i).connection();
				if(connection != null)
				{
					// It's connected to something so work out what position the other thing is in the outputs
					connections.add(new Point(connection.description().index(), i));
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
		ArrayList<IODescription> ios = new ArrayList<IODescription>();
		if(second != null && second.description().inputs() != null) ios.addAll(second.description().inputs());
		if(first != null && first.description().outputs() != null) ios.addAll(first.description().outputs());
		
		ArrayList<IOType> distinctTypes = new ArrayList<IOType>();
		Collections.sort(ios, WiringMap.this);
		
		// Sort by IOType then add the different ones
		String previous = "";
        for (IODescription io : ios) {
            IOType type = io.type();

            if (type.getClass().getCanonicalName().equals(previous) && !type.getClass().getCanonicalName().equals(Set.class.getCanonicalName()))
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
	protected void dispatchDraw(@NonNull Canvas canvas)
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

        Log.d(TAG, outputOffsets + " :: " + inputOffsets);

        int outputOffset = 0;
        int inputOffset = 0;

        synchronized (lock) {
            while (!outputOffsets.isEmpty()) {
                outputOffset += outputOffsets.poll();
            }
            while (!inputOffsets.isEmpty()) {
                inputOffset += inputOffsets.poll();
            }
        }

        for (PathColour path : paths) {
            paint.setColor(path.colour);

            Path p = new Path();
            p.moveTo(path.start.x, path.start.y + outputOffset);

            p.lineTo(path.end.x, path.end.y + inputOffset);

            if (LOG)
                Log.d(TAG, "Should be drawing path from [" + path.start.x + ", " + (path.start.y + outputOffset) +
                    "] to [" + path.end.x + ", " + (path.end.y + inputOffset) + "]");

            canvas.drawPath(p, paint);
        }
		
		super.dispatchDraw(canvas);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{		
		// Don't know if this actually does anything?
		super.onDraw(canvas);
		
		// Reset the paint in case something else has changed it
		
	}
	
	@Override
	public int compare(IODescription a, IODescription b)
	{
        if (a.type() == null)
            Log.e(TAG, "a Type null");

        if (b.type() == null)
            Log.e(TAG, "b type null");

		return a.type().getClass().getCanonicalName().compareTo(b.type().getClass().getCanonicalName());
	}
	
	private ArrayList<PathColour> getPaths()
	{
		ArrayList<PathColour> paths = new ArrayList<PathColour>();

        for (Point connection : connections)
        {
//            Path path = new Path();
            // Need to get the class of one of the items

            View selectedOutput = outputList.getChildAt(connection.x);
            View selectedInput = inputList.getChildAt(connection.y);

            if (connection.x == -1 || connection.y == -1) {
                Log.e(TAG, "Path epic failure, not really sure why");
                continue;
            }

            String className = first.description().outputs().get(connection.x).type().getClassName();
            int col = Color.HSVToColor(FULL_ALPHA, new float[]{hueMap.get(className), 1, 1});

            // This should be half the width of the ``tab'' you click on
            int px = 0;//(int) ((24 + 5) * scale + 0.5); // Half the square plus the border

            int[] layout = new int[2];
            this.getLocationOnScreen(layout);

            int[] outputTab = new int[2];
            selectedOutput.getLocationOnScreen(outputTab);

            // Move this left a bit to be in the middle of the input
            float startX = outputTab[0] - layout[0] + selectedOutput.getWidth() - px;
            float startY = outputTab[1] - layout[1] + (selectedOutput.getHeight() / 2);
            PointF start = new PointF(startX, startY);

            int[] inputTab = new int[2];
            selectedInput.getLocationOnScreen(inputTab);

            float endX = inputTab[0] - layout[0] + px;
            float endY = inputTab[1] - layout[1] + (selectedInput.getHeight() / 2);
            PointF end = new PointF(endX, endY);

            paths.add(new PathColour(start, end, col));
        }
		
		return paths;
	}
	
	private void setHighlight(IOType type, ViewGroup parent)
	{	
		// Set it so that the highlight isn't applied in the list in which the thing happened
		
		if(outputContainer.getVisibility() == View.VISIBLE)
		{
			setListAlpha(outputList, first.description().outputs(), 0, null, false);
			
			if(!parent.equals(outputList) && type != null)
				setListAlpha(outputList, first.description().outputs(), FULL_ALPHA / LOWLIGHT_ALPHA, type, false);
		}
		
		if(inputContainer.getVisibility() == View.VISIBLE)
		{
			setListAlpha(inputList, second.description().inputs(), 0, null, true);
			
			if(!parent.equals(inputList) & type != null)
				setListAlpha(inputList, second.description().inputs(), FULL_ALPHA / LOWLIGHT_ALPHA, type, true);
		}
	}
	
	private void setListAlpha(ListView lv, ArrayList<IODescription> ioList, int alpha, IOType type, boolean inputs)
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
				String itemClassName = ioList.get(i).type().getClass().getCanonicalName();
				
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

    public void redraw(boolean doLists) {
		this.postInvalidate();

        if (outputList != null && doLists) {
            this.outputList.invalidateViews();
        }

        if (doLists && inputList != null) {
            this.inputList.invalidateViews();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView v, int firstPosition, int visibleItems, int totalItems) {

        int incOffset = calculateIncrementalOffset(v, firstPosition, visibleItems, v.equals(inputList));
        if (incOffset == 0)
            return;

        synchronized (lock) {
            if (v.equals(outputList)) {
                outputOffsets.add(incOffset);
            } else {
                inputOffsets.add(incOffset);
            }
        }

        redraw(false);
    }

    public int calculateIncrementalOffset(AbsListView v, final int firstPosition, final int visibleItems, boolean input) {

        // Remember previous positions, if any
        SparseIntArray previousPositions = input ? inputPositions : outputPositions;

        // Store new positions
        if (input) {
            inputPositions = new SparseIntArray();
            for (int i = 0; i < visibleItems; i++) {
                inputPositions.put(firstPosition + i, v.getChildAt(i).getTop());
            }
        } else {
            outputPositions = new SparseIntArray();
            for (int i = 0; i < visibleItems; i++) {
                outputPositions.put(firstPosition + i, v.getChildAt(i).getTop());
            }
        }

        if (previousPositions != null) {
            // Find position which exists in both mPositions and previousPositions, then return the difference
            // of the new and old Y values.
            for (int i = 0; i < previousPositions.size(); i++) {
                int position = previousPositions.keyAt(i);
                int previousTop = previousPositions.get(position);
                Integer newTop = input ? inputPositions.get(position) : outputPositions.get(position);
                if (newTop != null) {
                    return newTop - previousTop;
                }
            }
        }

        return 0; // No view's position was in both previousPositions and mPositions
    }


    private class InputAdapter extends ArrayAdapter<ServiceIO>
	{
		public ArrayList<ServiceIO> items;
		
		public InputAdapter(Context parent, ArrayList<ServiceIO> items)
		{
			super(parent, R.layout.list_item_wiring_in, items);
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
            final IODescription description = item.description();
			final View endpoint =  v.findViewById(R.id.endpoint);
			final Drawable blob = v.findViewById(R.id.blob).getBackground();
			final Drawable stub = v.findViewById(R.id.stub).getBackground();
			
			int col = Color.HSVToColor( FULL_ALPHA / BASE_ALPHA,
					new float[]{ 
						hueMap.get(description.type().getClass().getCanonicalName()),
						1, 
						1 
					}
			);
			
			blob.setColorFilter(col, PorterDuff.Mode.ADD);
			stub.setColorFilter(col, PorterDuff.Mode.ADD);

			TextView ioName = (TextView) v.findViewById(R.id.io_name);
			ioName.setText(description.friendlyName());

			TextView ioType = (TextView) v.findViewById(R.id.io_type);
			TextView ioValue = (TextView) v.findViewById(R.id.io_value);

			int visibility = description.isMandatory() ? View.VISIBLE : View.GONE;
			v.findViewById(R.id.mandatory_bar).setVisibility(visibility);
			
			if(!item.hasValue())
			{
				ioType.setText(description.type().getName());
				ioValue.setText("");
			}
			else
			{
				ioType.setText(description.type().getName() + ": ");
				ioValue.setText(item.getManualValue().toString());
			}
			
			// If it's not unfiltered, then it's either manual or not
			if(item.isFiltered() == ServiceIO.UNFILTERED)
			{
				ioType.setText(description.type().getName());
			}
			else
			{
	    		// This is for a manual one
	    		if(item.isFiltered() == ServiceIO.MANUAL_FILTER)
	    		{
	    			String value = description.type().toString(item.getManualValue());
	    			ioValue.setText(value);
	    		}
	    		else if(item.isFiltered() == ServiceIO.SAMPLE_FILTER)
	    		{
	    			// Need to look up what the value for this thing is, but return the friendly name not the other thing
	    			ioValue.setText(item.getChosenSampleValue().name);
	    		}
			}

            endpoint.setVisibility(View.VISIBLE);

			
			int[] pos = new int[2];
			endpoint.getLocationOnScreen(pos);
			
			endpoint.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View b) 
				{
					if(inputConnection(position) || first == null || !firstDescription.hasOutputs())
					{
                        if(LOG) Log.d(TAG, "No highlights");
					}
					else if(iSelected == null && oSelected == null)
					{						
						// If they click an output first then just work normally and connect it for them
						setHighlight(description.type(), parent);
						b.setBackgroundColor(
							Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
							new float[] {
								hueMap.get(description.type().getClass().getCanonicalName()),
								1, 
								1
							}
						));
						
						iSelected = item;
						iIndex = position;
						activity.setStatus("Selected " + description.name());
					}
					else if(oSelected != null && oSelected.description().type().equals(description.type()) && iSelected == null)
					{	
						if(LOG) Log.d(TAG, "Input " + position  + " We have a match");
						
						// If the current input and output are in the connections list then do nothing. That would be stupid
						if(checkConnection(oIndex, position) || inputConnection(position))
							return;
						
						setHighlight(null, parent);
						
						iSelected = item;
						iIndex = position;
						activity.setStatus("Selected " + description.name());
						
						// This one
						b.setBackgroundColor(
						Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(description.type().getClass().getCanonicalName()),
									1, 
									1
								}
							)
						);
						
						// The corresponding output needs to be put back
						outputList.getChildAt(oIndex).findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(oSelected.description().getClass().getCanonicalName()),
									1, 
									1
								}
							)
						);

                        redraw(true);

                        // Update the drawing a bit later...
						Handler h = new Handler();
						h.postDelayed(new RedrawRunnable(oIndex, iIndex), 200);
					}
					else if(iSelected != null && oSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position + " (output is null, input is not)");
						// This means that we need to deselect the current one?
						setHighlight(null, parent);
						
						if(iIndex == position)
						{
							// We don't have a new one
							iSelected = null;
							iIndex = -1;
						}
						else
						{
							setHighlight(description.type(), parent);
							b.setBackgroundColor(
								Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(description.type().getClass().getCanonicalName()),
									1, 
									1
								}
							));
							
							iSelected = item;
							iIndex = position;
							activity.setStatus("Selected " + description.name());
						}

                        redraw(true);
                    }
					else
					{
						setHighlight(description.type(), parent);
						
						b.setBackgroundColor(
							Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(description.type().getClass().getCanonicalName()),
									1, 
									1
								}
							)
						);
						
						iSelected = item;
						iIndex = position;
						activity.setStatus("Selected " + description.name());
						oSelected = null;
						oIndex = -1;
					}		
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

            ServiceIO out = first.outputs().get(outputIndex);
            ServiceIO in = second.inputs().get(inputIndex);
			
			activity.setStatus("Connected " + out.description().friendlyName() + " to " + in.description().friendlyName());
			
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
            redraw(true);
        }
		
	}

	private class OutputAdapter extends ArrayAdapter<ServiceIO>
	{
		public ArrayList<ServiceIO> items;
		
		public OutputAdapter(Context parent, ArrayList<ServiceIO> items)
		{
			super(parent, R.layout.list_item_wiring_out, items);
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
            final IODescription description = item.description();
			final LinearLayout endpoint =  (LinearLayout) v.findViewById(R.id.endpoint);
			final Drawable blob = v.findViewById(R.id.blob).getBackground();
			final Drawable stub = v.findViewById(R.id.stub).getBackground();
			
			int col = Color.HSVToColor( FULL_ALPHA / BASE_ALPHA,
									new float[]{ 
										hueMap.get(item.description().type().getClass().getCanonicalName()),
										1, 
										1 
									}
			);
			
			blob.setColorFilter(col, PorterDuff.Mode.ADD);
			stub.setColorFilter(col, PorterDuff.Mode.ADD);
			
			TextView ioName = (TextView) v.findViewById(R.id.io_name);
			ioName.setText(item.description().friendlyName());
			
			TextView ioType = (TextView) v.findViewById(R.id.io_type);
			TextView ioValue = (TextView) v.findViewById(R.id.io_value);

            if (!item.hasValue()) {
				ioType.setText(item.description().type().getName());
				ioValue.setText("");
			}
			else
			{
				ioType.setText(item.description().type().getName() + ": ");
				ioValue.setText(item.getManualValue().toString());
			}
			
			// If it's not unfiltered, then it's either manual or not
			if(item.isFiltered() == ServiceIO.UNFILTERED)
			{
				ioType.setText(item.description().type().getName());
			}
			else
			{
				FilterValue fv = IOFilter.filters.get(item.getCondition());
				
				ioType.setText(item.description().type().getName() + ": " + fv.text + " ");
	    		
	    		// This is for a manual one
	    		if(item.isFiltered() == ServiceIO.MANUAL_FILTER)
	    		{
	    			String value = item.description().type().toString(item.getManualValue());
	    			ioValue.setText(value);
	    		}
	    		else if(item.isFiltered() == ServiceIO.SAMPLE_FILTER)
	    		{
	    			// Need to look up what the value for this thing is, but return the friendly name not the other thing
	    			ioValue.setText(item.getChosenSampleValue().name);
	    		}
			}
			
			// Change the filter button image if a filter is selected
            endpoint.setVisibility(View.VISIBLE);

			endpoint.setOnClickListener(new OnClickListener() 
			{	
				@Override
				public void onClick(View b) 
				{
					if(LOG) Log.d(TAG, "Output " + position + " (" + oIndex + ", " + iIndex + ")");
					
					if(second == null || !secondDescription.hasInputs())
					{
						return;
					}
					if(iSelected == null && oSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position + " (Both null)");
						// If they click an output first then just work normally and connect it for them
						setHighlight(item.description().type(), parent);
						b.setBackgroundColor(
							Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
							new float[] {
								hueMap.get(item.description().type().getClass().getCanonicalName()),
								1, 
								1
							}
						));
						
						oSelected = item;
						oIndex = position;
						activity.setStatus("Selected " + item.description().name());
					}
					else if(iSelected != null && iSelected.description().type().equals(item.description().type()) && oSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position  + " We have a match");
						
						// If the current input and output are in the connections list then do nothing. That would be stupid
						if(checkConnection(oIndex, position))
							return;
						
						setHighlight(null, parent);
						
						oSelected = item;
						oIndex = position;
						activity.setStatus("Selected " + item.description().name());
						
						// This one
						b.setBackgroundColor(
						Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.description().type().getClass().getCanonicalName()),
									1, 
									1
								}
							)
						);
						
						// The corresponding output needs to be put back
						inputList.getChildAt(iIndex).findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(oSelected.description().type().getClass().getCanonicalName()),
									1, 
									1
								}
							)
						);

                        redraw(true);

                        // Update the drawing a bit later...
						Handler h = new Handler();
						h.postDelayed(new RedrawRunnable(oIndex, iIndex), 200);
					}
					else if(oSelected != null && iSelected == null)
					{
						if(LOG) Log.d(TAG, "Output " + position + " (input is null, output is not)");
						// This means that we need to deselect the current one?
						setHighlight(null, parent);
						
						if(oIndex == position)
						{
							// We don't have a new one
							oSelected = null;
							oIndex = -1;
						}
						else
						{
							setHighlight(item.description().type(), parent);
							b.setBackgroundColor(
								Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.description().type().getClass().getCanonicalName()),
									1, 
									1
								}
							));
							
							oSelected = item;
							oIndex = position;
							activity.setStatus("Selected " + item.description().name());
						}

                        redraw(true);
                    }
					else
					{
						if(LOG) Log.d(TAG, "Output " + position + " (else....)");
						setHighlight(item.description().type(), parent);
						
						b.setBackgroundColor(
							Color.HSVToColor(
								FULL_ALPHA / HIGHLIGHT_ALPHA, 
								new float[] {
									hueMap.get(item.description().type().getClass().getCanonicalName()),
									1, 
									1
								}
							)
						);
						
						oSelected = item;
						oIndex = position;
						activity.setStatus("Selected " + item.description().name());
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
        private PointF start;
        private PointF end;
        private int colour;

        private PathColour(PointF start, PointF end, int colour) {
            this.start = start;
            this.end = end;
            this.colour = colour;
		}
	}
}