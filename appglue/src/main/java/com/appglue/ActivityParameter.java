package com.appglue;

//import static com.appglue.Constants.COMPOSITE_ID;
//import static com.appglue.Constants.DURATION;
//import static com.appglue.Constants.RUN_NOW;
//import static com.appglue.Constants.TEST;
//
//import java.util.ArrayList;
//
//import android.app.ActionBar;
import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
import android.os.Bundle;
//import android.util.SparseBooleanArray;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.BaseExpandableListAdapter;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ExpandableListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.appglue.Library;
//import com.appglue.ServiceParameter;
//import com.appglue.Constants.Param;
//import com.appglue.Constants.ProcessType;
//import com.appglue.Constants.Requiredness;
//import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
//import com.appglue.serviceregistry.Registry;

public class ActivityParameter extends Activity
{
	private boolean test;
	private int duration;
	private boolean runNow;
	
	private CompositeService composite;
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		setContentView(R.layout.parameter_list);
		
//		Intent intent = getIntent();
//		test = intent.getBooleanExtra(TEST, false);
//		duration = intent.getIntExtra(DURATION, 0);
//		runNow = intent.getBooleanExtra(RUN_NOW, false);
//
//		Registry registry = Registry.getInstance(this);
//
//		if(!test)
//		{
//			long compositeId = intent.getLongExtra(COMPOSITE_ID, -1);
//			composite = registry.getComposite(compositeId);
//		}
//		else
//		{
//			composite = registry.getTemp();
//		}
//
//		TextView nameText = (TextView) findViewById(R.id.param_name);
//
//		nameText.setText(composite.getName());
//
//		ActionBar actionBar = getActionBar();
//		actionBar.setTitle(getResources().getString(R.string.title_param) + ": " + composite.getName());
//		actionBar.setIcon(R.drawable.ic_menu_manage_small);
//
//		ExpandableListView paramServiceList = (ExpandableListView) findViewById(R.id.param_service_list);
//
//		ArrayList<ServiceDescription> items = new ArrayList<ServiceDescription>();
//
//		 for(int i = 0 ; i < composite.getComponents().size(); i++)
//		 {
//			 if(composite.getComponents().get(i).getProcessType() == ProcessType.NORMAL)
//				 items.add(composite.getComponents().get(i));
//		 }
//
//
//		paramServiceList.setAdapter(new ParamAdapter(items));
//
//		for(int i = 0; i < items.size(); i++)
//		{
//			paramServiceList.expandGroup(i, true);
//		}
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.parameter_menu, menu);
//
//		return true;
//	}
//
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		if(item.getItemId() == R.id.doneButton)
//		{
//			ArrayList<ServiceParameter> params = composite.getParameters();
//
//			for(int i = 0; i < params.size(); i++)
//			{
//				ServiceParameter p = params.get(i);
//
//				if(!p.isSet() && p.getRequiredness() == Requiredness.MANDATORY)
//				{
//					// Then tell the user they haven't set something that they need to set
//					AlertDialog.Builder builder = new AlertDialog.Builder(this);
//					builder.setTitle("You missed a mandatory parameter!");
//					builder.setMessage("Parameter \"" + p.getName() + "\" is mandatory and you haven't set it");
//					builder.setNeutralButton("Okay", new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//							dialog.cancel();
//						}
//					});
//					builder.show();
//					return false;
//				}
//				else if(!p.isSet())
//				{
//					params.remove(p);
//					i--;
//				}
//			}
//
//			if(!test)
//			{
//				Registry registry = Registry.getInstance(this);
//				if(registry.saveParametersForComposite(composite.getId(), params))
//				{
//					composite.setParamsSet(true);
//					Toast.makeText(this, String.format("Parameters saved for %s", composite.getName()), Toast.LENGTH_LONG).show();
//				}
//				else
//				{
//					composite.setParamsSet(false);
//					Toast.makeText(this, String.format("Parameters save failed for %s", composite.getName()), Toast.LENGTH_LONG).show();
//				}
//			}
//
//			Intent intent = new Intent();
//			intent.putExtra("result", true);
//			intent.putExtra(COMPOSITE_ID, composite.getId());
//			intent.putExtra(DURATION, duration);
//			intent.putExtra(RUN_NOW, runNow);
//
//			if (getParent() == null) {
//			    setResult(Activity.RESULT_OK, intent);
//			} else {
//			    getParent().setResult(Activity.RESULT_OK, intent);
//			}
//
//			finish();
//		}
//
//		return false;
//	}
//
//	 public class ParamAdapter extends BaseExpandableListAdapter
//	 {
//		 private ArrayList<ServiceDescription> items;
//
//		 public ParamAdapter(ArrayList<ServiceDescription> allItems)
//		 {
//			 this.items = allItems;
//		 }
//
//		 public Object getGroup(int groupPosition)
//		 {
//			 return items.get(groupPosition);
//		 }
//
//		 public int getGroupCount()
//		 {
//			 return items.size();
//		 }
//
//		 public long getGroupId(int groupPosition)
//		 {
//			 return groupPosition;
//		 }
//
//		 public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
//		 {
//			 View v = convertView;
//
//			 if(v == null)
//			 {
//				 LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				 v = vi.inflate(R.layout.parameter_list_item, null);
//			 }
//
//			 ServiceDescription group = (ServiceDescription) this.getGroup(groupPosition);
//
//			 TextView serviceName = (TextView) v.findViewById(R.id.param_name);
//			 serviceName.setText(group.getName());
//
//			 return v;
//		 }
//
//
//		 public Object getChild(int groupPosition, int childPosition)
//		 {
//			 ServiceDescription sd = (ServiceDescription) this.getGroup(groupPosition);
//			 ArrayList<ServiceParameter> children = sd.getParameters();
//
//			 if(children.size() == 0)
//				 return getResources().getString(R.string.param_none);
//			 else
//				 return children.get(childPosition);
//		 }
//
//		 public long getChildId(int groupPosition, int childPosition)
//		 {
//			 return childPosition;
//		 }
//
//		 public int getChildrenCount(int groupPosition)
//		 {
//			 ServiceDescription sd = (ServiceDescription) this.getGroup(groupPosition);
//			 ArrayList<ServiceParameter> children = sd.getParameters();
//
//			 if(children.size() > 0)
//				 return children.size();
//			 else
//				 return 1;
//		 }
//
//		 public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
//		 {
//			 Object o = this.getChild(groupPosition, childPosition);
//			 ServiceDescription service = (ServiceDescription) this.getGroup(groupPosition);
//
//			 try
//			 {
//				 String s = (String) o;
//
//				 TextView paramText = new TextView(ActivityParameter.this);
//				 paramText.setText(s);
//				 return paramText;
//			 }
//			 catch(ClassCastException e)
//			 {
//				 View v = convertView;
//
//				 if(v == null)
//				 {
//					 LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//					 v = vi.inflate(R.layout.parameter_sub_list_item, null);
//				 }
//
//				 final ServiceParameter param = (ServiceParameter) o;
//
//				 Requiredness required = param.getRequiredness();
//				 TextView requiredness = (TextView) v.findViewById(R.id.sub_param_requiredness);
//				 requiredness.setText(required.name);
//
//
//				 TextView name = (TextView) v.findViewById(R.id.sub_param_name);
//				 name.setText(param.getName());
//
//				 TextView type = (TextView) v.findViewById(R.id.sub_param_type);
//				 type.setText(param.getType().name);
//
//				 TextView description = (TextView) v.findViewById(R.id.sub_param_description);
//				 description.setText(param.getDescription());
//
//				 TextView value = (TextView) v.findViewById(R.id.sub_param_value);
//
//				 if(param.isSet())
//				 {
//					 if(param.useDefault())
//					 {
//						 value.setText("Using default");
//					 }
//					 else
//					 {
//						 String[] values = param.getValue();
//						 value.setText(Library.implode(values, ", ", false));
//					 }
//				 }
//				 else
//				 {
//					 value.setText("Not set");
//					 value.setTextColor(getResources().getColor(R.color.dim_on_dark));
//				 }
//
//				 Button paramButton = (Button) v.findViewById(R.id.sub_param_set);
//				 final CheckBox useDefault = (CheckBox) v.findViewById(R.id.sub_param_default);
//
//				 int dimOnDark = getResources().getColor(R.color.dim_text);
//				 if(required == Requiredness.OPTIONAL)
//				 {
//					 requiredness.setTextColor(dimOnDark);
//					 name.setTextColor(dimOnDark);
//					 type.setTextColor(dimOnDark);
//					 description.setTextColor(dimOnDark);
//					 useDefault.setTextColor(dimOnDark);
//
//					 ((TextView) v.findViewById(R.id.sub_param_name_fixed)).setTextColor(dimOnDark);
//					 ((TextView) v.findViewById(R.id.sub_param_description_fixed)).setTextColor(dimOnDark);
//				 }
//				 else
//				 {
//					 useDefault.setVisibility(View.GONE);
//				 }
//
//
//				 final AlertDialog.Builder builder = setupParameterDialog(service, param, paramButton, useDefault);
//
//				 paramButton.setOnClickListener(new OnClickListener()
//				 {
//					 public void onClick(View v)
//					 {
//						 builder.create();
//						 builder.show();
//					 }
//				 });
//
//				 useDefault.setOnClickListener(new OnClickListener()
//				 {
//					 public void onClick(View v)
//					 {
//						 if(useDefault.isChecked())
//						 {
//							 param.setSet(true);
//							 param.setUseDefault(true);
//						 }
//						 else
//						 {
//							 param.setUseDefault(false);
//						 }
//					 }
//				 });
//
//				 return v;
//			 }
//		 }
//
//		 public boolean isChildSelectable(int groupPosition, int childPosition)
//		 {
//			 return false;
//		 }
//
//		 public boolean hasStableIds()
//		 {
//			 return true;
//		 }
//	 }
//
//	 private AlertDialog.Builder setupManySetDialog(final ServiceDescription service, final ServiceParameter param, Button button, final CheckBox useDefault)
//	 {
//		 final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityParameter.this);
//
//		 // Then add a button to do the selection
//		 button.setText("Pick Choices");
//
//		 builder.setTitle("Choose as many values as you want!");
//
//		 boolean[] states = new boolean[param.getPossibleUser().length];
//		 if(param.getValue() == null)
//		 {
//			 for(int j = 0; j < states.length; j++)
//			 {
//				 states[j] = false;
//			 }
//		 }
//		 else
//		 {
//			 String[] values = param.getValue();
//			 String[] systemValues = param.getPossibleSystem();
//
//			 for(int j = 0; j < systemValues.length; j++)
//			 {
//				 boolean found = false;
//				 for(int k = 0; k < values.length; k++)
//				 {
//					 if(systemValues[j].equals(values[k]))
//					 {
//						 found = true;
//						 continue;
//					 }
//				 }
//
//				 if(found)
//					 states[j] = true;
//				 else
//					 states[j] = false;
//
//			 }
//		 }
//
//		 builder.setMultiChoiceItems(param.getPossibleUser(), states, new DialogInterface.OnMultiChoiceClickListener()
//		 {
//			 public void onClick(DialogInterface dialogInterface, int item, boolean state)
//			 {
//
//			 }
//		 });
//
//		 builder.setPositiveButton("Okay", new DialogInterface.OnClickListener()
//		 {
//			 public void onClick(DialogInterface dialog, int id)
//			 {
//				 SparseBooleanArray checked = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
//				 ArrayList<String> strings = new ArrayList<String>();
//
//				 for(int j = 0; j < checked.size(); j++)
//				 {
//					 if(checked.valueAt(j))
//					 {
//						 strings.add(param.getPossibleSystem()[checked.keyAt(j)]);
//					 }
//				 }
//
//				 String[] values = new String[strings.size()];
//
//				 param.setSet(true);
//				 service.setAreParamsSet(true);
//				 param.setValue(strings.toArray(values));
//
//
//				 if(useDefault.isChecked() && param.getRequiredness() != Requiredness.MANDATORY)
//				 {
//					 useDefault.performClick();
//				 }
//
//				 dialog.dismiss();
//			 }
//		 });
//
//		 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
//		 {
//			 public void onClick(DialogInterface dialog, int id)
//			 {
//				 dialog.cancel();
//			 }
//		 });
//
//		 button.setOnClickListener(new OnClickListener()
//		 {
//			 public void onClick(View v)
//			 {
//				 builder.create().show();
//			 }
//		 });
//
//		 return builder;
//	 }
//
//	 private AlertDialog.Builder setupStringDialog(final ServiceParameter param, Button button, final CheckBox useDefault)
//	 {
//		 final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityParameter.this);
//
//		 // Then add a button to do the selection
//		 button.setText("Set value");
//
//		 builder.setTitle("Enter text value");
//
//		 final EditText stringEdit = new EditText(this);
//		 builder.setView(stringEdit);
//		 if(param.isSet())
//		 {
//			 stringEdit.setText(param.getValue()[0]);
//		 }
//
//		 builder.setPositiveButton("Okay", new DialogInterface.OnClickListener()
//		 {
//			@Override
//			public void onClick(DialogInterface dialog, int which)
//			{
//				param.setValue(new String[] { stringEdit.getText().toString() } );
//
//				 if(useDefault.isChecked() && param.getRequiredness() != Requiredness.MANDATORY)
//				 {
//					 useDefault.performClick();
//				 }
//			}
//		 });
//
//		 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which)
//			{
//				dialog.cancel();
//			}
//		 });
//
//		 return builder;
//	 }
//
//	 private AlertDialog.Builder setupParameterDialog(final ServiceDescription service, final ServiceParameter param, Button button, final CheckBox useDefault)
//	 {
//		 if(param.getType() == Param.MANY_SET)
//		 {
//			 return setupManySetDialog(service, param, button, useDefault);
//		 }
//		 else if(param.getType() == Param.STRING)
//		 {
//			 return setupStringDialog(param, button, useDefault);
//		 }
//
//		 return null;
//	 }
//
//	public void onBackPressed()
//	{
//		finish();
//	}
}
