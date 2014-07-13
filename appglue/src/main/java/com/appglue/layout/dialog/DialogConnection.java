package com.appglue.layout.dialog;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.ActivityWiring;
import com.appglue.R;
import com.appglue.ServiceIO;
import com.appglue.layout.WiringMap;

import java.util.ArrayList;

public class DialogConnection extends DialogCustom {
    private WiringMap parent;

    public DialogConnection(final ActivityWiring context, final WiringMap parent, final ListView outputList, final ListView inputList, final ServiceIO item, final int position) {
        super(context, item);
        this.parent = parent;

        // Show something to delete the links to this one

        final ArrayList<Point> ps = parent.getConnectionsOut(position);

        ListView lv = new ListView(context);
        lv.setAdapter(new ArrayAdapter<Point>(context, R.layout.list_item_connection, ps) {
            public View getView(int position, View convertView, ViewGroup vg) {
                if (convertView == null) {
                    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = vi.inflate(R.layout.list_item_connection, vg);
                }

                final View v = convertView;

                final Point p = ps.get(position);

                TextView connectionIn = (TextView) v.findViewById(R.id.connection_in);
                TextView connectionOut = (TextView) v.findViewById(R.id.connection_out);
                ImageButton removeButton = (ImageButton) v.findViewById(R.id.connection_remove);

                final ServiceIO out = parent.getFirst().getOutputs().get(p.x);
                final ServiceIO in = parent.getSecond().getInputs().get(p.y);

                connectionOut.setText(out.getFriendlyName());
                connectionIn.setText(in.getFriendlyName());

                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parent.removeConnection(p);
                        ps.remove(p);
                        context.setStatus("Removed connection between " + out.getName() + " and " + in.getName());

                        // If there aren't any other connections for this output
                        if (parent.getConnectionsOut(p.x).size() == 0) {
                            View outputView = outputList.getChildAt(p.x);
                            outputView.findViewById(R.id.blob).setBackgroundResource(R.drawable.io_blob);
                            outputView.findViewById(R.id.stub).setBackgroundResource(R.drawable.io_stub);
                        }

                        // There can't be any other connections for this output, so just change the image
                        View inputView = inputList.getChildAt(p.x);
                        inputView.findViewById(R.id.blob).setBackgroundResource(R.drawable.io_blob);
                        inputView.findViewById(R.id.stub).setBackgroundResource(R.drawable.io_stub);

                        if (ps.size() == 0) {
                            // If there are no more rows, dismiss the dialog
                            dismiss();
                        }

                        // Delete the row of the thing
                        notifyDataSetChanged();
                        registry.updateCurrent();

                        // Redraw so that the connection disappears
                        parent.redraw();
                    }

                });

                return v;
            }
        });

        setView(lv);
        setTitle("Edit connections for " + item.getFriendlyName());

        show();
    }


}
