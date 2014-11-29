package com.isanexusdev.androidcpg;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ResultsActivity extends Activity{

	private static final String TAG = ResultsActivity.class.getName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(123456432);
		setContentView(R.layout.results_activity);
		Button exit = (Button)findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		LinearLayout successUploadsLayout = (LinearLayout)findViewById(R.id.success_uploads_layout);
		LinearLayout failedUploadsLayout = (LinearLayout)findViewById(R.id.failed_uploads_layout);
		ListView successUploadsView = (ListView)successUploadsLayout.findViewById(R.id.success_uploads);
		ListView failedUploadsView = (ListView)failedUploadsLayout.findViewById(R.id.failed_uploads);
		TextView noUploadInfoView = (TextView)findViewById(R.id.no_upload_info);


		SharedPreferences settings = AndroidCPG.getSharedPreferences();
		String lastUploads = settings.getString("lastUploads", "");
		int nSuccessUploads = 0;
		int nFailedUploads = 0;
		String[] successUploads = null;
		String[] failedUploads = null;
		if (lastUploads.length() > 0){
			try{
				JSONObject lastUploadsJSN = new JSONObject(lastUploads);
				JSONArray successUploadsArray = lastUploadsJSN.getJSONArray("successUploadsArray");
				JSONArray failedUploadsArray = lastUploadsJSN.getJSONArray("failedUploadsArray");
				successUploads = new String[successUploadsArray.length()];
				failedUploads = new String[failedUploadsArray.length()];
				for (int i = 0; i < successUploadsArray.length(); i++){
					String tmpJSNString = "";
					try {
						tmpJSNString = successUploadsArray.getString(i);
						nSuccessUploads++;
					} catch (Exception e) {}
					successUploads[i] = tmpJSNString;
				}
				for (int i = 0; i < failedUploadsArray.length(); i++){
					String tmpJSNString = "";
					try {
						tmpJSNString = failedUploadsArray.getString(i);
						nFailedUploads++;
					} catch (Exception e) {}
					failedUploads[i] = tmpJSNString;
				}
			} catch (Exception e){}
		}
		
		successUploadsLayout.setVisibility((nSuccessUploads == 0 ? View.GONE:View.VISIBLE));
		failedUploadsLayout.setVisibility((nFailedUploads == 0 ? View.GONE:View.VISIBLE));
		noUploadInfoView.setVisibility((nSuccessUploads + nFailedUploads > 0 ? View.GONE:View.VISIBLE));
		
		if (successUploads == null || nSuccessUploads == 0){
			successUploads = new String[0];
		}
		if (failedUploads == null || nFailedUploads == 0){
			failedUploads = new String[0];
		}
		
		successUploadsView.setAdapter(new ArrayAdapter<String> (this, R.layout.listviewitem_success, R.id.text1, successUploads));
		failedUploadsView.setAdapter(new ArrayAdapter<String> (this, R.layout.listviewitem_failed, R.id.text1, failedUploads));
	}

	@Override
	protected void onResume(){
		super.onResume();
	}

	@Override
	protected void onPause(){
		super.onPause();
		UploadService uploadService = AndroidCPG.getUploadService();
		if ( uploadService == null || !uploadService.isUploading){
			System.exit(0);
		}
	}

	@Override
	protected void onStop(){
		super.onStop();
		UploadService uploadService = AndroidCPG.getUploadService();
		if ( uploadService == null || !uploadService.isUploading){
			System.exit(0);
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		UploadService uploadService = AndroidCPG.getUploadService();
		if ( uploadService == null || !uploadService.isUploading){
			System.exit(0);
		}
	}
}
