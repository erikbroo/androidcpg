package com.isanexusdev.androidcpg;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;

public class GetVineVideoDetailsAsyncTask extends AsyncTask<String, Integer, Bitmap> {
	private static final String TAG = GetVineVideoDetailsAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	String mTitle = "";
	String mDescription = "";
	private GetVineVideoDetailsListener mListener = null;
	public static interface GetVineVideoDetailsListener {
		public void result(Bitmap thumb, String title, String description);
	}
	public GetVineVideoDetailsAsyncTask(String id, GetVineVideoDetailsListener listener){
		mListener = listener;
		try{
			//connectURL = new URL("https://vine.co/v/"+id);
			connectURL =  new URL("https://vine.co/oembed/"+id+".json");
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
			conn.setInstanceFollowRedirects(true);
			//conn.setRequestMethod("GET");
			//conn.setRequestProperty("User-Agent", "Mozilla/17.0 (X11; Linux x86_512; rv:956.0) Gecko/20290101 Firefox/94.0");
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
				//String thumbUrl = GetVineVideoDetailsAsyncTask.getProperty(encryptedResponse, "image");
				//mTitle = GetVineVideoDetailsAsyncTask.getProperty(encryptedResponse, "title");
				//mDescription = GetVineVideoDetailsAsyncTask.getProperty(encryptedResponse, "description");
				JSONObject jObject = null;
				try {
					jObject = new JSONObject(encryptedResponse);	
				} catch (Exception e) {
					jObject = (new JSONArray(encryptedResponse)).getJSONObject(0);
				}
				mTitle = jObject.getString("author_name");
				if (mTitle != null && mTitle.length() > 0){
					mTitle = "Vine.co - "+ mTitle;
				}
				mDescription = jObject.getString("title");
				String thumbUrl = jObject.getString("thumbnail_url");
				if (thumbUrl != null && thumbUrl.length() > 0){
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
	
	/**private static String getProperty(final String data, final String property){
		int indexStart = data.indexOf("og:"+property);
		if (indexStart < 0){
			return "";
		}
		indexStart = data.indexOf("content", indexStart);
		if (indexStart < 0){
			return "";
		}
		indexStart = data.indexOf("\"", indexStart);
		if (indexStart < 0){
			return "";
		}
		int indexEnd =data.indexOf("\"", indexStart + 1);
		if (indexEnd < 0){
			return "";
		}
		return data.substring(indexStart+1,indexEnd);
	}*/
}

