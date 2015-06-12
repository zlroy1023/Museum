package com.example.vlc;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingFragment extends Fragment{

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		 View view = inflater.inflate(R.layout.fragment_setting, container, false);
		 
		 ListView list = (ListView) view.findViewById(R.id.sample_listView);
		 
		 final String[] strs = new String[] {
			    "Auto", "32K", "16k", " 8k" };
		
		

		list.setAdapter(new ArrayAdapter<String> (this.getActivity(), android.R.layout.simple_list_item_1, strs));	

		
			 
			 
			 
		return view;
	}
}