package com.isanexusdev.androidcpg;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import android.os.AsyncTask;

public class CreateAlbumAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = CreateAlbumAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	int mNewAlbumId = -1;
	private CreateAlbumListener mListener = null;
	public static interface CreateAlbumListener {
		public void result(CreateAlbumAsyncTask task, int result);
	}
	public CreateAlbumAsyncTask(CreateAlbumListener listener){
		mListener = listener;
	}


	public int getNewAlbumId(){
		return mNewAlbumId;
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
		success = true;
		String encryptedResponse = null;
		HttpURLConnection conn = null;

		try
		{
			connectURL = new URL(Utils.mHost+"plugins/androidcpg/delete.php?what=albmgr");

			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";
			//------------------ CLIENT REQUEST
			conn = (HttpURLConnection) connectURL.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			conn.setChunkedStreamingMode(1024 * 128);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"name\""+ lineEnd + "" + lineEnd+ params[0]);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"cat\""+ lineEnd + "" + lineEnd+ params[1]);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"op\""+ lineEnd + "" + lineEnd+ "add");
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"position\""+ lineEnd + "" + lineEnd+ params[2]);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploader\""+ lineEnd + "" + lineEnd+ "android");
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

		if (success && encryptedResponse != null) {
			try {
				JSONObject json = new JSONObject(encryptedResponse);
				if (json.has("message") && json.getString("message").equalsIgnoreCase("true")){
					if (json.has("newAid")){
						mNewAlbumId = json.getInt("newAid");
					} else {
						mNewAlbumId = -1;
					}
					return 1;
				}
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}
}

