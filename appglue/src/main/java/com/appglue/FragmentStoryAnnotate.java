package com.appglue;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.appglue.engine.CompositeService;
import com.appglue.serviceregistry.Registry;

public class FragmentStoryAnnotate extends Fragment implements OnClickListener
{
	private Registry registry;
	
	private EditText nameText;
	private EditText descriptionText;
	private TextView doneButton;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        View v =  inflater.inflate(R.layout.fragment_story_annotate, container, false);
        
        registry = Registry.getInstance(getActivity());

        nameText = (EditText) v.findViewById(R.id.name_text);
        descriptionText = (EditText) v.findViewById(R.id.story_description_text);
        
        doneButton = (TextView) v.findViewById(R.id.done_button);
        doneButton.setOnClickListener(this);
        
        return v;
    }
	
	public void onClick(View v)
	{
		if(v.equals(doneButton))
		{
			String name = nameText.getText().toString();
			String description = descriptionText.getText().toString();
			
			CompositeService cs = registry.getService();
			cs.setName(name);
			cs.setDescription(description);
			
			registry.saveComposite(cs);
			
			getActivity().finish();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		
	}
}
