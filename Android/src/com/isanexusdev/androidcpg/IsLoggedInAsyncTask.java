package com.isanexusdev.androidcpg;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.os.AsyncTask;

public class IsLoggedInAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = IsLoggedInAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	private IsLoggedInListener mListener = null;
	public static interface IsLoggedInListener {
		public void result(int result);
	}
	public IsLoggedInAsyncTask(IsLoggedInListener listener){
		mListener = listener;
		try{
			connectURL = new URL(Utils.mHost+"plugins/androidcpg/login.php");
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
			if (encryptedResponse.toLowerCase().contains("form action=\"login.php?referer=")){
				try
				{
					String lineEnd = "\r\n";
					String twoHyphens = "--";
					String boundary = "*****";
					//------------------ CLIENT REQUEST
					try {
						if (conn != null) {
							conn.disconnect();
						}
					} catch (Exception e){}
					conn = (HttpURLConnection) connectURL.openConnection();

					// Allow Inputs
					conn.setDoInput(true);

					// Allow Outputs
					conn.setDoOutput(true);

					// Don't use a cached copy.
					conn.setUseCaches(false);
					conn.setInstanceFollowRedirects(false);
					// Use a post method.
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

					DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );

					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"username\""+ lineEnd + "" + lineEnd+ params[0]);
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"password\""+ lineEnd + "" + lineEnd+ params[1]);
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"remember_me\""+ lineEnd + "" + lineEnd+ "1");
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"submitted\""+ lineEnd + "" + lineEnd+ "1");
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

					// close streams
					dos.flush();
					dos.close();
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
					if (encryptedResponse.toLowerCase().contains("<div class=\"cpg_message_success\">")){
						SharedPreferences settings = AndroidCPG.getSharedPreferences();
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("username", params[0]);
						editor.putString("password", params[1]);
						editor.putString("id", params[1]);
						editor.commit();

						return 1;
					} else {
						return 0;
					}
				}
			} else if (encryptedResponse.toLowerCase().contains("<h2>error</h2>")){
				//Already logged in (logged at browser probably)
				SharedPreferences settings = AndroidCPG.getSharedPreferences();
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("username", params[0]);
				editor.putString("password", params[1]);
				editor.putString("id", params[1]);
				editor.commit();
				return 1;
			} else {
				return 0;
			}
		}
		return -1;
	}

}

