package com.appglue;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentComponentListRemote extends FragmentComponentList
{


	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle)
	{
		View v = super.onCreateView(inflater, container, icicle);
		((TextView) v.findViewById(R.id.simple_list_none)).setText("No remote components");

		return v;
	}
	
	public void onActivityCreated(Bundle icicle)
	{
		super.onActivityCreated(icicle);

//		serviceListView.setOnItemClickListener(new OnItemClickListener()
//		{
//			@Override
//			public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
//			{
//				Intent intent = new Intent(getActivity(), ActivityComponent.class);
//				intent.putExtra(SERVICE_TYPE, ServiceType.REMOTE.index);
//				intent.putExtra(CLASSNAME, services.get(position).getClassName());
//				intent.putExtra(JUST_A_LIST, justList);
//				getActivity().startActivityForResult(intent, 0);
//			}
//		});
    }
}
