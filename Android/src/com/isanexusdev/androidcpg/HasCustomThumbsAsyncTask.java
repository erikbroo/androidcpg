package com.isanexusdev.androidcpg;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class HasCustomThumbsAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = HasCustomThumbsAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	private HasCustomThumbsListener mListener = null;
	public static interface HasCustomThumbsListener {
		public void result(int result);
	}
	public HasCustomThumbsAsyncTask(HasCustomThumbsListener listener){
		mListener = listener;
		try{
			connectURL = new URL(Utils.mHost+"plugins/custom_thumb/codebase.php");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	protected void onCancelled() {
		if (mListener != null){
			try {
				mListener.result(0);
			} catch (Exception e) {}
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (mListener != null){
			try {
				mListener.result(result);
			} catch (Exception e) {}
		}
	}
	@Override
	protected Integer doInBackground(String... params) {
		success = true;
		String encryptedResponse = null;
		HttpURLConnection conn = null;
		try
		{
			//------------------ CLIENT REQUEST

			// Open a HTTP connection to the URL
			System.setProperty("http.keepAlive", "false");
			conn = (HttpURLConnection) connectURL.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(false);
			InputStream is = conn.getInputStream();
			// retrieve the response from server
			int ch;

			StringBuffer b =new StringBuffer();
			while( ( ch = is.read() ) != -1 ){
				b.append( (char)ch );
			}
			try {
				encryptedResponse=b.toString();
			} catch (Exception e) {
				success = false;
			}

			try {
				is.close();
			} catch (Exception e){

			}
			try {
				conn.disconnect();
			} catch (Exception e){}
		}
		catch (MalformedURLException ex){
			success = false;
		}catch (IOException ioe){
			success = false;
		}catch (Exception ioe){
			success = false;
		}

		if (success && encryptedResponse != null){
			if (encryptedResponse.toLowerCase().contains("not in coppermine...")){
				return 1;
			}
		}
		return -1;
	}

}

