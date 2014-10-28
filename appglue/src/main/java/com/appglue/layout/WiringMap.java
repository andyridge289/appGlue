package com.appglue.layout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.appglue.ActivityWiring;
import com.appglue.FragmentWiring;
import com.appglue.IODescription;
import com.appglue.R;
import com.appglue.TST;
import com.appglue.description.datatypes.IOType;
import com.appglue.description.datatypes.Set;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.IOFilter;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.animation.WeightedExpandAnimation;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import static com.appglue.Constants.FULL_ALPHA;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class WiringMap extends LinearLayout implements Comparator<IODescription>, AbsListView.OnScrollListener {

    private ComponentService first;
    private ComponentService second;

    private FrameLayout outputFrame;
    private ListView outputList;
    private OutputAdapter outputAdapter;
    private SparseIntArray outputPositions;
    private Queue<Integer> outputOffsets;
    private View outputContainer;
    private View noInputs;
    private View addInput;

    private FrameLayout inputFrame;
    private ListView inputList;
    private InputAdapter inputAdapter;
    private SparseIntArray inputPositions;
    private Queue<Integer> inputOffsets;
    private View inputContainer;
    private View noOutputs;
    private View addOutput;

    private View filterFrame;
    private View noFilters;
    private ListView filterList;
    private View addFilter;
    private TextView filterAndor;
    private FilterAdapter filterAdapter;

    private static final int LOWLIGHT_ALPHA = 10;
    private static final int HIGHLIGHT_ALPHA = 5;
    private static final int BASE_ALPHA = 2;

    private ActivityWiring activity;
    private int wiringMode;

    private ServiceIO iSelected;
    private int iIndex;

    private ServiceIO oSelected;
    private int oIndex;

    TST<Integer> hueMap;
    private Registry registry;

    private ArrayList<Point> connections;
    private final Object lock = new Object();
    private int position;

    public WiringMap(Context context) {
        super(context);
        create(context);
    }

    public WiringMap(Context context, AttributeSet attributes) {
        super(context, attributes);
        create(context);
    }

    public WiringMap(Context context, AttributeSet attributes, int defStyle) {
        super(context, attributes, defStyle);
        create(context);
    }

    public void create(Context context) {
        this.setWillNotDraw(false);

        this.activity = (ActivityWiring) context;

        View v = View.inflate(context, R.layout.wiring_map, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.addView(v);

        registry = Registry.getInstance(context);

        inputFrame = (FrameLayout) findViewById(R.id.wiring_input_frame);
        outputFrame = (FrameLayout) findViewById(R.id.wiring_output_frame);
        filterFrame = findViewById(R.id.wiring_filter_frame);

        outputList = (ListView) findViewById(R.id.output_list);
        outputList.setClickable(false);
        outputList.setOnScrollListener(this);

        inputList = (ListView) findViewById(R.id.input_list);
        inputList.setClickable(false);
        inputList.setOnScrollListener(this);

        filterList = (ListView) findViewById(R.id.wiring_filter_list);

        outputContainer = findViewById(R.id.outputs);
        inputContainer = findViewById(R.id.inputs);

        noOutputs = findViewById(R.id.no_outputs);
        noInputs = findViewById(R.id.no_inputs);
        noFilters = findViewById(R.id.wiring_no_filters);
        filterAndor = (TextView) findViewById(R.id.wiring_filter_andor);

        addOutput = findViewById(R.id.add_output);
        addInput = findViewById(R.id.add_input);

        connections = new ArrayList<Point>();

        inputOffsets = new LinkedList<Integer>();
        outputOffsets = new LinkedList<Integer>();

        iIndex = -1;
        oIndex = -1;

        wiringMode = -1;
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
        for (int i = 0; i < connections.size(); i++) {
            Point q = connections.get(i);

            if (p.x == q.x && p.y == q.y) {
                // Remove it and decrement so that we carry on checking. Although we could just return...
                connections.remove(i);
                i--;
            }
        }

    }

    public ArrayList<Point> getConnectionsOut(int outputIndex) {
        ArrayList<Point> points = new ArrayList<Point>();

        for (Point connection : connections) {
            if (connection.x == outputIndex)
                points.add(connection);
        }

        return points;
    }

    public ArrayList<Point> getConnectionsIn(int inputIndex) {
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


    public void set(final ComponentService first, final ComponentService second, int mode) {
        this.first = first;
        if (first != null) {
            if (first.getDescription().getOutputs().size() > 0) {
                // There are getOutputs, show the list, hide the none and the add
                outputAdapter = new OutputAdapter(activity, first.getOutputs());
                outputList.setAdapter(outputAdapter);
                outputContainer.setVisibility(View.VISIBLE);
                noOutputs.setVisibility(View.INVISIBLE);
                addOutput.setVisibility(View.INVISIBLE);
            } else {
                // There are no getInputs, show the none, hide the list and the add
                outputContainer.setVisibility(View.INVISIBLE);
                noOutputs.setVisibility(View.VISIBLE);
                addOutput.setVisibility(View.INVISIBLE);
            }

            if (first.hasFilters()) {
                Log.d(TAG, first.getFilters().size() + " filters for " + first.getID() + "(" + first.getDescription().getName() + ")");
                filterAdapter = new FilterAdapter(getContext(), first.getFilters());
                filterList.setAdapter(filterAdapter);
                filterList.setVisibility(View.VISIBLE);
                noFilters.setVisibility(View.GONE);

                filterAndor.setText(first.getFilterCondition() ? "OR" : "AND");
                filterAndor.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (first.getFilterCondition()) {
                            first.setFilterCondition(false);
                            filterAndor.setText("AND");
                        } else {
                            first.setFilterCondition(true);
                            filterAndor.setText("OR");
                        }
                        filterAdapter.notifyDataSetChanged();
                    }
                });
                if (first.getFilters().size() > 1) {
                    filterAndor.setVisibility(View.VISIBLE);
                } else {
                    filterAndor.setVisibility(View.GONE);
                }
            } else {
                Log.d(TAG, "Nope, no filters for " + first.getID() + "(" + first.getDescription().getName() + ")");
            }
        } else {
            // There isn't a service here. Hide the list and the text that says NONE
            outputContainer.setVisibility(View.INVISIBLE);
            noOutputs.setVisibility(View.INVISIBLE);
            addOutput.setVisibility(View.VISIBLE);
        }

        this.second = second;
        if (second != null) {
            if (second.getDescription().getInputs().size() > 0) {
                inputAdapter = new InputAdapter(activity, second.getInputs());
                inputList.setAdapter(inputAdapter);
                inputContainer.setVisibility(View.VISIBLE);
                noInputs.setVisibility(View.INVISIBLE);
                addInput.setVisibility(View.INVISIBLE);
            } else {
                inputContainer.setVisibility(View.INVISIBLE);
                noInputs.setVisibility(View.VISIBLE);
                addInput.setVisibility(View.INVISIBLE);
            }
        } else {
            inputContainer.setVisibility(View.INVISIBLE);
            noInputs.setVisibility(View.INVISIBLE);
            addInput.setVisibility(View.VISIBLE);
        }

        // Check if there are any set things.
        if (second != null && second.getInputs() != null) {
            ArrayList<ServiceIO> in = second.getInputs();
            for (int i = 0; i < in.size(); i++) {
                ServiceIO connection = in.get(i).getConnection();
                if (connection != null) {
                    // It's connected to something so work out what position the other thing is in the getOutputs
                    connections.add(new Point(connection.getDescription().getIndex(), i));
                }
            }
        }

        addFilter = findViewById(R.id.wiring_filter_add);
        addFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.filter(first.getID(), -1, position);
            }
        });

        hueGeneration();
        this.wiringMode = mode;
        redraw(true); // There's a post invalidate in redraw
    }

    private void hueGeneration() {
        // Count the number of distinct IO types that are available across both services
        ArrayList<IODescription> ios = new ArrayList<IODescription>();
        if (second != null && second.getDescription().getInputs() != null)
            ios.addAll(second.getDescription().getInputs());
        if (first != null && first.getDescription().getOutputs() != null)
            ios.addAll(first.getDescription().getOutputs());

        ArrayList<IOType> distinctTypes = new ArrayList<IOType>();
        Collections.sort(ios, WiringMap.this);

        // Sort by IOType then add the different ones
        String previous = "";
        for (IODescription io : ios) {
            IOType type = io.getType();

            if (type.getClassName().equals(previous) && !type.getClassName().equals(Set.class.getCanonicalName()))
                continue;

            previous = type.getClassName();
            distinctTypes.add(type);
        }

        if (distinctTypes.size() == 0)
            return;

        // Create colours equally spaced around the Hue spectrum but drop the Saturation
        int gap = 360 / distinctTypes.size();
        hueMap = new TST<Integer>();
        for (int i = 0; i < distinctTypes.size(); i++) {
            hueMap.put(distinctTypes.get(i).getClassName(), i * gap);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float scale = activity.getResources().getDisplayMetrics().density;
        paint.setStrokeWidth(4 * scale);

        ArrayList<PathColour> paths = getPaths();
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
    public void onDraw(Canvas canvas) {
        // Don't know if this actually does anything?
        super.onDraw(canvas);

        // Reset the paint in case something else has changed it

    }

    @Override
    public int compare(IODescription a, IODescription b) {
        if (a.getType() == null)
            Log.e(TAG, "a Type null");

        if (b.getType() == null)
            Log.e(TAG, "b type null");

        return a.getType().getClass().getCanonicalName().compareTo(b.getType().getClassName());
    }

    private ArrayList<PathColour> getPaths() {
        ArrayList<PathColour> paths = new ArrayList<PathColour>();

        for (Point connection : connections) {
//            Path path = new Path();
            // Need to get the class of one of the items

            View selectedOutput = outputList.getChildAt(connection.x);
            View selectedInput = inputList.getChildAt(connection.y);

            if (selectedInput == null || selectedOutput == null)
                continue; // Probably just try again at the next redraw. It probably doesn't like the animations

            if (connection.x == -1 || connection.y == -1) {
                Log.e(TAG, "Path epic failure, not really sure why");
                continue;
            }

            String className = first.getDescription().getOutputs().get(connection.x).getType().getClassName();
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

    private void setHighlight(IOType type, ViewGroup parent) {
        // Set it so that the highlight isn't applied in the list in which the thing happened

        if (outputContainer.getVisibility() == View.VISIBLE) {
            setListAlpha(outputList, first.getDescription().getOutputs(), 0, null, false);

            if (!parent.equals(outputList) && type != null)
                setListAlpha(outputList, first.getDescription().getOutputs(), FULL_ALPHA / LOWLIGHT_ALPHA, type, false);
        }

        if (inputContainer.getVisibility() == View.VISIBLE) {
            setListAlpha(inputList, second.getDescription().getInputs(), 0, null, true);

            if (!parent.equals(inputList) & type != null)
                setListAlpha(inputList, second.getDescription().getInputs(), FULL_ALPHA / LOWLIGHT_ALPHA, type, true);
        }
    }

    private void setListAlpha(ListView lv, ArrayList<IODescription> ioList, int alpha, IOType type, boolean inputs) {
        if (ioList == null)
            return;

        if (type == null) {
            for (int i = 0; i < lv.getChildCount(); i++) {
                View vg = lv.getChildAt(i);
                vg.findViewById(R.id.endpoint).setBackgroundColor(Color.WHITE);
            }
        } else {
            String className = type.getClass().getCanonicalName();
            for (int i = 0; i < lv.getChildCount(); i++) {
                String itemClassName = ioList.get(i).getType().getClassName();

                if (inputConnection(i) && inputs)
                    continue;

                if (itemClassName.equals(className)) {
                    View vg = lv.getChildAt(i);
                    vg.findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(alpha, new float[]{hueMap.get(itemClassName), 1, 1}));
                }
            }
        }
    }

    public void redraw(boolean refreshLists) {

        if (wiringMode == -1) {
            // If we don't have one yet then it should have defaulted to wiring.
            wiringMode = FragmentWiring.MODE_WIRING;
        }

        float inputWeight;
        float outputWeight;
        float filterWeight;

        switch (wiringMode) {
            case FragmentWiring.MODE_FILTER:
                outputWeight = 1.0f;
                inputWeight = 1.0f;
                filterWeight = 2.0f;
                if (outputAdapter != null) {
                    outputAdapter.contract();
                }
                if (inputAdapter != null) {
                    inputAdapter.contract();
                }
                break;

            case FragmentWiring.MODE_VALUE:
                outputWeight = 1.0f;
                inputWeight = 3.0f;
                filterWeight = 0.0f;
                if (outputAdapter != null) {
                    outputAdapter.contract();
                }
                if (inputAdapter != null) {
                    inputAdapter.expand();
                }
                break;

            default: // Wiring mode I guess, or just anything else
                outputWeight = 1.0f;
                inputWeight = 1.0f;
                filterWeight = 0.0f;
                if (outputAdapter != null) {
                    outputAdapter.reset();
                }
                if (inputAdapter != null) {
                    inputAdapter.reset();
                }
                break;
        }

        Log.d(TAG, String.format("Weights: %f %f %f", outputWeight, filterWeight, inputWeight));

        WeightedExpandAnimation ia = new WeightedExpandAnimation(inputFrame, ((LayoutParams) inputFrame.getLayoutParams()).weight, inputWeight);
        ia.setDuration(500);
        inputFrame.startAnimation(ia);

        WeightedExpandAnimation oa = new WeightedExpandAnimation(outputFrame, ((LayoutParams) outputFrame.getLayoutParams()).weight, outputWeight);
        oa.setDuration(500);
        outputFrame.startAnimation(oa);

        WeightedExpandAnimation fa = new WeightedExpandAnimation(filterFrame, ((LayoutParams) filterFrame.getLayoutParams()).weight, filterWeight);
        fa.setDuration(500);
        filterFrame.startAnimation(fa);

        this.postInvalidate();

        if (outputList != null && refreshLists) {
            this.outputList.invalidateViews();
        }

        if (refreshLists && inputList != null) {
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

    /**
     * This shows the dialog that is used to set values.
     *
     * @param item The item for the dialog to show.
     */
    private void showIODialog(final ServiceIO item) {
        DialogIO di = new DialogIO(activity, item, null);
        di.show();
    }

    public void setWiringMode(int wiringMode) {
        this.wiringMode = wiringMode;
        redraw(true);
    }

    public void autoConnect() {

        if (first == null || second == null) {
            return;
        }

        ArrayList<ServiceIO> outputs = first.getDisconnectedOutputs();
        ArrayList<ServiceIO> inputs = second.getDisconnectedInputs();

        for (int i = 0; i < outputs.size(); ) {
            ServiceIO out = outputs.get(i);
            IOType type = out.getType();

            boolean removed = false;

            for (int j = 0; j < inputs.size(); j++) {
                ServiceIO in = inputs.get(j);
                if (type.typeEquals(in.getType())) {

                    Handler h = new Handler();
                    h.postDelayed(new RedrawRunnable(out.getDescription().getIndex(),
                            in.getDescription().getIndex()), 200);
                    Log.d(TAG, String.format("Connecting %d to %d", out.getDescription().getIndex(),
                            in.getDescription().getIndex()));

                    outputs.remove(i);
                    inputs.remove(j);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                i++;
            }
        }

    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    private class InputAdapter extends WiringIOAdapter {

        public InputAdapter(Context parent, ArrayList<ServiceIO> items) {
            super(parent, R.layout.list_item_wiring_in, items);
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_wiring_in, null);
            }

            final View v = convertView;

            final ServiceIO item = items.get(position);
            final IODescription iod = item.getDescription();

            final ImageView setButton = (ImageView) v.findViewById(R.id.set_button);

            final View endpoint = v.findViewById(R.id.endpoint);
            final Drawable blob = v.findViewById(R.id.blob).getBackground();
            final Drawable stub = v.findViewById(R.id.stub).getBackground();

            int col = Color.HSVToColor(FULL_ALPHA / BASE_ALPHA,
                    new float[]{
                            hueMap.get(iod.getType().getClassName()),
                            1,
                            1
                    }
            );

            blob.setColorFilter(col, PorterDuff.Mode.ADD);
            stub.setColorFilter(col, PorterDuff.Mode.ADD);

            TextView ioName = (TextView) v.findViewById(R.id.io_name);
            ioName.setText(iod.getFriendlyName());

            TextView ioType = (TextView) v.findViewById(R.id.io_type);
            TextView ioValue = (TextView) v.findViewById(R.id.io_value);

            int visibility = iod.isMandatory() ? View.VISIBLE : View.GONE;
            v.findViewById(R.id.mandatory_bar).setVisibility(visibility);

            if (item != null) {
                if (!item.hasValue()) {
                    ioType.setText(iod.getType().getName());
                    ioValue.setText("");
                } else {
                    ioType.setText(iod.getType().getName() + ": ");
                    String valueName = item.getType().toString(item.getValue().getManualValue());
                    ioValue.setText(valueName);
                }
            }

            endpoint.setVisibility(View.VISIBLE);


            int[] pos = new int[2];
            endpoint.getLocationOnScreen(pos);

            endpoint.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View b) {
                    if (inputConnection(position) || first == null || !first.getDescription().hasOutputs()) {
                        if (LOG) Log.d(TAG, "No highlights");
                    } else if (iSelected == null && oSelected == null) {
                        // If they click an output first then just work normally and connect it for them
                        setHighlight(iod.getType(), parent);
                        b.setBackgroundColor(
                                Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(iod.getType().getClassName()),
                                                1,
                                                1
                                        }
                                ));

                        iSelected = item;
                        iIndex = position;
//                        activity.setStatus("Selected " + iod.getName());
                    } else if (oSelected != null && oSelected.getDescription().getType().equals(iod.getType()) && iSelected == null) {
                        if (LOG) Log.d(TAG, "Input " + position + " We have a match");

                        // If the current input and output are in the connections list then do nothing. That would be stupid
                        if (checkConnection(oIndex, position) || inputConnection(position))
                            return;

                        setHighlight(null, parent);

                        iSelected = item;
                        iIndex = position;
//                        activity.setStatus("Selected " + iod.getName());

                        // This one
                        b.setBackgroundColor(
                                Color.HSVToColor(
                                        FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(iod.getType().getClassName()),
                                                1,
                                                1
                                        }
                                )
                        );

                        // The corresponding output needs to be put back
                        View outElement = outputList.getChildAt(oIndex);
                        View outEndpoint = outElement.findViewById(R.id.endpoint);
                        String className = oSelected.getDescription().getType().getClassName();
                        Integer hue = hueMap.get(className);
                        int colour = Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA, new float[]{hue, 1, 1});
                        outEndpoint.setBackgroundColor(colour);

                        redraw(true);

                        // Update the drawing a bit later...
                        Handler h = new Handler();
                        h.postDelayed(new RedrawRunnable(oIndex, iIndex), 200);
                    } else if (iSelected != null && oSelected == null) {
                        if (LOG)
                            Log.d(TAG, "Output " + position + " (output is null, input is not)");
                        // This means that we need to deselect the current one?
                        setHighlight(null, parent);

                        if (iIndex == position) {
                            // We don't have a new one
                            iSelected = null;
                            iIndex = -1;
                        } else {
                            setHighlight(iod.getType(), parent);
                            b.setBackgroundColor(
                                    Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA,
                                            new float[]{
                                                    hueMap.get(iod.getType().getClassName()),
                                                    1,
                                                    1
                                            }
                                    ));

                            iSelected = item;
                            iIndex = position;
                            activity.setStatus("Selected " + iod.getName());
                        }

                        redraw(true);
                    } else {
                        setHighlight(iod.getType(), parent);

                        b.setBackgroundColor(
                                Color.HSVToColor(
                                        FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(iod.getType().getClassName()),
                                                1,
                                                1
                                        }
                                )
                        );

                        iSelected = item;
                        iIndex = position;
                        oSelected = null;
                        oIndex = -1;
                    }
                }
            });

            if (item.hasValue()) {
                setButton.setImageResource(R.drawable.ic_settings_black_48dp);
            } else {
                setButton.setImageResource(R.drawable.ic_add_black_48dp);
            }

            setButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showIODialog(item);
                }
            });

            if (this.isExpanded()) {
                if (item.getConnection() == null) {
                    endpoint.setVisibility(View.GONE);
                    setButton.setVisibility(View.VISIBLE);
                }
                ioName.setVisibility(View.VISIBLE);
                ioType.setVisibility(View.VISIBLE);
                ioValue.setVisibility(View.VISIBLE);
            } else if (this.isContracted()) {
                endpoint.setVisibility(View.VISIBLE);
                setButton.setVisibility(View.GONE);
                ioName.setVisibility(View.GONE);
                ioType.setVisibility(View.GONE);
                ioValue.setVisibility(View.GONE);
            } else { // It's normal
                endpoint.setVisibility(View.VISIBLE);
                setButton.setVisibility(View.GONE);
                ioName.setVisibility(View.VISIBLE);
                ioType.setVisibility(View.VISIBLE);
                ioValue.setVisibility(View.VISIBLE);
            }

            return v;
        }
    }

    private class RedrawRunnable implements Runnable {
        private int outputIndex;
        private int inputIndex;

        private RedrawRunnable(int outputIndex, int inputIndex) {
            this.inputIndex = inputIndex;
            this.outputIndex = outputIndex;
        }

        @Override
        public void run() {
            View outElement = outputList.getChildAt(outputIndex);
            View inElement = inputList.getChildAt(inputIndex);

            ServiceIO out = first.getOutputs().get(outputIndex);
            ServiceIO in = second.getInputs().get(inputIndex);

            activity.setStatus("Connected " + out.getDescription().getFriendlyName() + " to " + in.getDescription().getFriendlyName());

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

            registry.updateComposite(registry.getCurrent());
            redraw(true);
        }

    }

    private class OutputAdapter extends WiringIOAdapter {

        private TextView ioName;
        private TextView ioType;
        private TextView ioValue;

        public OutputAdapter(Context parent, ArrayList<ServiceIO> items) {
            super(parent, R.layout.list_item_wiring_out, items);
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_wiring_out, null);
            }

            final View v = convertView;

            final ServiceIO item = items.get(position);
            final IODescription iod = item.getDescription();

            final LinearLayout endpoint = (LinearLayout) v.findViewById(R.id.endpoint);
            final Drawable blob = v.findViewById(R.id.blob).getBackground();
            final Drawable stub = v.findViewById(R.id.stub).getBackground();

            int col = Color.HSVToColor(FULL_ALPHA / BASE_ALPHA,
                    new float[]{
                            hueMap.get(iod.getType().getClassName()),
                            1,
                            1
                    }
            );

            blob.setColorFilter(col, PorterDuff.Mode.ADD);
            stub.setColorFilter(col, PorterDuff.Mode.ADD);

            ioName = (TextView) v.findViewById(R.id.io_name);
            ioName.setText(iod.getFriendlyName());

            ioType = (TextView) v.findViewById(R.id.io_type);
            ioValue = (TextView) v.findViewById(R.id.io_value);

            if (item != null) {
                if (!item.hasValue()) {
                    ioType.setText(iod.getType().getName());
                    ioValue.setText("");
                } else {
                    ioType.setText(iod.getType().getName() + ": ");
                    String valueName = item.getType().toString(item.getValue().getManualValue());
                    ioValue.setText(valueName);
                }
            }

            // Change the filter button image if a filter is selected
            endpoint.setVisibility(View.VISIBLE);

            endpoint.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View b) {
                    if (LOG) Log.d(TAG, "Output " + position + " (" + oIndex + ", " + iIndex + ")");

                    if (second == null || !second.getDescription().hasInputs()) {
                        return;
                    }
                    if (iSelected == null && oSelected == null) {
                        if (LOG) Log.d(TAG, "Output " + position + " (Both null)");
                        // If they click an output first then just work normally and connect it for them
                        setHighlight(item.getDescription().getType(), parent);
                        b.setBackgroundColor(
                                Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(item.getDescription().getType().getClassName()),
                                                1,
                                                1
                                        }
                                ));

                        oSelected = item;
                        oIndex = position;
                        activity.setStatus("Selected " + item.getDescription().getName());
                    } else if (iSelected != null && iSelected.getDescription().getType().equals(item.getDescription().getType()) && oSelected == null) {
                        if (LOG) Log.d(TAG, "Output " + position + " We have a match");

                        // If the current input and output are in the connections list then do nothing. That would be stupid
                        if (checkConnection(oIndex, position))
                            return;

                        setHighlight(null, parent);

                        oSelected = item;
                        oIndex = position;
                        activity.setStatus("Selected " + item.getDescription().getName());

                        // This one
                        b.setBackgroundColor(
                                Color.HSVToColor(
                                        FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(item.getDescription().getType().getClassName()),
                                                1,
                                                1
                                        }
                                )
                        );

                        // The corresponding output needs to be put back
                        inputList.getChildAt(iIndex).findViewById(R.id.endpoint).setBackgroundColor(Color.HSVToColor(
                                        FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(oSelected.getDescription().getType().getClassName()),
                                                1,
                                                1
                                        }
                                )
                        );

                        redraw(true);

                        // Update the drawing a bit later...
                        Handler h = new Handler();
                        h.postDelayed(new RedrawRunnable(oIndex, iIndex), 200);
                    } else if (oSelected != null && iSelected == null) {
                        if (LOG)
                            Log.d(TAG, "Output " + position + " (input is null, output is not)");
                        // This means that we need to deselect the current one?
                        setHighlight(null, parent);

                        if (oIndex == position) {
                            // We don't have a new one
                            oSelected = null;
                            oIndex = -1;
                        } else {
                            setHighlight(item.getDescription().getType(), parent);
                            b.setBackgroundColor(
                                    Color.HSVToColor(FULL_ALPHA / HIGHLIGHT_ALPHA,
                                            new float[]{
                                                    hueMap.get(item.getDescription().getType().getClassName()),
                                                    1,
                                                    1
                                            }
                                    ));

                            oSelected = item;
                            oIndex = position;
                            activity.setStatus("Selected " + item.getDescription().getName());
                        }

                        redraw(true);
                    } else {
                        if (LOG) Log.d(TAG, "Output " + position + " (else....)");
                        setHighlight(item.getDescription().getType(), parent);

                        b.setBackgroundColor(
                                Color.HSVToColor(
                                        FULL_ALPHA / HIGHLIGHT_ALPHA,
                                        new float[]{
                                                hueMap.get(item.getDescription().getType().getClassName()),
                                                1,
                                                1
                                        }
                                )
                        );

                        oSelected = item;
                        oIndex = position;
                        activity.setStatus("Selected " + item.getDescription().getName());
                        iSelected = null;
                        iIndex = -1;
                    }
                }
            });

            if (this.isContracted()) {
                ioName.setVisibility(View.GONE);
                ioType.setVisibility(View.GONE);
                ioValue.setVisibility(View.GONE);
            } else if (this.isExpanded()) {
                // XXX Might need this
            } else {
                ioName.setVisibility(View.VISIBLE);
                ioType.setVisibility(View.VISIBLE);
                ioValue.setVisibility(View.VISIBLE);
            }

            return v;
        }
    }

    private abstract class WiringIOAdapter extends ArrayAdapter<ServiceIO> {

        protected int mode;
        protected final int MODE_NORMAL = 0;
        protected final int MODE_EXPANDED = 1;
        protected final int MODE_CONTRACTED = -1;

        protected ArrayList<ServiceIO> items;

        public WiringIOAdapter(Context context, int resource, ArrayList<ServiceIO> objects) {
            super(context, resource, objects);
            this.items = objects;
            this.mode = MODE_NORMAL;
        }

        protected boolean isContracted() {
            return this.mode == MODE_CONTRACTED;
        }

        protected boolean isExpanded() {
            return this.mode == MODE_EXPANDED;
        }

        protected void expand() {
            this.mode = MODE_EXPANDED;
            notifyDataSetChanged();
        }

        protected void contract() {
            this.mode = MODE_CONTRACTED;
            notifyDataSetChanged();
        }

        protected void reset() {
            this.mode = MODE_NORMAL;
            notifyDataSetChanged();
        }

        public abstract View getView(final int position, View convertView, final ViewGroup parent);
    }

    private class FilterAdapter extends ArrayAdapter<IOFilter> {

        private ArrayList<IOFilter> items;

        public FilterAdapter(Context context, ArrayList<IOFilter> objects) {
            super(context, R.layout.list_item_filter, objects);
            this.items = objects;
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_item_filter, null);
            }

            final IOFilter item = items.get(position);

            TextView andor = (TextView) convertView.findViewById(R.id.andor);
            if (items.size() > 1 && items.size() != position + 1) {
                andor.setVisibility(View.VISIBLE);
                andor.setText(first.getFilterCondition() ? "AND" : "OR");
            } else {
                andor.setVisibility(View.GONE);
            }


            final TableLayout table = (TableLayout) convertView.findViewById(R.id.table);
            table.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View vv) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle("Your Title");

                    String enableText = item.isEnabled() ? "Disable" : "Enable";

                    alertDialogBuilder.setItems(new CharSequence[]{"Edit", enableText, "Delete"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    // Edit the filter
                                    activity.filter(first.getID(), item.getID(), position);
                                    break;

                                case 1:
                                    // Disable or enable the filter
                                    if (item.isEnabled())
                                        table.setEnabled(false);
                                    else
                                        table.setEnabled(true);
                                    break;

                                case 2:
                                    // Delete the filter
                                    items.remove(item);
                                    FilterAdapter.this.notifyDataSetChanged();
                                    if (first.getFilters().size() > 1) {
                                        filterAndor.setVisibility(View.VISIBLE);
                                    } else {
                                        filterAndor.setVisibility(View.GONE);
                                    }
                                    registry.updateComposite(registry.getCurrent());
                                    break;
                            }
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

            table.removeAllViews();
            ArrayList<ServiceIO> ios = item.getIOs();
            for (ServiceIO io : ios) {

                TableRow row = new TableRow(getContext());
                table.addView(row);

                TextView ioText = new TextView(getContext());
                ioText.setText(io.getDescription().getFriendlyName());
                ioText.setTypeface(null, Typeface.BOLD);
                TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.span = 2;
                ioText.setLayoutParams(params);
                row.addView(ioText);

                ArrayList<IOValue> values = item.getValues(io);

                for (IOValue value : values) {

                    TableRow subRow = new TableRow(getContext());
                    table.addView(subRow);

                    TextView conditionText = new TextView(getContext());
                    conditionText.setText(value.getCondition().shortText);
                    subRow.addView(conditionText);

                    TextView valueText = new TextView(getContext());

                    if (value.getFilterState() == IOValue.MANUAL) {
                        valueText.setText(io.getType().toString("\"" + value.getManualValue()) + "\"");
                    } else if (value.getFilterState() == IOValue.SAMPLE) {
                        valueText.setText(io.getType().toString("\"" + value.getSampleValue().getValue() + "\""));
                    }

                    subRow.addView(valueText);
                }
            }

            return convertView;
        }

    }

    private class PathColour {
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