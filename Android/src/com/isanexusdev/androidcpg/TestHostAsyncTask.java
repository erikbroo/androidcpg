package com.isanexusdev.androidcpg;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.SharedPreferences;
import android.os.AsyncTask;

public class TestHostAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = TestHostAsyncTask.class.getName();
	HttpClient mClient;
	private TestHostListener mListener = null;
	public static interface TestHostListener {
		public void result(TestHostAsyncTask task, int result);
	}
	public TestHostAsyncTask(TestHostListener listener){
		mListener = listener;
	}


	@Override
	protected void onCancelled() {
		if (mListener != null){
			try {
				mListener.result(this, 0);
			} catch (Exception e) {}
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (mListener != null){
			try {
				mListener.result(this, result);
			} catch (Exception e) {}
		}
	}
	@Override
	protected Integer doInBackground(String... params) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);

		try {
			mClient = new DefaultHttpClient(httpParameters);
		} catch (Exception e){
			return 0;
		}


		//Let's check if it is a coppermine gallery by testing index.php, db_input.php, delete.php and login.php
		int result = test(params[0]+"index.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 2;
		}

		result = test(params[0]+"db_input.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 2;
		}

		result = test(params[0]+"delete.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 2;
		}

		result = test(params[0]+"login.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 2;
		}

		//Let's check if AndroidCPG plugin is installed by checking db_input.php, delete.php, login.php and getAlbums.php

		result = test(params[0]+"plugins/androidcpg/db_input.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 3;
		}

		result = test(params[0]+"plugins/androidcpg/delete.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 3;
		}

		result = test(params[0]+"plugins/androidcpg/login.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 3;
		}

		result = test(params[0]+"plugins/androidcpg/getAlbums.php");
		if (result == -1){
			return 0;
		} else if (result == 0){
			return 3;
		}

		SharedPreferences settings = AndroidCPG.getSharedPreferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("host", params[0]);
		editor.commit();

		return 1;
	}

	private int test(String url){
		HttpGet request = null;
		int result = -1;
		try{
			request = new HttpGet(url);
			HttpResponse response = mClient.execute(request);
			int code = response.getStatusLine().getStatusCode();

			if (code == 200){
				result =  1;
			} else {
				result = 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (request != null){
			try {
				request.abort();
			} catch (Exception e) {}
		}

		return result;
	}
}

