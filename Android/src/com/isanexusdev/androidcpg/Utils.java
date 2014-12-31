package com.isanexusdev.androidcpg;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils {
	public static String mHost = "";
	public static final List<String> IMG_EXT = new ArrayList<String>();
	public static final List<String> VID_EXT = new ArrayList<String>();

	static{
		try {
			mHost = AndroidCPG.getSharedPreferences().getString("host","");
		} catch (Exception e){
			e.printStackTrace();
		}

		IMG_EXT.add("jpg");
		IMG_EXT.add("bmp");
		IMG_EXT.add("gif");
		IMG_EXT.add("iff");
		IMG_EXT.add("jb2");
		IMG_EXT.add("jp2");
		IMG_EXT.add("jpc");
		IMG_EXT.add("jpe");
		IMG_EXT.add("jpeg");
		IMG_EXT.add("jpx");
		IMG_EXT.add("png");
		IMG_EXT.add("psd");
		IMG_EXT.add("swc");

		VID_EXT.add("mpg");
		VID_EXT.add("m4v");
		VID_EXT.add("avi");
		VID_EXT.add("flv");
		VID_EXT.add("3gp");
		VID_EXT.add("asf");
		VID_EXT.add("asx");
		VID_EXT.add("mov");
		VID_EXT.add("mp4");
		VID_EXT.add("mpeg");
		VID_EXT.add("ogv");
		VID_EXT.add("swf");
		VID_EXT.add("webm");
		VID_EXT.add("wmv");

	}

	public static void testHost(String host, TestHostAsyncTask.TestHostListener listener){
		TestHostAsyncTask testHostAsyncTask = new TestHostAsyncTask(listener);
		testHostAsyncTask.execute(host);
	}

	public static void isLoggedIn(String username, String password, IsLoggedInAsyncTask.IsLoggedInListener listener){
		IsLoggedInAsyncTask isLoggedInAsyncTask = new IsLoggedInAsyncTask(listener);
		isLoggedInAsyncTask.execute(username,password);
	}

	public static void fetchAlbums(FetchAlbumsAsyncTask.FetchAlbumsListener listener){
		FetchAlbumsAsyncTask fetchAlbumsAsyncTask = new FetchAlbumsAsyncTask(listener);
		fetchAlbumsAsyncTask.execute();
	}

	public static void uploadFile(String albumId, String fileName, String title, String caption, UploadFileAsyncTask.UploadFileListener listener){
		UploadFileAsyncTask uploadFileAsyncTask = new UploadFileAsyncTask(listener);
		uploadFileAsyncTask.execute(albumId,fileName, title, caption);
	}

	public static void uploadRemoteVideo(String albumId, String videoName, String videoUrl, String videoId, String title, String caption,  UploadRemoteVideoAsyncTask.UploadRemoteVideoListener listener){
		UploadRemoteVideoAsyncTask uploadRemoteVideoAsyncTask = new UploadRemoteVideoAsyncTask(listener);
		uploadRemoteVideoAsyncTask.execute(albumId,videoName, videoUrl, videoId, title, caption);
	}

	public static void getYoutubeVideoDetails(String id, GetYoutubeVideoDetailsAsyncTask.GetYoutubeVideoDetailsListener listener){
		GetYoutubeVideoDetailsAsyncTask getYoutubeVideoDetails = new GetYoutubeVideoDetailsAsyncTask(id, listener);
		getYoutubeVideoDetails.execute();
	}

	public static void getVimeoVideoDetails(String id, GetVimeoVideoDetailsAsyncTask.GetVimeoVideoDetailsListener listener){
		GetVimeoVideoDetailsAsyncTask getVimeoVideoDetails = new GetVimeoVideoDetailsAsyncTask(id, listener);
		getVimeoVideoDetails.execute();
	}
	
	public static void getVineVideoDetails(String id, GetVineVideoDetailsAsyncTask.GetVineVideoDetailsListener listener){
		GetVineVideoDetailsAsyncTask getVineVideoDetails = new GetVineVideoDetailsAsyncTask(id, listener);
		getVineVideoDetails.execute();
	}

	public static void createAlbum(String albumName, String catId, String catPos, CreateAlbumAsyncTask.CreateAlbumListener listener){
		CreateAlbumAsyncTask createAlbumAsyncTask = new CreateAlbumAsyncTask(listener);
		createAlbumAsyncTask.execute(albumName,catId,catPos);
	}

	public static String getPathFromUri(Uri uri) {
		String uriString = uri.toString();
		String path = "";
		if (uriString.toLowerCase().startsWith("content:")) {
			try {
				Cursor c = AndroidCPG.getAppContext().getContentResolver().query(Uri.parse(uriString/**"content://media/external/images/media/1"*/), null, null, null, null);
				c.moveToNext();
				path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
				c.close();
				return path;
			} catch (Exception e){}
		}

		return uri.getPath();
	}

	public static String extractYoutubeVideoId(String url){
		String urlLC = url.toLowerCase();
		int index = urlLC.indexOf(".be/");
		if (index < 0 || index > url.length()){
			index = urlLC.indexOf(".com/");
		}
		if (index < 0 || index > url.length()) {
			return "";
		}
		url = url.substring(index);
		urlLC = url.toLowerCase();
		index = urlLC.indexOf("watch?v=");
		if (index > 0 && index < url.length()){
			index = index + 8;
		} else {
			index = urlLC.indexOf("/");
			if (index > 0 && index < url.length()){
				index = index + 1;
			}
		}

		if (index < 0 || index > url.length()) {
			return "";
		}

		url = url.substring(index);
		index = url.indexOf("&");
		if (index > 0 && index < url.length()){
			url = url.substring(0,index);
		}

		index = url.indexOf("?");
		if (index > 0 && index < url.length()){
			url = url.substring(0,index);
		}

		return url;
	}

	public static String extractVimeoVideoId(String url){
		url = url.trim();
		String urlLC = url.toLowerCase();
		int index = urlLC.indexOf(".com/");
		if (index < 0 || index > url.length()) {
			return "";
		}
		url = url.substring(index+5);
		if (url.endsWith("/")){
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}
	
	public static String extractVineVideoId(String url){
		url = url.trim();
		String urlLC = url.toLowerCase();
		int index = urlLC.indexOf(".co/v/");
		if (index < 0 || index > url.length()) {
			return "";
		}
		url = url.substring(index+6);
		if (url.endsWith("/")){
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public static boolean isVideo(String filename){
		try {
			String ext = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
			return VID_EXT.contains(ext);
		} catch (Exception e){}

		return false;
	}

	public static String getStackTrace(Throwable t){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

}
