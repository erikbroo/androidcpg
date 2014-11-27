package com.isanexusdev.androidcpg;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

public class UploadFileAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = UploadFileAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	private UploadFileListener mListener = null;
	public static interface UploadFileListener {
		public void result(int result);
		public void progress(int progress);
	}
	public UploadFileAsyncTask(UploadFileListener listener){
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
		File file = null;
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

			conn.setChunkedStreamingMode(1024 * 1024);

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
			if (params[2] != null){
				title = params[2].trim();
			}
			dos.writeBytes("Content-Disposition: form-data; name=\"title\""+ lineEnd + "" + lineEnd+ title);
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			String caption = "";
			if (params[3] != null){
				caption = params[3].trim();
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
			file = new File(params[1]);
			dos.writeBytes("Content-Disposition: form-data; name=\"userpicture\";filename=\"" + URLEncoder.encode(file.getName(), "UTF-8") + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			// create a buffer of maximum size
			byte[] buf = new byte[1024*1024]; // 1M buffer
			int readBytes;

			InputStream is = new FileInputStream(file);

			long size = file.length();
			long uploaded = 0;
			while((readBytes = is.read(buf)) != -1) {
				dos.write(buf, 0, readBytes);
				uploaded = uploaded + readBytes;
				mListener.progress((int)(Math.min(uploaded * 100 / size,95)));
			}

			// send multipart form data necesssary after file data...

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			dos.flush();
			dos.close();
			is.close();
			is = conn.getInputStream();
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
			} catch (Exception e){}
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
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1 && Utils.isVideo(file.getName())) {
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
							try {
								Bitmap bm = null;
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
									MediaPlayer mp = MediaPlayer.create(AndroidCPG.getAppContext(), Uri.fromFile(file));
									int duration = mp.getDuration();
									mp.release();
									MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
									mediaMetadataRetriever.setDataSource(AndroidCPG.getAppContext(), Uri.fromFile(file));
									bm = mediaMetadataRetriever.getFrameAtTime(duration / 2, MediaMetadataRetriever.OPTION_CLOSEST);
									mediaMetadataRetriever.release();
								}
								if (bm != null) {
									try{
										int bmWidth = bm.getWidth();
										int bmHeight = bm.getHeight();
										if (bmWidth != 128 && bmHeight != 128 || bmWidth > 128 || bmHeight > 128) {
											if (bmWidth > bmHeight) {
												bm = Bitmap.createScaledBitmap(bm, 128, 128*bmHeight/bmWidth,true);
											} else {
												bm = Bitmap.createScaledBitmap(bm, 128*bmWidth/bmHeight, 128,true);
											}
										}

										Bitmap overlay = BitmapFactory.decodeResource(AndroidCPG.getAppContext().getResources(), R.drawable.play_button);
										Bitmap finalBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());

										Canvas canvas = new Canvas(finalBitmap);
										int left = ( bm.getWidth() - overlay.getWidth()) / 2;
										int top = (bm.getHeight() - overlay.getHeight()) / 2;
										canvas.drawBitmap(bm, 0,0, null);
										canvas.drawBitmap(overlay, left,top, null);
										if (finalBitmap != null){
											bm = finalBitmap;
										}
									}catch (Exception e){
										e.printStackTrace();
									}catch (OutOfMemoryError e){}
									UploadCustomThumbAsyncTask uploadCustomThumbAsyncTask = new UploadCustomThumbAsyncTask(videoId, bm, null);
									uploadCustomThumbAsyncTask.doInBackground(params[1]);
								}
							} catch (Exception e){}
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

