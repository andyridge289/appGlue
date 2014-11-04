package com.appglue;

import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.appglue.engine.description.IOValue;
import com.appglue.library.AppGlueLibrary;

import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.FULL_ALPHA;
import static com.appglue.library.AppGlueConstants.BASE_ALPHA;
import static com.appglue.Constants.INDEX;

public class FragmentTutorial extends Fragment {
    private int index = -1;

    public static FragmentTutorial create(int index) {
        FragmentTutorial ft = new FragmentTutorial();
        Bundle b = new Bundle();
        b.putInt(INDEX, index);
        ft.setArguments(b);
        return ft;
    }

    public FragmentTutorial() {

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (getArguments() != null) {
            this.index = getArguments().getInt(INDEX);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        ScrollView v = (ScrollView) inflater.inflate(R.layout.fragment_tutorial, container, false);

        switch(index) {
            case 0:
                v.addView(createComposite(inflater));
                break;

            case 1:
                v.addView(createComponent(inflater));
                break;

            case 2:
                v.addView(createTrigger(inflater));
                break;

            case 3:
                v.addView(createWiring(inflater));
                break;

            case 4:
                v.addView(createFilter(inflater));
                break;

            case 5:
                v.addView(createSchedule(inflater));
                break;
        }

        return v;
    }

    private View createSchedule(final LayoutInflater inflater) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv0 = createTitleView(R.string.tutorial_6_0, R.color.schedule);
        layout.addView(tv0);

        TextView tv1 = createTextView(R.string.tutorial_6_1);
        layout.addView(tv1);

        TextView tv10 = createTitleView(R.string.tutorial_6_10, R.color.log);
        layout.addView(tv10);

        TextView tv11 = createTextView(R.string.tutorial_6_11);
        layout.addView(tv11);

        return layout;
    }

    private View createFilter(final LayoutInflater inflater) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv0 = createTitleView(R.string.tutorial_5_0, R.color.filter);
        layout.addView(tv0);

        View v0 = inflater.inflate(R.layout.wiring_tab_bar, null);
        LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m0 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp0.setMargins(0, 0, 0, m0);
        v0.setLayoutParams(lp0);
        v0.findViewById(R.id.filter_button_all).setSelected(true);
        layout.addView(v0);

        TextView tv1 = createTextView(R.string.tutorial_5_1);
        layout.addView(tv1);

        View v1 = inflater.inflate(R.layout.list_item_filter, null);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m1 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp1.setMargins(0, 0, 0, m1);
        v1.setLayoutParams(lp1);
        layout.addView(v1);
        TableLayout table = (TableLayout) v1.findViewById(R.id.table);
        TableRow row = new TableRow(getActivity());
        table.addView(row);
        TextView ioText = new TextView(getActivity());
        ioText.setText("Output");
        ioText.setTypeface(null, Typeface.BOLD);
        TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.span = 2;
        ioText.setLayoutParams(params);
        row.addView(ioText);
        for (int i = 0; i < 2; i++) {
            TableRow subRow = new TableRow(getActivity());
            table.addView(subRow);
            TextView conditionText = new TextView(getActivity());
            conditionText.setText("=");
            subRow.addView(conditionText);
            TextView valueText = new TextView(getActivity());
            if (i == 0) {
                valueText.setText("\"Something\"");
            } else {
                valueText.setText("\"Something else\"");
            }
            subRow.addView(valueText);
        }

        TextView tv2 = createTextView(R.string.tutorial_5_2);
        layout.addView(tv2);

        View v2 = inflater.inflate(R.layout.fragment_filter_item, null);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m2 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp2.setMargins(0, 0, 0, m2);
        v2.setLayoutParams(lp2);
        layout.addView(v2);
        v2.findViewById(R.id.no_filters).setVisibility(View.GONE);
        LinearLayout container = (LinearLayout) v2.findViewById(R.id.filter_value_container);

        ((TextView) v2.findViewById(R.id.filter_io_name)).setText("Output");
        ((TextView) v2.findViewById(R.id.filter_io_type)).setText("Text");

        View v21 = inflater.inflate(R.layout.fragment_filter_value, null);
        container.addView(v21);

        RadioButton sampleRadio = (RadioButton) v21.findViewById(R.id.filter_radio_sample);
        sampleRadio.setChecked(false);
        sampleRadio.setEnabled(false);
        RadioButton manualRadio = (RadioButton) v21.findViewById(R.id.filter_radio_manual);
        manualRadio.setChecked(true);
        manualRadio.setEnabled(false);

        ((EditText) v21.findViewById(R.id.filter_value_text)).setText("Something");
        ((Switch) v21.findViewById(R.id.filter_value_switch)).setChecked(true);
        ((Spinner) v21.findViewById(R.id.filter_condition_spinner)).setAdapter(new SpinnerAdapter() {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
                v.setText((String) getItem(position));
                return v;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Object getItem(int position) {
                return position == 0 ? "Equals" : "Doesn't equal";
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
                v.setText((String) getItem(position));
                return v;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        });
        // TODO The spinner needs to contain equals

        TextView tv3 = createTextView(R.string.tutorial_5_3);
        layout.addView(tv3);

        TextView tv4 = createTextView(R.string.tutorial_5_4);
        layout.addView(tv4);

        TextView tv5 = createTextView(R.string.tutorial_5_5);
        layout.addView(tv5);

        return layout;
    }

    private View createWiring(LayoutInflater inflater) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv0 = createTitleView(R.string.tutorial_4_0, R.color.composite);
        layout.addView(tv0);

        TextView tv1 = createTextView(R.string.tutorial_4_01);
        layout.addView(tv1);

        TextView tv2 = createTextView(R.string.tutorial_4_02);
        layout.addView(tv2);

        TextView tv3 = createTitleView(R.string.tutorial_4_1, R.color.black);
        layout.addView(tv3);

        View v0 = inflater.inflate(R.layout.wiring_tab_bar, null);
        LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m0 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp0.setMargins(0, 0, 0, m0);
        v0.setLayoutParams(lp0);
        v0.findViewById(R.id.wiring_button_all).setSelected(true);
        layout.addView(v0);

        TextView tv4 = createTextView(R.string.tutorial_4_2);
        layout.addView(tv4);

        LinearLayout v1 = createInputOutput(inflater, false);
        layout.addView(v1);

        TextView tv5 = createTextView(R.string.tutorial_4_3);
        layout.addView(tv5);

        LinearLayout v2 = createInputOutput(inflater, true);
        layout.addView(v2);

        TextView tv6 = createTextView(R.string.tutorial_4_4);
        layout.addView(tv6);

        TextView tv7 = createTextView(R.string.tutorial_4_5);
        layout.addView(tv7);

        TextView tv8 = createTextView(R.string.tutorial_4_6);
        layout.addView(tv8);

        TextView tv10 = createTitleView(R.string.tutorial_4_8, R.color.black);
        layout.addView(tv10);

        View v5 = inflater.inflate(R.layout.wiring_tab_bar, null);
        LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m5 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp5.setMargins(0, 0, 0, m5);
        v5.setLayoutParams(lp5);
        v5.findViewById(R.id.value_button_all).setSelected(true);
        layout.addView(v5);

        TextView tv11 = createTextView(R.string.tutorial_4_9);
        layout.addView(tv11);

        int fiveDip = (int) AppGlueLibrary.dpToPx(getResources(), 5);
        int oneDip = (int) AppGlueLibrary.dpToPx(getResources(), 1);
        LinearLayout v6 = new LinearLayout(getActivity());
        v6.setBackgroundResource(R.color.hexAAA);
        v6.setPadding(fiveDip, oneDip, 0, oneDip);
        v6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final View v32 = inflater.inflate(R.layout.list_item_wiring_in, null);
        v32.setBackgroundResource(R.color.white);
        v32.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v6.addView(v32);
        ImageView setButton = ((ImageView) v32.findViewById(R.id.set_button));
        setButton.setImageResource(R.drawable.ic_add_black_48dp);
        setButton.setVisibility(View.VISIBLE);
        v32.findViewById(R.id.endpoint).setVisibility(View.GONE);
        ((TextView) v32.findViewById(R.id.io_name)).setText("Input");
        ((TextView) v32.findViewById(R.id.io_type)).setText("Text");
        v32.findViewById(R.id.mandatory_bar).setVisibility(View.GONE);
        layout.addView(v6);

        return layout;
    }



    private LinearLayout createInputOutput(LayoutInflater inflater, final boolean wiring) {

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.weight = 2;

        int fiveDip = (int) AppGlueLibrary.dpToPx(getResources(), 5);
        int oneDip = (int) AppGlueLibrary.dpToPx(getResources(), 1);

        LinearLayout v2 = new LinearLayout(getActivity());
        v2.setBackgroundResource(R.color.hexAAA);
        v2.setPadding(0, oneDip, fiveDip, oneDip);
        v2.setLayoutParams(lp1);


        final View v22 = inflater.inflate(R.layout.list_item_wiring_out, null);
        v22.setBackgroundResource(R.color.white);
        v22.setLayoutParams(lp1);
        v2.addView(v22);
        Drawable blob = v22.findViewById(R.id.blob).getBackground();
        Drawable stub = v22.findViewById(R.id.stub).getBackground();

        int col = Color.HSVToColor(FULL_ALPHA / BASE_ALPHA,
                new float[]{
                        0,
                        1,
                        1
                }
        );

        blob.setColorFilter(col, PorterDuff.Mode.ADD);
        stub.setColorFilter(col, PorterDuff.Mode.ADD);
        ((TextView) v22.findViewById(R.id.io_name)).setText("Output");
        ((TextView) v22.findViewById(R.id.io_type)).setText("Text");

        View v4 = new View(getActivity());
        LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp4.weight = 1;
        v4.setLayoutParams(lp4);


        LinearLayout v3 = new LinearLayout(getActivity());
        v3.setBackgroundResource(R.color.hexAAA);
        v3.setPadding(fiveDip, oneDip, 0, oneDip);
        v3.setLayoutParams(lp1);

        final View v32 = inflater.inflate(R.layout.list_item_wiring_in, null);
        v32.setBackgroundResource(R.color.white);
        v32.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v3.addView(v32);
        final Drawable blob2 = v32.findViewById(R.id.blob).getBackground();
        final Drawable stub2 = v32.findViewById(R.id.stub).getBackground();
        blob2.setColorFilter(col, PorterDuff.Mode.ADD);
        stub2.setColorFilter(col, PorterDuff.Mode.ADD);
        ((TextView) v32.findViewById(R.id.io_name)).setText("Input");
        ((TextView) v32.findViewById(R.id.io_type)).setText("Text");
        v32.findViewById(R.id.mandatory_bar).setVisibility(View.GONE);

        LinearLayout v1 = new LinearLayout(getActivity()) {
            @Override
            protected void dispatchDraw(Canvas canvas) {

                if (!wiring) {
                    super.dispatchDraw(canvas);
                    return;
                }

                Paint paint = new Paint();
                paint.setDither(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);

                float scale = getActivity().getResources().getDisplayMetrics().density;
                paint.setStrokeWidth(4 * scale);

                int outputOffset = 0;
                int inputOffset = 0;

                int col = Color.HSVToColor(FULL_ALPHA, new float[]{ 0, 1, 1});

                int px = 0;//(int) ((24 + 5) * scale + 0.5); // Half the square plus the border

                int[] layout = new int[2];
                this.getLocationOnScreen(layout);

                int[] outputTab = new int[2];
                v22.getLocationOnScreen(outputTab);

                // Move this left a bit to be in the middle of the input
                float startX = outputTab[0] - layout[0] + v22.getWidth() - px;
                float startY = outputTab[1] - layout[1] + (v22.getHeight() / 2);
                PointF start = new PointF(startX, startY);

                int[] inputTab = new int[2];
                v32.getLocationOnScreen(inputTab);

                float endX = inputTab[0] - layout[0] + px;
                float endY = inputTab[1] - layout[1] + (v32.getHeight() / 2);
                PointF end = new PointF(endX, endY);

                paint.setColor(col);

                Path p = new Path();
                p.moveTo(start.x, start.y + outputOffset);

                p.lineTo(end.x, end.y + inputOffset);

                canvas.drawPath(p, paint);

                super.dispatchDraw(canvas);
            }
        };

        v1.addView(v2);
        v1.addView(v4);
        v1.addView(v3);

        return v1;
    }

    private View createTrigger(LayoutInflater inflater) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv0 = createTitleView(R.string.tutorial_3_0, R.color.component);
        layout.addView(tv0);

        TextView tv1 = createTextView(R.string.tutorial_3_1);
        layout.addView(tv1);

        TextView tv2 = createTextView(R.string.tutorial_3_2);
        layout.addView(tv2);

        TextView tv3 = createTextView(R.string.tutorial_3_3);
        layout.addView(tv3);

        View v0 = inflater.inflate(R.layout.list_item_component, null);
        LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m0 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp0.setMargins(0, 0, 0, m0);
        v0.setLayoutParams(lp0);
        layout.addView(v0);

        ((TextView) v0.findViewById(R.id.service_name)).setText("Trigger");
        v0.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
        v0.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);

        LinearLayout flagContainer = (LinearLayout) v0.findViewById(R.id.flag_container);

        View vv0 = inflater.inflate(R.layout.component_attribute, null);
        flagContainer.addView(vv0);
        vv0.findViewById(R.id.component_attribute_icon).setBackgroundResource(R.drawable.ic_exit_to_app_white_18dp);
        ((TextView) vv0.findViewById(R.id.component_attribute_text)).setText("Trigger");

        TextView tv4 = createTextView(R.string.tutorial_3_4);
        layout.addView(tv4);

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m1 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 5);
        lp1.setMargins(0, 0, 0, m1);

        View vv1 = inflater.inflate(R.layout.component_attribute, null);
        vv1.setLayoutParams(lp1);
        layout.addView(vv1);
        vv1.findViewById(R.id.component_attribute_icon).setBackgroundResource(R.drawable.ic_money);
        ((TextView) vv1.findViewById(R.id.component_attribute_text)).setText("Costs money");

        View vv2 = inflater.inflate(R.layout.component_attribute, null);
        vv2.setLayoutParams(lp1);
        layout.addView(vv2);
        vv2.findViewById(R.id.component_attribute_icon).setBackgroundResource(R.drawable.ic_my_location_white_18dp);
        ((TextView) vv2.findViewById(R.id.component_attribute_text)).setText("Uses GPS");

        return layout;
    }

    private View createComponent(LayoutInflater inflater) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView tv0 = createTitleView(R.string.tutorial_2_0, R.color.component);
        layout.addView(tv0);

        TextView tv1 = createTextView(R.string.tutorial_2_1);
        layout.addView(tv1);

        TextView tv2 = createTextView(R.string.tutorial_2_2);
        layout.addView(tv2);

        View v0 = inflater.inflate(R.layout.list_item_component, null);
        LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m0 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp0.setMargins(0, 0, 0, m0);
        v0.setLayoutParams(lp0);
        layout.addView(v0);

        ((TextView) v0.findViewById(R.id.service_name)).setText("Component 1");
        v0.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
        v0.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);

        TextView tv3 = createTextView(R.string.tutorial_2_3);
        layout.addView(tv3);

        View v1 = inflater.inflate(R.layout.list_item_component, null);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m1 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp1.setMargins(0, 0, 0, m1);
        v1.setLayoutParams(lp1);
        layout.addView(v1);

        ((TextView) v1.findViewById(R.id.service_name)).setText("Component with inputs");
        v1.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.has_io);
        v1.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);

        TextView tv4 = createTextView(R.string.tutorial_2_4);
        layout.addView(tv4);

        View v2 = inflater.inflate(R.layout.list_item_component, null);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m2 = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp2.setMargins(0, 0, 0, m2);
        v2.setLayoutParams(lp2);
        layout.addView(v2);

        ((TextView) v2.findViewById(R.id.service_name)).setText("Component with outputs");
        v2.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
        v2.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.has_io);

        TextView tv5 = createTextView(R.string.tutorial_2_5);
        layout.addView(tv5);

        TextView tv6 = createTextView(R.string.tutorial_2_6);
        layout.addView(tv6);

        return layout;
    }

    private View createComposite(LayoutInflater inflater) {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView tv0 = createTitleView(R.string.tutorial_1_0, R.color.composite);
        layout.addView(tv0);

        TextView tv1 = createTextView(R.string.tutorial_1_1);
        layout.addView(tv1);

        TextView tv2 = createTextView(R.string.tutorial_1_2);
        layout.addView(tv2);

        View v = inflater.inflate(R.layout.list_item_composite, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = (int) AppGlueLibrary.dpToPx(getActivity().getResources(), 10);
        lp.setMargins(0, 0, 0, m);
        v.setLayoutParams(lp);
        layout.addView(v);

        v.findViewById(R.id.composite_item_bg).setBackgroundResource(R.color.material_amber200);
        for (int i = 1; i < 4; i++) {
            TextView ctv = new TextView(getActivity());
            ctv.setText("Component " + i);
            ((LinearLayout) v.findViewById(R.id.composite_components)).addView(ctv);
        }

        TextView tv3 = createTextView(R.string.tutorial_1_3);
        layout.addView(tv3);

        return layout;
    }

    private TextView createTitleView(int text, int color) {

        Resources res = getActivity().getResources();

        TextView tv = new TextView(getActivity());
        tv.setTextColor(res.getColor(color));
        tv.setText(res.getString(text));
        tv.setTextSize(22);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = (int) AppGlueLibrary.dpToPx(res, 5);
        lp.setMargins(0, 0, 0, m);
        tv.setLayoutParams(lp);

        return tv;
    }

    private TextView createTextView(int stringRes) {

        Resources res = getActivity().getResources();

        TextView tv = new TextView(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int m = (int) AppGlueLibrary.dpToPx(res, 5);
        lp.setMargins(0, 0, 0, m);
        tv.setLayoutParams(lp);

        String first = res.getString(stringRes);
        SpannableString text = new SpannableString(first);
        applyStyles(first, text);

        tv.setText(text, TextView.BufferType.SPANNABLE);
        return tv;
    }

    private void applyStyles(String s, SpannableString ss) {

        Resources res = getActivity().getResources();

        ArrayList<Point> composites = find(s, res.getStringArray(R.array.composite_terms));
        ArrayList<Point> components = find(s, res.getStringArray(R.array.component_terms));
        ArrayList<Point> importants = find(s, res.getStringArray(R.array.important_terms));
        ArrayList<Point> filters = find(s, res.getStringArray(R.array.filter_terms));
        ArrayList<Point> schedules = find(s, res.getStringArray(R.array.schedule_terms));
        ArrayList<Point> logs = find(s, res.getStringArray(R.array.log_terms));

        for (Point p : composites) {
            ss.setSpan(new TextAppearanceSpan(getActivity(), R.style.composite_text), p.x, p.x + p.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point p : components) {
            ss.setSpan(new TextAppearanceSpan(getActivity(), R.style.component_text), p.x, p.x + p.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point p : importants) {
            ss.setSpan(new TextAppearanceSpan(getActivity(), R.style.important_text), p.x, p.x + p.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point p : filters) {
            ss.setSpan(new TextAppearanceSpan(getActivity(), R.style.filter_text), p.x, p.x + p.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point p : schedules) {
            ss.setSpan(new TextAppearanceSpan(getActivity(), R.style.schedule_text), p.x, p.x + p.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Point p : logs) {
            ss.setSpan(new TextAppearanceSpan(getActivity(), R.style.log_text), p.x, p.x + p.y, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private ArrayList<Point> find(String search, String[] ss) {

        ArrayList<Point> p = new ArrayList<Point>();

        for (String s : ss) {
            if (search.toLowerCase().contains(s.toLowerCase())) {
                p.add(new Point(search.toLowerCase().indexOf(s.toLowerCase()), s.length()));
            }
        }

        return p;

    }
}