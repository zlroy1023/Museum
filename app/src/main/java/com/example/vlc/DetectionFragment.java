package com.example.vlc;




import android.app.Activity;
import android.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;




	public class DetectionFragment extends Fragment {

		/* Bundle??key */
		public static final String TAG_ID = "DetectionFragment";

		public Button start_btn;
		public Button stop_btn;

		public TextView showIDView;
		public TextView showStatusView;

		private OnRecordBtnClickListener startListener;


		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			View view =  inflater.inflate(R.layout.fragment_detection, container, false);


			start_btn = (Button) view.findViewById(R.id.start_detect_btn);
			stop_btn = (Button) view.findViewById(R.id.stop_detect_btn);

			showIDView = (TextView) view.findViewById(R.id.detected_id);
			showStatusView = (TextView) view.findViewById(R.id.detect_status_tv);


		 /* §µ?? ???????????? TAG_ID ???*/
			boolean isIllegal = getArguments().containsKey(TAG_ID);

			if(isIllegal){
            /* ??????? TAG_ID ???, ??????????? value */
				boolean recordstate = getArguments().getBoolean(TAG_ID);
				if(recordstate)
					start_btn.setEnabled(false);
				else
					stop_btn.setEnabled(false);
			}



			start_btn.setOnClickListener(new start_btn_Click());// ??????
			stop_btn.setOnClickListener(new stop_btn_Click());// ??????

			return view;
		}


		class start_btn_Click implements View.OnClickListener {
			public void onClick(View arg0) {
				//	id_tv.setText("003");
				//	status_tv.setText("004");
				start_btn.setEnabled(false);
				stop_btn.setEnabled(true);

				startListener.onStartBtnClicked(true);
			}
		}

		class stop_btn_Click implements View.OnClickListener {
			public void onClick(View arg0) {
				//	id_tv.setText("001");
				//	status_tv.setText("002");
				start_btn.setEnabled(true);
				stop_btn.setEnabled(false);
				showIDView.setText("");
				showStatusView.setText("");

				startListener.onStartBtnClicked(false);
			}
		}


		public void sendVlcId(String _vlcid, boolean recording) {
			String vlcid = _vlcid;

			if(recording) {
				if(vlcid != null) {
					showIDView.setText(vlcid);
					// showIDView.setText(String.format("%1$#x", result)); // ???????
				}
				else {
					showIDView.setText(String.valueOf("No Signal"));
				}
				showStatusView.setText("Detecting LED...");
			}
			else {
				showIDView.setText("");
				showStatusView.setText("");
			}

		}

		public interface OnRecordBtnClickListener{
			public void onStartBtnClicked(boolean Record);
		}


		public void onAttach(Activity activity)
		{
			super.onAttach(activity);
			try
			{
				startListener = (OnRecordBtnClickListener) activity;//?????????????
			}
			catch (ClassCastException e)
			{
				throw new ClassCastException(activity.toString() + "must implement OnStartBtnClickListener");//?????????????Activity?????????????????????????????????????????????
			}
		}





	}