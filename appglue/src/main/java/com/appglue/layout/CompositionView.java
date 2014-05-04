package com.appglue.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.appglue.ActivityCompositionCanvas;
import com.appglue.R;
import com.appglue.ServiceIO;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.io.IOException;
import java.util.ArrayList;

public class CompositionView extends View
{
	private Paint paint;
	private Bitmap background;
	
	private float posX = 0;
	private float posY = 0;
	private float lastX = 0;
	private float lastY = 0;

	private int canvasWidth = 2000;
	private int canvasHeight = 2000;
	
	private int deviceWidth;
	private int deviceHeight;
	
	private ArrayList<ComponentRegion> regions;

	private final int COMPONENT_WIDTH = 400;
	private final int COMPONENT_HEIGHT = 400;
	private final int COMPONENT_GAP = COMPONENT_WIDTH / 5;
	private final int IO_PADDING = 30;
	private final int IO_SPACE = COMPONENT_HEIGHT - 2 * IO_PADDING;
	
	private final int MAND_WIDTH = 30;
	private final int MAND_HEIGHT = 20;
	private final int NON_MAND_WIDTH = 20;
	private final int NON_MAND_HEIGHT = 10;
	private final int BORDER = 2;
	
	private static final int INVALID_POINTER_ID = -1;

	// The active pointer is the one currently moving our object.
	private int activePointerId = INVALID_POINTER_ID;
	
	// Gesture Stuff
	private ScaleGestureDetector sgd;
	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;
	private float scaleFactor = 1.0f;
	
	private ActivityCompositionCanvas activity;
	private Registry registry;
	
	public CompositionView(Context context) 
	{
		super(context);
		setup();
	}
	
	public CompositionView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setup();
	}
	
	public CompositionView(Context context, AttributeSet attrs, int something)
	{
		super(context, attrs, something);
		setup();
	}
	
	public void setup()
	{
		paint = new Paint();
		activity = (ActivityCompositionCanvas) this.getContext();
		
		sgd = new ScaleGestureDetector(activity, new ScaleGestureListener());
//		gestureDetector = new GestureDetector(activity, new CanvasGestureDetector());
//		gestureListener = new View.OnTouchListener() 
//		{
//			@Override
//			public boolean onTouch(View v, MotionEvent event) 
//			{
//				return gestureDetector.onTouchEvent(event);
//			}
//		};
//		this.setOnTouchListener(gestureListener);
		
		Point p = new Point();
		Display d = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		d.getSize(p);
		
		float scale = activity.getResources().getDisplayMetrics().density;
		int px = (int) (48 * scale + 0.5);
		
		deviceWidth = p.x;
		deviceHeight = p.y - px;
		
		regions = new ArrayList<ComponentRegion>();
		
		// Initially set the canvas size to be the same as the screen size
		canvasWidth = deviceWidth;
		canvasHeight = deviceHeight;
		
		registry = Registry.getInstance(activity);

		background = loadBackground();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		sgd.onTouchEvent(ev);
		
		final int action = ev.getAction();
		switch(action & MotionEvent.ACTION_MASK)
		{
			case MotionEvent.ACTION_DOWN:
			{
				final float x = ev.getX();
				final float y = ev.getY();
				
				lastX = x;
				lastY = y;
				
				activePointerId = ev.getPointerId(0);
				break;
			}
			
			case MotionEvent.ACTION_MOVE:
			{
				final int pointerIndex = ev.findPointerIndex(activePointerId);
				final float x = ev.getX(pointerIndex);
				final float y = ev.getY(pointerIndex);
				
				// Only move if the gesture detector isn't processing a gesture
				if(!sgd.isInProgress())
				{
					final float dx = x - lastX;
					final float dy = y - lastY;
					
					posX += dx;
					posY += dy;
					
					invalidate();
				}
				
				lastX = x;
				lastY = y;
				
				break;
			}
			
			case MotionEvent.ACTION_UP:
				activePointerId = INVALID_POINTER_ID;
				break;
		
			case MotionEvent.ACTION_CANCEL:
				activePointerId = INVALID_POINTER_ID;
				break;
				
			case MotionEvent.ACTION_POINTER_UP:
			{
				final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = ev.getPointerId(pointerIndex);
				
				if(pointerId == activePointerId)
				{
					 final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			            lastX = ev.getX(newPointerIndex);
			            lastY = ev.getY(newPointerIndex);
			            activePointerId = ev.getPointerId(newPointerIndex);
				}
				
				break;
			}
		}
		
		return true;
	}
	
	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas)
	{
		ArrayList<ServiceDescription> components = registry.getService().getComponents();
		
		super.onDraw(canvas);
		canvas.save();
		
		canvas.translate(posX, posY);
		canvas.scale(scaleFactor, scaleFactor);
		
		
		regions.clear();
		
		canvasWidth = deviceWidth + (components.size() * (COMPONENT_WIDTH + COMPONENT_GAP));
		canvasHeight = deviceHeight;
		
		canvas.drawBitmap(background, 0, 0, paint);
		
		if(components.size() == 0)
		{
			// Put in a placeholder component
			int left = deviceWidth / 2 - COMPONENT_WIDTH / 2;
			int top = deviceHeight / 2 - COMPONENT_HEIGHT / 2;
			canvas.drawBitmap(loadPlaceholder(), left, top, paint);
			
			regions.add(new ComponentRegion(null, new Region(left, top, left + COMPONENT_WIDTH, top + COMPONENT_HEIGHT)));
		}
		else
		{	
			for(int i = 0; i < components.size() ; i++)
			{
				int left = (i * (COMPONENT_WIDTH + COMPONENT_GAP)) + deviceWidth / 2 - COMPONENT_WIDTH / 2;
				int top = deviceHeight / 2 - COMPONENT_HEIGHT / 2;
				canvas.drawBitmap(createComponentBitmap(i, components.get(i)), left, top, paint);
				
				regions.add(new ComponentRegion(components.get(i), new Region(left, top, left + COMPONENT_WIDTH, top + COMPONENT_HEIGHT)));
			}
			
			// Draw the connections before we draw the placeholder??
			drawConnections(components, canvas, (int) posX);
			
			int left = components.size() * (COMPONENT_WIDTH + COMPONENT_GAP) + deviceWidth / 2 - COMPONENT_WIDTH / 2;
			int top = deviceHeight / 2 - COMPONENT_HEIGHT / 2;
			canvas.drawBitmap(loadPlaceholder(), left, top, paint);
			
			regions.add(new ComponentRegion(null, new Region(left, top, left + COMPONENT_WIDTH, top + COMPONENT_HEIGHT)));
		}
		
		canvas.restore();
	}

	public Bitmap loadBackground() 
	{
		Drawable bg = this.getContext().getResources().getDrawable(R.drawable.bg);
		Bitmap b = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		
		bg.setBounds((int) posX, (int) posY, (int) posX + canvasWidth * 5, (int) posY + canvasHeight);
		bg.draw(c);

		return b;
	}
	
	public Bitmap loadPlaceholder()
	{
		Drawable placeholder = this.getContext().getResources().getDrawable(R.drawable.dc_placeholder);
		Bitmap b = Bitmap.createBitmap(COMPONENT_WIDTH, COMPONENT_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		
		placeholder.setBounds(0, 0, COMPONENT_WIDTH, COMPONENT_HEIGHT);
		placeholder.draw(c);
		
		Paint p = new Paint();
		p.setColor(getContext().getResources().getColor(R.color.android_darkpurple));
		
		String text = "+";
		p.setTextSize(200);
		float w = p.measureText(text);
		c.drawText(text, (COMPONENT_WIDTH / 2 - w / 2), (COMPONENT_HEIGHT / 2), p);
		
		text = "Add component";
		p.setTextSize(30);
		w = p.measureText(text);
		c.drawText(text, (COMPONENT_WIDTH / 2 - w / 2), (3 * COMPONENT_HEIGHT / 4), p);
		
		return b;
	}
	
	public Bitmap createComponentBitmap(int index, ServiceDescription component)
	{
		// Setup the background of the component
		Resources r = this.getContext().getResources();
		Drawable inputDrawable = r.getDrawable(R.drawable.inputs);
		Drawable outputDrawable = r.getDrawable(R.drawable.outputs);
		Drawable eitherDrawable = r.getDrawable(R.drawable.puts_in_out);
		
		Bitmap b = Bitmap.createBitmap((COMPONENT_WIDTH + MAND_WIDTH), COMPONENT_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		
		if(component.hasInputs())
		{
			eitherDrawable.setBounds(0, 0, COMPONENT_WIDTH / 5, COMPONENT_HEIGHT);
			eitherDrawable.draw(c);
		}
		else
		{
			inputDrawable.setBounds(0, 0, COMPONENT_WIDTH / 5, COMPONENT_HEIGHT);
			inputDrawable.draw(c);
		}
		
		if(component.hasOutputs())
		{
			eitherDrawable.setBounds(4 * COMPONENT_WIDTH / 5, 0, COMPONENT_WIDTH, COMPONENT_HEIGHT);
			eitherDrawable.draw(c);
		}
		else
		{
			outputDrawable.setBounds(4 * COMPONENT_WIDTH / 5, 0, COMPONENT_WIDTH, COMPONENT_HEIGHT);
			outputDrawable.draw(c);
		}
		
		// Bars at the top and the bottom
		Paint p = new Paint();
		p.setColor(r.getColor(R.color.comp_item_border));
		c.drawRect(COMPONENT_WIDTH / 5, 0, 4 * COMPONENT_WIDTH / 5, 5, p);
		c.drawRect(COMPONENT_WIDTH / 5, COMPONENT_HEIGHT - 5, 4 * COMPONENT_WIDTH / 5, COMPONENT_HEIGHT, p);
		
		// Work out how long the text is and decide how to draw it, also maybe make it a bit bigger
		
		
		// Set the name of the component
		p.setColor(Color.BLACK);
		p.setTextSize(20);
		c.drawText(component.getName(), COMPONENT_WIDTH / 5 + 10, 2 * COMPONENT_HEIGHT / 3, p);
		
		int iconSize = 2 * COMPONENT_WIDTH / 5;
		int left = COMPONENT_WIDTH / 2 - iconSize / 2;
		int top = COMPONENT_WIDTH / 10;
		
		try 
		{
			String iconLocation = component.getApp().getIconLocation();
			Bitmap componentIcon = LocalStorage.getInstance().readIcon(iconLocation);
			
			if(componentIcon == null)
			{
				// We've not been able to get the icon, so load a default
				componentIcon = BitmapFactory.decodeResource(r, R.drawable.icon);
			}

			c.drawBitmap(componentIcon, 
						 new Rect(0, 0, componentIcon.getWidth(), componentIcon.getHeight()), 
						 new Rect(left, top, left + iconSize, top + iconSize), 
						 p);
		}
		catch (IOException e) 
		{
			Drawable defaultIcon = r.getDrawable(R.drawable.icon);
			defaultIcon.setBounds(left, top, left + iconSize, top + iconSize);
			defaultIcon.draw(c);
		}
		
		
		// Populate the inputs and outputs of the component
		if(component.hasInputs())
		{
			ArrayList<ServiceIO> inputs = component.getInputs();
			int gap = IO_SPACE / (inputs.size() + 1);
			
			Drawable outsideDrawable = r.getDrawable(R.drawable.input_out);
			Drawable insideDrawable = r.getDrawable(R.drawable.input_in_off);
			
			Paint p2 = new Paint();
			p.setColor(getResources().getColor(R.color.comp_item_border));
			p2.setColor(Color.WHITE);
			
			Drawable insideOnDrawable = null;
			
			for(int i = 0; i < inputs.size(); i++)
			{
				int y = (i + 1) * gap + IO_PADDING;
				
				ServiceIO input = inputs.get(i);
				
				if(input.isMandatory())
				{
					outsideDrawable.setBounds(0, y, MAND_WIDTH, y + MAND_HEIGHT);
					insideDrawable.setBounds(0, y + 2, MAND_WIDTH - BORDER, y + MAND_HEIGHT - BORDER);
					
					if(input.getConnection() != null)
					{
						insideOnDrawable = r.getDrawable(R.drawable.input_in_link);
						insideOnDrawable.setBounds(2, y + 2, MAND_WIDTH - BORDER, y + MAND_HEIGHT - BORDER);
					}
					else if(input.isFiltered() != ServiceIO.UNFILTERED)
					{
						insideOnDrawable = r.getDrawable(R.drawable.input_in_value);
						insideOnDrawable.setBounds(2, y + 2, MAND_WIDTH - BORDER, y + MAND_HEIGHT - BORDER);
					}
				}
				else
				{
					outsideDrawable.setBounds(0, y, NON_MAND_WIDTH, y + NON_MAND_HEIGHT);
					insideDrawable.setBounds(0, y + 2, NON_MAND_WIDTH - BORDER, y + NON_MAND_HEIGHT - BORDER);

					if(input.getConnection() != null)
					{
						insideOnDrawable = r.getDrawable(R.drawable.input_in_link);
						insideOnDrawable.setBounds(2, y + 2, NON_MAND_WIDTH - BORDER, y + NON_MAND_HEIGHT - BORDER);
					}
					else if(input.hasValue())
					{
						insideOnDrawable = r.getDrawable(R.drawable.input_in_value);
						insideOnDrawable.setBounds(2, y + 2, NON_MAND_WIDTH - BORDER, y + NON_MAND_HEIGHT - BORDER);
					}
				}
				
				outsideDrawable.draw(c);
				insideDrawable.draw(c);
				
				if(insideOnDrawable != null)
					insideOnDrawable.draw(c);
			}
		}
		
		// The output links need to look like the input ones
		// The wires need to point at the middle of the things and be more representative of the other stuff
		
		if(component.hasOutputs())
		{
			Drawable outsideDrawable = r.getDrawable(R.drawable.input_out);
			
			ArrayList<ServiceIO> outputs = component.getOutputs();
			int gap = IO_SPACE / (outputs.size() + 1);
			
			for(int i = 0; i < outputs.size(); i++)
			{
				int y = (i + 1) * gap + IO_PADDING;
				
				outsideDrawable.setBounds(COMPONENT_WIDTH - NON_MAND_WIDTH / 2, y, 
										  COMPONENT_WIDTH + NON_MAND_WIDTH / 2, y + NON_MAND_HEIGHT);
				outsideDrawable.draw(c);
			}
		}
		
		if(activity.getSelectedIndex() == index)
			return getHighlight(b);
		else
			return b;
	}
	
	public Bitmap getHighlight(Bitmap b)
	{
		if(b == null)
			return null;
		
		int think = 6;
		
		int w = b.getWidth();
		int h = b.getHeight();
		
		int newW = w - think;
		int newH = h - think;
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(w, h, conf);
	    Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, newW, newH, false);
	    
	    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    Canvas c = new Canvas(bmp);
	    
	    
	 // Right
	    Shader rShader = new LinearGradient(newW, 0, w, 0, Color.GRAY, Color.LTGRAY, Shader.TileMode.CLAMP);
	    paint.setShader(rShader);
	    c.drawRect(newW, think, w, newH, paint);

	    // Bottom
	    Shader bShader = new LinearGradient(0, newH, 0, h, Color.GRAY, Color.LTGRAY, Shader.TileMode.CLAMP);
	    paint.setShader(bShader);
	    c.drawRect(think, newH, newW  , h, paint);

	    //Corner
	    Shader cShader = new LinearGradient(0, newH, 0, h, Color.LTGRAY, Color.LTGRAY, Shader.TileMode.CLAMP);
	    paint.setShader(cShader);
	    c.drawRect(newW, newH, w  , h, paint);


	    c.drawBitmap(scaledBitmap, 0, 0, null);
		
		return bmp;
	}
	
	public void drawConnections(ArrayList<ServiceDescription> components, Canvas c, int xPos)
	{
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeJoin(Paint.Join.ROUND);
		p.setStrokeCap(Paint.Cap.ROUND);
		p.setAntiAlias(true);
		p.setStrokeWidth(2);
		
		Paint p2 = new Paint();
		p2.setColor(this.getContext().getResources().getColor(R.color.android_purple));
		
		// The one on the end is always the add component button
		for(int i = 0; i < components.size() - 1; i++)
		{
			// Get the links between the two different components
			ServiceDescription first = components.get(i);
			ServiceDescription second = components.get(i + 1);
			
			if(first == null || second == null)
				continue;
			
			// Get the links from the first to the second
			if(!first.hasOutputs() || !second.hasInputs())
				continue;
			
			// The left position of the line is the right hand size of the first component
			int right = (i + 1) * (COMPONENT_WIDTH + COMPONENT_GAP) + xPos + deviceWidth / 2 - COMPONENT_WIDTH / 2;
			int left = right - COMPONENT_GAP + NON_MAND_WIDTH / 2;
			
			ArrayList<ServiceIO> outputs = first.getOutputs();
			ArrayList<ServiceIO> inputs = second.getInputs();
			for(int j = 0; j < outputs.size(); j++)
			{
				ServiceIO input = outputs.get(j).getConnection();
				
				if(input == null)
					continue;
				
				// Draw a line between the two places
				int oGap = IO_SPACE / (outputs.size() + 1);
				int iGap = IO_SPACE / (inputs.size() + 1);
				int inputHeight = input.isMandatory() ? MAND_HEIGHT : NON_MAND_HEIGHT;
				
				int leftTop = oGap * (j + 1) + IO_PADDING + deviceHeight / 2 - COMPONENT_HEIGHT / 2 + NON_MAND_HEIGHT / 2;
				int rightTop = iGap * (input.getIndex() + 1) + IO_PADDING + deviceHeight / 2 - COMPONENT_HEIGHT / 2; 
				
				//c.drawArc(oval, startAngle, sweepAngle, useCenter, p)
				
				c.drawLine(	left, leftTop,
							right, 
							rightTop + inputHeight / 2, 
							p);
				
				// Draw something to fill in the 
				c.drawRect(right, rightTop, right + BORDER, rightTop + inputHeight, p2);
			}
		}
	}
	
	private class ComponentRegion
	{
		private ServiceDescription component;
		private Region region;
		
		private ComponentRegion(ServiceDescription component, Region region)
		{
			this.component = component;
			this.region = region;
		}
	}
	
//	private class CanvasGestureDetector extends SimpleOnGestureListener
//	{
//		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
//		{
//			int newX = posX - (int) distanceX;
//			int newY = posY - (int) distanceY;
//			
//			// If we're already as far left as we can go, don't go any further left
//			// If we're already as far right as we can go, don't go any further right
//			if(newX < deviceWidth - canvasWidth)
//				posX = deviceWidth - canvasWidth;
//			else if(newX > 0)
//				posX = 0;
//			else
//				posX = newX;
//			
//			// If we're already as far up as we can go, don't go any further up
//			// If we're already as far down as we can go, don't go any further down
//			if(newY < deviceHeight - canvasHeight)
//				posY = deviceHeight - canvasHeight;
//			else if(newY > 0)
//				posY = 0;
//			else
//				posY = newY;
//			
//			CompositionView.this.invalidate();
//			return true;
//		}	
//		
//		public boolean onDown(MotionEvent event)
//		{
//			return true;
//		}
//		
//		public boolean onSingleTapUp(MotionEvent event)
//		{
//			float posX = event.getX();
//			float posY = event.getY();
//			
//			for(int i = 0; i < regions.size(); i++)
//			{
//				ComponentRegion region = regions.get(i);
//				if(region.region.contains((int) posX, (int) posY))
//				{
//					if(region.component == null)
//					{
//						// Then we need to go to add a component
//						activity.add();
//					}
//					else
//					{
//						float newX = posX - region.region.getBounds().left;
//						
//						if(newX <= COMPONENT_WIDTH / 5)
//						{
//							// If it's less than something then it's in the input bar
//							activity.wire(i);
//						}
//						else if(newX >= 4 * COMPONENT_WIDTH / 5)
//						{
//							// If it's greater than something else than it's in the output bar
//							activity.wire(i + 1);
//						}
//						else
//						{
//							// Otherwise it's in the middle - bring up the actions we can perform
//							if(activity.getActionMode() != null)
//							{
//								activity.getActionMode().finish();
//								activity.startActionMode(i, region.component);
//							}
//							
//							activity.startActionMode(i, region.component);
//						}
//					}
//				}
//			}
//			
//			// If we've got this far then the click is outside of all of the things
//			if(activity.getActionMode() != null)
//				activity.getActionMode().finish();
//			
//			
//			return true;
//		}
//	}
	
	private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{		
		public boolean onScale(ScaleGestureDetector detector)
		{
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

//			if(scaleFactor < 5f)
//			{
//
//				float focusX = detector.getFocusX();
//				float focusY = detector.getFocusY();
//
//				float diffX = focusX - posX;
//				float diffY = focusY - posY;
//
//				diffX = diffX * scaleFactor - diffX;
//				diffY = diffY * scaleFactor - diffY;
//
//				posX -= diffX;
//				posY -= diffY;
//			}
			
			invalidate();
			return true;
		}
	}
}
