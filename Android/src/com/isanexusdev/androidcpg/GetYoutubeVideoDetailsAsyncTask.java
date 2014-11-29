package com.isanexusdev.androidcpg;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;

public class GetYoutubeVideoDetailsAsyncTask extends AsyncTask<String, Integer, Bitmap> {
	private static final String TAG = GetYoutubeVideoDetailsAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	String mTitle = "";
	String mDescription = "";
	private GetYoutubeVideoDetailsListener mListener = null;
	public static interface GetYoutubeVideoDetailsListener {
		public void result(Bitmap thumb, String title, String description);
	}
	public GetYoutubeVideoDetailsAsyncTask(String id, GetYoutubeVideoDetailsListener listener){
		mListener = listener;
		try{
			connectURL = new URL("http://gdata.youtube.com/feeds/api/videos/"+id+"?v=2&alt=json");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	protected void onCancelled() {
		if (mListener != null){
			try {
				mListener.result(null,null,null);
			} catch (Exception e) {}
		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (mListener != null){
			try {
				mListener.result(result, mTitle, mDescription);
			} catch (Exception e) {}
		}
	}
	@Override
	protected Bitmap doInBackground(String... params) {
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
			try {
				String thumbUrl = "";
				JSONObject jObject = new JSONObject(encryptedResponse);
				jObject = jObject.getJSONObject("entry");
				try{
					jObject = jObject.getJSONObject("media$group");
				} catch (Exception e){}
				try {
					JSONObject titleObject = jObject.getJSONObject("media$title");
					mTitle = titleObject.getString("$t");
				} catch (Exception e) {}
				try {
					JSONObject titleObject = jObject.getJSONObject("media$description");
					mDescription = titleObject.getString("$t");
				} catch (Exception e) {}
				JSONArray thumbArray = jObject.getJSONArray("media$thumbnail");
				int bestWidth = 0;
				for (int i = 0; i < thumbArray.length(); i++){
					try {
						JSONObject thumb = thumbArray.getJSONObject(i);
						if (!thumb.has("url") || !thumb.has("width")){
							continue;
						}
						int width = thumb.getInt("width");
						if (width <= 0){
							continue;
						}
						if (bestWidth < 128){
							bestWidth = width;
							thumbUrl = thumb.getString("url");
						} else if (bestWidth > width && width > 128){
							bestWidth = width;
							thumbUrl = thumb.getString("url");
						}
					} catch (Exception e){}
				}
				if (thumbUrl.length() > 0){

					try {
						URL thumbURL = new URL(thumbUrl);
						URLConnection connURL = thumbURL.openConnection();
						connURL.connect();
						InputStream is = connURL.getInputStream();
						BufferedInputStream bis = new BufferedInputStream(is);
						Bitmap bm = BitmapFactory.decodeStream(bis);
						try {
							bis.close();
						} catch (Exception e){
							e.printStackTrace();
						}
						try {
							is.close();
						} catch (Exception e){
							e.printStackTrace();
						}

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
							return finalBitmap;
						}catch (Exception e){
							e.printStackTrace();
						}catch (OutOfMemoryError e){}
						return bm;
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			} catch (Exception e){
				e.printStackTrace();;
			}
		}

		return null;
	}
}

