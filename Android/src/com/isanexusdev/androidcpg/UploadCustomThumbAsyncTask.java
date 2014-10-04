package com.isanexusdev.androidcpg;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class UploadCustomThumbAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = UploadCustomThumbAsyncTask.class.getName();
	URL connectURL;
	String mVideoId;
	Bitmap mBitmap = null;
	boolean success;
	private UploadCustomThumbListener mListener = null;
	public static interface UploadCustomThumbListener {
		public void result(int result);
		public void progress(int progress);
	}
	public UploadCustomThumbAsyncTask(Integer videoId, Bitmap bitmap, UploadCustomThumbListener listener){
		mListener = listener;
		mVideoId = String.valueOf(videoId);
		mBitmap = bitmap;
		try{
			connectURL = new URL(Utils.mHost+"displayimage.php?custom_thmb_id="+mVideoId);
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
		success = false;
		String encryptedResponse = null;
		HttpURLConnection conn = null;

		//LET'S GET THE TOKEN AT TIMESTAMP
		try {
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

			try{
				is.close();;
			} catch (Exception e){}
			try{
				conn.disconnect();
			} catch (Exception e){}

			try {
				success = true;
				encryptedResponse=b.toString();
			} catch (Exception e) {
				success = false;
			}
		}
		catch (MalformedURLException ex){
			success = false;
		}catch (IOException ioe){
			success = false;
		}catch (Exception ioe){
			success = false;
		}

		if (success && encryptedResponse != null) {
			success = false;
			try {
				connectURL = new URL(Utils.mHost + "displayimage.php?custom_thmb_id=" + mVideoId);
			} catch (Exception e){
				return 0;
			}

			String token = "";
			String timeStamp = "";
			int tokenIndexStart = encryptedResponse.toLowerCase().indexOf("<input type=\"hidden\" name=\"form_token\" value=\"");
			int tokenIndexEnd = 0;
			if (tokenIndexStart > 0 && tokenIndexStart < encryptedResponse.length()){
				tokenIndexEnd = encryptedResponse.toLowerCase().indexOf("\"",tokenIndexStart + 46);
				if (tokenIndexEnd > tokenIndexStart + 46 && tokenIndexEnd < encryptedResponse.length()){
					token = encryptedResponse.substring(tokenIndexStart + 46, tokenIndexEnd);
				}
			}
			if (token.length() > 0){
				int timeStampIndexStart = encryptedResponse.toLowerCase().indexOf("<input type=\"hidden\" name=\"timestamp\" value=\"");
				int timeStampIndexEnd = 0;
				if (timeStampIndexStart > 0 && timeStampIndexStart < encryptedResponse.length()){
					timeStampIndexEnd = encryptedResponse.toLowerCase().indexOf("\"",timeStampIndexStart + 45);
					if (timeStampIndexEnd > timeStampIndexStart + 45 && timeStampIndexEnd < encryptedResponse.length()){
						timeStamp = encryptedResponse.substring(timeStampIndexStart + 45, timeStampIndexEnd);
					}
				}
			}
			if (token.length() > 0 && timeStamp.length() > 0) {
				try {
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
					conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

					DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

					//<input type="file" name="fileupload" size="40" class="listbox" />
					//<input type="checkbox" name="create_intermediate" />
					//<input type="hidden" name="form_token" value="ed8361f349689626dcb68c0b991f05ab" />
					//<input type="hidden" name="timestamp" value="1411761685" />

					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"form_token\"" + lineEnd + "" + lineEnd + token);
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"timestamp\"" + lineEnd + "" + lineEnd + timeStamp);
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"create_intermediate\"" + lineEnd + "" + lineEnd + "1");
					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"fileupload\";filename=\"th_" + URLEncoder.encode(params[0], "UTF-8") + ".jpg" + "\"" + lineEnd);
					dos.writeBytes(lineEnd);
					// create a buffer of maximum size

					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, dos);

					// send multipart form data necesssary after file data...

					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

					// close streams
					dos.flush();
					dos.close();

					InputStream is = conn.getInputStream();
					// retrieve the response from server
					int ch;

					StringBuffer b = new StringBuffer();
					while ((ch = is.read()) != -1) {
						b.append((char) ch);
					}
					try {
						encryptedResponse = b.toString();
						success = true;
					} catch (Exception e) {
						success = false;
					}
					try{
						is.close();;
					} catch (Exception e){}
					try{
						conn.disconnect();
					} catch (Exception e){}
				} catch (MalformedURLException ex) {
					success = false;
				} catch (IOException ioe) {
					success = false;
				} catch (Exception ioe) {
					success = false;
				}
			}
			if (success && encryptedResponse != null) {
				if (encryptedResponse.toLowerCase().contains("<div class=\"cpg_message_success\">")) {
					return 1;
				} else {
					return 0;
				}
			}
			return 0;
		}
		return 0;
	}
}

