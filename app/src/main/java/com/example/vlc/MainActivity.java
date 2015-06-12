package com.example.vlc;



import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;



public class MainActivity extends Activity  implements DetectionFragment.OnRecordBtnClickListener{

	protected static final String TAG = "MainActivity";

	private ImageButton mDetection,mPosition,mSetting;
	private View currentButton;

//	public TextView showIDView;
//	public TextView showStatusView;

	private boolean isRecording = false;// ???????????????
	public long VLCID = 0;// ??????
	public int frequency = 32000;// ??????????????????32k????????Щ?????????32k??????????????????????
	public short audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// ????λ????????16λ
	public short channelConfiguration = AudioFormat.CHANNEL_IN_MONO;// ?????
	private Handler handler,recordHandler;// ?????????????????????

	RecordThread recordThread;
	DetectionFragment detectfm = null;
	PositionFragment positionfm = null;
	SettingFragment settingfm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//TextView detail = (TextView)findViewById(R.id.detail);
		//detail.setText("My Awesome Text");

		findView();
		init();

	}

	private void findView(){

		mDetection=(ImageButton) findViewById(R.id.buttom_detection);
		mPosition=(ImageButton) findViewById(R.id.buttom_position);
		mSetting=(ImageButton) findViewById(R.id.buttom_setting);

	}

	private void init(){
		mDetection.setOnClickListener(detectionOnClickListener);
		mPosition.setOnClickListener(positionOnClickListener);
		mSetting.setOnClickListener(settingOnClickListener);
		mDetection.performClick();

		handler = new Handler() {// ????????????????????
			public void handleMessage(Message msg) {
				String message = (String) msg.obj;
				//	if (isRecording)
				//		showIDView.setText(message);
			}

		};

		recordHandler = new Handler() {// ??????????????????
			public void handleMessage(Message msg) {
				//long result = (Long) msg.obj;
				String  result = (String) msg.obj;
				long mapID = 3408;//a postion under the map
				if(result =="4K") mapID = 1408;
				else if (result =="8K") mapID = 2410;
				//	System.out.println("result =" + result);

				if(detectfm != null) {
					detectfm.sendVlcId(result,isRecording);
				}

				else if(positionfm != null){
					MapView mapview = (MapView)findViewById(R.id.mapview);
					if(mapview!= null)
						mapview.sendVlcId(mapID,isRecording);
				}


			}

		};

	}

	public Handler getHandler() {// ???????
		return this.handler;
	}



	private OnClickListener detectionOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			positionfm = null;
			settingfm = null;
			/* ???? Bundle ????, Activity ????? Fragment ?????????????????д??? */
			Bundle recordstate = new Bundle();
	        /* ???????? Bundle ??????, ??????????ü?? */
			recordstate.putBoolean(DetectionFragment.TAG_ID, isRecording);

			FragmentManager fm=getFragmentManager();
			FragmentTransaction ft=fm.beginTransaction();
			//	DetectionFragment detectfm = new DetectionFragment();
			detectfm = new DetectionFragment();

			/* ?? Activity ?????????? ????? Fragment ???? */
			detectfm.setArguments(recordstate);

			ft.replace(R.id.fl_content,detectfm,MainActivity.TAG);
			ft.commit();
			setButton(v);

		}

	};

	private OnClickListener positionOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			detectfm = null;
			settingfm = null;
			FragmentManager fm=getFragmentManager();
			FragmentTransaction ft=fm.beginTransaction();
			//	PositionFragment positionfm = new PositionFragment();
			positionfm = new PositionFragment();
			ft.replace(R.id.fl_content,positionfm,MainActivity.TAG);
			ft.commit();
			setButton(v);
		}
	};

	private OnClickListener settingOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			detectfm = null;
			positionfm = null;
			FragmentManager fm=getFragmentManager();
			FragmentTransaction ft=fm.beginTransaction();
			//	SettingFragment settingfm = new SettingFragment();
			settingfm = new SettingFragment();
			ft.replace(R.id.fl_content,settingfm,MainActivity.TAG);
			ft.commit();
			setButton(v);
		}
	};


	private void setButton(View v){
		if(currentButton!=null&&currentButton.getId()!=v.getId()){
			currentButton.setEnabled(true);
		}
		v.setEnabled(false);
		currentButton=v;
	}


	// 求采样数据均值，用于判决高低电平
	private int avg(int[] data) {
		int length = data.length;
		int sum = 0;
		for (int i = 0; i < length; i++) {
			sum = sum + data[i];
		}
		sum = sum / length;
		return sum;
	}


	public String getCode(int dataBinary,int numberBinary,String resultBinary){
		int repeat = 0;
		if(numberBinary%4==3) repeat = numberBinary/4+1;
		else if((numberBinary%4==1)||(numberBinary%4==0)) repeat = numberBinary/4;
		if(repeat > 0)
			for (int i = 0;i<repeat;i++){
				resultBinary = resultBinary + dataBinary;
			}
		return resultBinary;
	}


	public void onStartBtnClicked(boolean Record){

		isRecording = Record;

//		showIDView = (TextView) findViewById(R.id.detected_id);
//		showStatusView = (TextView) findViewById(R.id.detect_status_tv);

		if(isRecording)
		{
			recordThread = new RecordThread();
			recordThread.start();
		}
		else
		{
			//	showIDView.setText("");
			//	showStatusView.setText("");
		}

	}

	class RecordThread extends Thread {

		RecordThread() {
			super();
		}
		@Override
		public void run() {

			// ????????????????????С??buffer????
			final int bufferSize = AudioRecord.getMinBufferSize(frequency,
					channelConfiguration, audioEncoding);
			// ?????AudioRecord
			final AudioRecord audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC, frequency,
					channelConfiguration, audioEncoding, bufferSize);
			audioRecord.startRecording();

			while (isRecording) {
				short[] buffer = new short[bufferSize];
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);// ????bufferSize?????????



				// ????β????????????????????λ??к??0????flag??0???????len?ó???0?????
				// ?????0??????????????β??????
				// ?????0????????????β?????????????????
				// ????β??????????0
				// ??????????????????ж?????????β???????????????????0????
				int flag = 0;
				for (int i = 0; i < bufferReadResult; i++) {
					if (buffer[i] != 0) {
						flag = i;
						break;
					}
				}
				int len = bufferReadResult - flag - 10;
				//	System.out.println(bufferReadResult);
				//	System.out.println(len);
				int[] tmp;


				short temp[];
				if (len < 50) {
					tmp = new int[50];

					temp = new short[50];
					for (int i = 0; i < 50; i++)
						tmp[i] = 0;
					continue;
				} else {// ??????????????16λ???????????????int?????????????tmp
					tmp = new int[len];
					temp = new short[len];
					for (int i = flag; i < flag + len; i++) {
						tmp[i - flag] = buffer[i];
					}
				}
				//最大值
				int maxBuffer = 0;
				for (int i = 0; i < buffer.length; i++) {
					maxBuffer = Math.max(maxBuffer,buffer[i]);
				}
				int start = 0;
				//信号达到一定强度，start
				if(maxBuffer > 100) start = 1;

				int data[] = new int[buffer.length];

				for(int i = 0; i < bufferSize; i++)
				{
					data[i] = (int)buffer[i];
				}


				//判断高低电平
				//String outputBinary = "";

				int avg = avg(data);
				for (int i = 0; i < buffer.length; i++) {
					if (data[i] >= avg)
						data[i] = 0;
					else
						data[i] = 1;
				}

				String showLetter = "";
				String tempValue = "";
				String tempValueOld = "";

				for (int i = 1,count = 0,success = 0; i < buffer.length; i++) {

					if (data[i-1] != data[i]) {

						//outputBinary = outputBinary +"0";
						if(count > 5) {
							tempValue = "4K";

						}
						else if(count > 3){
							tempValue = "8K";

						}
						//else showLetter = "No signal "+ count;
						count = 0;

						if((tempValue != "")&&(tempValue == tempValueOld)){
							success++;

						}
						else success = 0;

						if(success == 4){
							showLetter = tempValue;
							break;
						}
						tempValueOld = tempValue;

					}


					else {

						//outputBinary = outputBinary + "2";

						count++;
					}

				}







				//decode


				if(start == 0) showLetter = "No signal";

				if(detectfm !=null){
					SoundWave soundwave = (SoundWave)findViewById(R.id.soundwave);
					if(soundwave != null)
						soundwave.sndAudioBuf(buffer,bufferReadResult);
				}

				// tmp.length约1910

				System.out.println(showLetter);


				Message message = Message.obtain();
				message.obj = String.valueOf(showLetter);

				recordHandler.sendMessage(message);

			}
			audioRecord.stop();
			audioRecord.release();
		}
	}



}