package com.isanexusdev.androidcpg;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

public class FetchAlbumsAsyncTask extends AsyncTask<String, Integer, Integer> {
	private static final String TAG = FetchAlbumsAsyncTask.class.getName();
	URL connectURL;
	boolean success;
	List<String[]> mAlbums = new ArrayList<String[]>();
	boolean mCancreate = false;
	int mCatid = 0;
	int mCatpos = 0;
	private FetchAlbumsListener mListener = null;
	public static interface FetchAlbumsListener {
		public void result(FetchAlbumsAsyncTask task, int result);

	}

	public FetchAlbumsAsyncTask(FetchAlbumsListener listener){
		mListener = listener;
		try{
			connectURL = new URL(Utils.mHost+"plugins/androidcpg/getAlbums.php");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public List<String[]> getAlbums(){
		return mAlbums;
	}

	public boolean canCreate(){
		return mCancreate;
	}
	public int getCatPos(){
		return mCatpos;
	}
	public int getCatId(){
		return mCatid;
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
			String[] lines = encryptedResponse.split("\\r\\n");
			if (lines.length < 4|| !lines[0].equals("OK")){
				return 0;
			} else {
				mCancreate = (lines[1].equals("cancreate"));
				mCatid = Integer.parseInt(lines[2]);
				mCatpos = Integer.parseInt(lines[3]);
				for (int i = 4; i < lines.length; i++){
					mAlbums.add(lines[i].split("\\|sep\\|"));
				}
			}

			return 1;
		}
		return -1;
	}

}

