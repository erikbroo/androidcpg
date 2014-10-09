package com.isanexusdev.androidcpg;

import java.net.CookieHandler;
import java.net.CookieManager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class AndroidCPG extends Application{

	private static Context context;
	private static CookieManager cookieManager;

	private static UploadService sUploadService = null;
	private static SendShare sSendShareActivity = null;

	public static UploadService getUploadService(){
		return sUploadService;
	}

	public static void setUploadService(UploadService uploadService){
		AndroidCPG.sUploadService = uploadService;
	}

	public static SendShare getSendShareActivity(){
		return sSendShareActivity;
	}

	public static void setSendShareActivity(SendShare sendShareActivity){
		sSendShareActivity = sendShareActivity;
	}

	public void onCreate(){
		super.onCreate();
		context = getApplicationContext();
		cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}

	public static Context getAppContext() {
		return context;
	}



	public static SharedPreferences getSharedPreferences(){
		return context.getSharedPreferences("settings", 0);
	}
	public static CookieManager getCookieManager(){
		return cookieManager;
	}

	public static boolean isDoubleLogin(){
		return getSharedPreferences().getBoolean("doublelogin", false);
	}
}
