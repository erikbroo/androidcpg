package com.isanexusdev.androidcpg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;


public class Settings extends PreferenceActivity
implements SharedPreferences.OnSharedPreferenceChangeListener{
	private static final String TAG = Settings.class.getName();

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		getPreferenceManager().setSharedPreferencesName("settings");
		addPreferencesFromResource(R.xml.settings);

		getWindow().getDecorView().setBackgroundColor(0xff909090);
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.i(TAG, key);
	}
}