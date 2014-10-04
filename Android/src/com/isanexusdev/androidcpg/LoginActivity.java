package com.isanexusdev.androidcpg;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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

	private Button mTestHostButton;
	private EditText mHost;
	private String mHostAddress = "";
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
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// Reset errors.
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
			Utils.isLoggedIn(email, password, new IsLoggedInAsyncTask.IsLoggedInListener() {
				@Override
				public void result(int result) {
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
						return;
					} else {
						mLoginSuccessView.setVisibility(View.GONE);
						Toast.makeText(LoginActivity.this, R.string.loginfailed, Toast.LENGTH_SHORT).show();
						mEmailSignInButton.setEnabled(true);
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



