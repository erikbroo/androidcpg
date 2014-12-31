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

public class UploadRemoteVideoAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = UploadRemoteVideoAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	private UploadRemoteVideoListener mListener = null;
	public static interface UploadRemoteVideoListener {
		public void result(int result);
		public void progress(int progress);
	}
	public UploadRemoteVideoAsyncTask(UploadRemoteVideoListener listener){
		mListener = listener;
		try{
			connectURL = new URL(Utils.mHost+"plugins/androidcpg/db_input.php");
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
			conn.setInstanceFollowRedirects(false);
			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"album\""+ lineEnd + "" + lineEnd+ params[0]);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			String title = "";
			if (params[4] != null){
				title = params[4].trim();
			}
			dos.writeBytes("Content-Disposition: form-data; name=\"title\""+ lineEnd + "" + lineEnd+ title);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			String caption = "";
			if (params[5] != null){
				caption = params[5].trim();
			}
			dos.writeBytes("Content-Disposition: form-data; name=\"caption\""+ lineEnd + "" + lineEnd+ caption);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"keywords\""+ lineEnd + "" + lineEnd+ "" );
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"event\""+ lineEnd + "" + lineEnd+ "picture");
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploader\""+ lineEnd + "" + lineEnd+ "android");
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"userpicture\";filename=\"" + URLEncoder.encode(params[1], "UTF-8") + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes(params[2]);
			mListener.progress(80);

			// send multipart form data necesssary after file data...
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
			int successIndex = encryptedResponse.toLowerCase().indexOf("<div class=\"cpg_message_success\">");
			if (successIndex > 0 && successIndex < encryptedResponse.length()){
				if (params[3].length() > 0){
					HasCustomThumbsAsyncTask hasCustomThumbsAsyncTask = new HasCustomThumbsAsyncTask(null);
					Integer result = hasCustomThumbsAsyncTask.doInBackground();
					if (result == 1) {
						int videoId = -1;
						encryptedResponse = encryptedResponse.substring(successIndex);
						int displayImageIdIndex = encryptedResponse.toLowerCase().indexOf("<a href=\"displayimage.php?pid=");
						if (displayImageIdIndex > 0 && displayImageIdIndex < encryptedResponse.length()) {
							encryptedResponse = encryptedResponse.substring(displayImageIdIndex + 30);
							int displayImageIdEndIndex = encryptedResponse.indexOf("\"");
							if (displayImageIdEndIndex > 0 && displayImageIdEndIndex < encryptedResponse.length()) {
								try {
									videoId = Integer.parseInt(encryptedResponse.substring(0, displayImageIdEndIndex));
								} catch (Exception e) {
									videoId = -1;
								}
							}
						}
						if (videoId >= 0) {
							Bitmap bm = null;
							if (params[1].contains("youtube")){
								GetYoutubeVideoDetailsAsyncTask getYoutubeVideoThumbAsyncTask = new GetYoutubeVideoDetailsAsyncTask(params[3], null);
								bm = getYoutubeVideoThumbAsyncTask.doInBackground();
							} else if (params[1].contains("vimeo")){
								GetVimeoVideoDetailsAsyncTask getVimeoVideoThumbAsyncTask = new GetVimeoVideoDetailsAsyncTask(params[3], null);
								bm = getVimeoVideoThumbAsyncTask.doInBackground();
							} else if (params[1].contains("vine")){
								GetVineVideoDetailsAsyncTask getVineVideoThumbAsyncTask = new GetVineVideoDetailsAsyncTask(params[3], null);
								bm = getVineVideoThumbAsyncTask.doInBackground();
							}
							if (bm != null) {
								UploadCustomThumbAsyncTask uploadCustomThumbAsyncTask = new UploadCustomThumbAsyncTask(videoId, bm, null);
								uploadCustomThumbAsyncTask.doInBackground(params[1]);
							}
						}
					}
				}
				mListener.progress(100);
				return 1;
			} else {
				return 0;
			}
		}
		return 0;
	}
}

