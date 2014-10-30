package com.isanexusdev.androidcpg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends Activity{
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private static final String TAG = LoginActivity.class.getName();

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormScrollView;
	private View mLoginSuccessView;
	private View mUsernameAndPasswordFormView;
	private Button mEmailSignInButton;
	private Button mShowResultButton;
	

	private Button mTestHostButton;
	private EditText mHost;
	private String mHostAddress = "";
	private String mLastResult = "";
	

	int loginAtteps = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UploadService uploadService = AndroidCPG.getUploadService();
		boolean stoppedUploadService = true;
		if (uploadService != null) {
			stoppedUploadService = uploadService.stopIfNotUploading();
		}

		//If uploads are in progress, login activity is not allowed to be launched
		if (!stoppedUploadService){
			Toast.makeText(this, R.string.uploads_in_progress_login_not_allowed, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mHost = (EditText) findViewById(R.id.host);

		mHost.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					testHost();
					return true;
				}
				return false;
			}
		});
		mHost.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				mUsernameAndPasswordFormView.setVisibility(View.GONE);
				if (mHost.getText().toString().trim().length() > 0) {
					mTestHostButton.setEnabled(true);
				} else {
					mTestHostButton.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});


		mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
		
		mShowResultButton = (Button) findViewById(R.id.show_result_button);
		mShowResultButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				WebDialog dialog = new WebDialog(LoginActivity.this, mLastResult);
		        dialog.setCancelable(true);
		        dialog.show();
			}
		});
		
		

		mTestHostButton = (Button) findViewById(R.id.testhost);
		mTestHostButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				testHost();
			}
		});

		mLoginFormScrollView = findViewById(R.id.login_form_scroll);
		mUsernameAndPasswordFormView = findViewById(R.id.usernameandpassword_form);
		mLoginSuccessView = findViewById(R.id.login_success);
		mProgressView = findViewById(R.id.login_progress);
	}


	@Override
	protected void onStart(){
		super.onStart();
		UploadService uploadService = AndroidCPG.getUploadService();
		boolean stoppedUploadService = true;
		if (uploadService != null) {
			stoppedUploadService = uploadService.stopIfNotUploading();
		}

		//If uploads are in progress, login activity is not allowed to be launched
		if (!stoppedUploadService){
			Toast.makeText(this, R.string.uploads_in_progress_login_not_allowed, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		UploadService uploadService = AndroidCPG.getUploadService();
		boolean stoppedUploadService = true;
		if (uploadService != null) {
			stoppedUploadService = uploadService.stopIfNotUploading();
		}

		//If uploads are in progress, login activity is not allowed to be launched
		if (!stoppedUploadService){
			Toast.makeText(this, R.string.uploads_in_progress_login_not_allowed, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		SharedPreferences settings = AndroidCPG.getSharedPreferences();
		String username = settings.getString("username", "");
		String password = settings.getString("password", "");
		String host = settings.getString("host", "");
		mUsernameView.setText(username);
		mPasswordView.setText(password);
		if (host.length() > 0){
			mHostAddress = host;
			mHost.setText(mHostAddress);
		}

		if (mHostAddress.length() > 0 && username.length() > 0 && password.length() > 0){
			mUsernameAndPasswordFormView.setVisibility(View.VISIBLE);
			attemptLogin();
		} else {
			mEmailSignInButton.setEnabled(true);
		}
		
		if (mProgressView.getVisibility() == View.GONE && mLoginSuccessView.getVisibility() == View.GONE && mUsernameAndPasswordFormView.getVisibility() == View.VISIBLE &&
				mLastResult != null && mLastResult.trim().length() > 0){
			mShowResultButton.setVisibility(View.VISIBLE);
		} else {
			mShowResultButton.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.optionmenudeletecredentials:
			SharedPreferences settings = AndroidCPG.getSharedPreferences();
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("username", "");
			editor.putString("password", "");
			editor.putString("lastAlbum", "");
			editor.putString("host", "");
			editor.commit();
			return true;
		case R.id.optionmenusettings:
			Intent i = new Intent(getApplicationContext(), com.isanexusdev.androidcpg.Settings.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			return true;
		case R.id.optionmenuclose:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// Reset errors.
		loginAtteps++;
		if (loginAtteps >= 3){
			loginAtteps = 1;
		}
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;


		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			showProgress(false);
			mEmailSignInButton.setEnabled(true);
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mEmailSignInButton.setEnabled(false);
			showProgress(true);
			mLastResult = "";
			mShowResultButton.setVisibility(View.GONE);
			Utils.isLoggedIn(email, password, new IsLoggedInAsyncTask.IsLoggedInListener() {
				@Override
				public void result(int result, IsLoggedInAsyncTask isLoggedInAsyncTask) {
					showProgress(false);
					Log.i(TAG, "isLoggedIn result: " + result);
					if (result == 1) {
						Toast.makeText(LoginActivity.this, R.string.loginsuccess, Toast.LENGTH_LONG).show();
						mEmailSignInButton.setEnabled(true);
						mProgressView.setVisibility(View.GONE);
						mLoginFormScrollView.setVisibility(View.GONE);
						mLoginSuccessView.setVisibility(View.VISIBLE);
						mLoginSuccessView.findViewById(R.id.goback).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								mLoginFormScrollView.setVisibility(View.VISIBLE);
								mLoginSuccessView.setVisibility(View.GONE);
							}
						});
						mLoginSuccessView.findViewById(R.id.exit).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								finish();
								System.exit(0);
							}
						});
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
							}
						}, 100);
						getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

						if (AndroidCPG.isDoubleLogin() && loginAtteps == 1){
							attemptLogin();
						} else {
							Utils.fetchAlbums(new FetchAlbumsAsyncTask.FetchAlbumsListener() {
								@Override
								public void result(FetchAlbumsAsyncTask task, int result) {
									Log.i(TAG, "fetchAlbums result: " + result);
								}
							});
						}
					} else {
						mLastResult = isLoggedInAsyncTask.getReply();
						if (AndroidCPG.isDoubleLogin() && loginAtteps == 1){
							attemptLogin();
						} else {
							mLoginSuccessView.setVisibility(View.GONE);
							Toast.makeText(LoginActivity.this, R.string.loginfailed, Toast.LENGTH_SHORT).show();
							mEmailSignInButton.setEnabled(true);
						}
					}
					
					if (mLastResult != null && mLastResult.trim().length() > 0){
						mShowResultButton.setVisibility(View.VISIBLE);
					} else {
						mShowResultButton.setVisibility(View.GONE);
					}
				}
			});
		}
	}


	private void testHost(){

		mHostAddress = mHost.getText().toString().trim();
		if (mHostAddress.length() == 0){
			return;
		}
		if (!mHostAddress.toLowerCase().startsWith("http://") && !mHostAddress.toLowerCase().startsWith("https://")){
			mHostAddress = "http://" + mHostAddress;
		}

		if (!mHostAddress.toLowerCase().endsWith("/")){
			mHostAddress = mHostAddress + "/";
		}

		if (mHostAddress != mHost.getText().toString()){
			mHost.setText(mHostAddress);
		}

		showProgress(true);
		Utils.testHost(mHostAddress, new TestHostAsyncTask.TestHostListener() {
			@Override
			public void result(TestHostAsyncTask task, int result) {
				showProgress(false);
				if (result == 1){
					Utils.mHost = mHostAddress;
					Toast.makeText(LoginActivity.this, R.string.hostcorrect, Toast.LENGTH_LONG).show();
					mUsernameAndPasswordFormView.setVisibility(View.VISIBLE);
					mUsernameView.requestFocus();
					if (mUsernameView.getText().toString().length() > 0 && mPasswordView.getText().toString().length() > 0){
						attemptLogin();
					}
					return;
				} else if (result == 2){
					Toast.makeText(LoginActivity.this, R.string.hostnotcorrect_nocoppermine, Toast.LENGTH_LONG).show();
				} else if (result == 3){
					Toast.makeText(LoginActivity.this, R.string.hostnotcorrect_noplugin, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(LoginActivity.this, R.string.hostnotcorrect, Toast.LENGTH_LONG).show();
				}

				mUsernameAndPasswordFormView.setVisibility(View.GONE);
			}
		});
	}
	/**
	 * Shows the progress UI and hides the login form.
	 */
	public void showProgress(final boolean show) {
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
}



