package com.isanexusdev.androidcpg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SendShare extends Activity {
	private static final String TAG = SendShare.class.getName();
	private Spinner mAlbumsList = null;
	private Button mUploadButton = null;
	private Button mNewAlbumButton = null;
	public ProgressBar mUploadprogress = null;
	public ImageView mPreview = null;
	private AlertDialog mAddNewAlbumDialog = null;
	private ProgressDialog mProgressDialog = null;
	private EditText mTitleTextView = null;
	private EditText mCaptionTextView = null;

	private Button mNextItemButton = null;
	private Button mPrevItemButton = null;
	private TextView mPreviewNotAvailable = null;

	private boolean mCanCreateAlbums = false;

	//This is given by the getAlbumsTask, last category position for insertion and user category id (this data is necessary for new album insertion)
	private int mCatPos = 0;
	private int mCatId = 0;
	int loginAtteps = 0;

	int mCurrentItem = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidCPG.setSendShareActivity(this);
		setContentView(R.layout.activity_send_share);
		mPreviewNotAvailable = (TextView) findViewById(R.id.preview_not_available);
		mPreview = (ImageView) findViewById(R.id.preview);
		mUploadButton = (Button) findViewById(R.id.upload);
		mNewAlbumButton = (Button) findViewById(R.id.newalbum);
		mAlbumsList = (Spinner) findViewById(R.id.albums);
		mUploadprogress = (ProgressBar) findViewById(R.id.uploadprogress);
		mTitleTextView = (EditText) findViewById(R.id.image_title);
		mCaptionTextView = (EditText) findViewById(R.id.image_caption);
		mNextItemButton = (Button) findViewById(R.id.next_item);
		mPrevItemButton = (Button) findViewById(R.id.prev_item);
		mNextItemButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentItem++;
				setItemDetails(mCurrentItem);				
			}
		});
		mPrevItemButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentItem--;
				setItemDetails(mCurrentItem);				
			}
		});
		mTitleTextView.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable paramEditable){
				titleChanged(paramEditable.toString());
			}
			public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3){}
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3){}
		});

		mCaptionTextView.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable paramEditable){
				captionChanged(paramEditable.toString());
			}
			public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3){}
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3){}
		});

		// Get intent, action and MIME type
		final Intent intent = getIntent();
		final String action = intent.getAction();
		final String type = intent.getType();

		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null || !uploadService.isUploading) {
			if (uploadService == null) {
				createProgress();
				startService(new Intent(this, UploadService.class));

				//Wait for the UploadService to start and then, continueOnCreate
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						UploadService uploadService = AndroidCPG.getUploadService();
						if (uploadService == null) {
							new Handler().post(this);
							return;
						}
						dissmissProgress();
						continueOnCreate(action, type, intent);
					}
				});
			} else {
				continueOnCreate(action, type, intent);
			}

		} else {
			//Uploas are in progress
			if (Intent.ACTION_SEND.equals(action) && type != null || Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
				//We skip incoming new uploads from user
				Toast.makeText(SendShare.this, R.string.uploads_in_progress_skipping_new_ones, Toast.LENGTH_LONG).show();
			}

			mNewAlbumButton.setEnabled(false);
			mUploadButton.setEnabled(false);
			mAlbumsList.setEnabled(false);
			mTitleTextView.setEnabled(false);
			mCaptionTextView.setEnabled(false);
			mNextItemButton.setEnabled(false);
			mPrevItemButton.setEnabled(false);

			//populate album list
			populateAlbums();
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					mNewAlbumButton.setEnabled(false);
					mUploadButton.setEnabled(false);
					mAlbumsList.setEnabled(false);
					mTitleTextView.setEnabled(false);
					mCaptionTextView.setEnabled(false);
					mNextItemButton.setEnabled(false);
					mPrevItemButton.setEnabled(false);
				}
			});
		}

	}

	void continueOnCreate(String action,  String type, Intent intent){
		boolean handled = false;
		mCurrentItem = 0;
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/") || type.startsWith("video/")) {
				handled = handleSend(intent);
			} else if(type.startsWith("text/")){
				try {
					Bundle extras = intent.getExtras();
					String value = extras.getString(Intent.EXTRA_TEXT);
					String valueLC = value.toLowerCase();
					if (value != null){
						if (valueLC.contains("youtube") || valueLC.contains("youtu.be")){
							int index = valueLC.indexOf(": http://you");
							if (index < 0 || index > valueLC.length()){
								index = valueLC.indexOf(": https://you");
							}
							if (index < 0 || index > valueLC.length()){
								index = valueLC.indexOf(": http://www.you");
							}
							if (index < 0 || index > valueLC.length()){
								index = valueLC.indexOf(": https://www.you");
							}
							if (index >= 0 && index <= value.length()) {
								handled = true;
								UploadService uploadService = AndroidCPG.getUploadService();
								uploadService.mRemoteVideoUploadName = buildValidName(value.substring(0,index))+".youtube";
								uploadService.mRemoteVideoUpload = value.substring(index + 2);
								uploadService.mRemoteVideoUploadDetails = new String[2];
								uploadService.setYoutubeVideoDetails(Utils.extractYoutubeVideoId(uploadService.mRemoteVideoUpload));
								mUploadButton.setText(R.string.uploadyoutube);
								mTitleTextView.setEnabled(true);
								mCaptionTextView.setEnabled(true);
								mNextItemButton.setEnabled(true);
								mPrevItemButton.setEnabled(true);
							} else {
								handled = false;
							}
						} else if ( valueLC.contains("vimeo.com")){
							String name = "";
							String title = extras.getString(Intent.EXTRA_SUBJECT);
							if (title != null && title.trim().length() > 0){
								name = buildValidName(title)+".vimeo";
							} else {
								int index = valueLC.indexOf(".com/");
								if (index >= 0 && index <= value.length()) {
									name = buildValidName(valueLC.substring(index+5))+".vimeo";
								}
							}
							if (name.length() > 0) {
								handled = true;
								UploadService uploadService = AndroidCPG.getUploadService();
								uploadService.mRemoteVideoUploadName = name;
								uploadService.mRemoteVideoUpload = value;
								uploadService.mRemoteVideoUploadDetails = new String[2];
								uploadService.setVimeoVideoDetails(Utils.extractVimeoVideoId(uploadService.mRemoteVideoUpload));
								mUploadButton.setText(R.string.uploadvimeo);
								mTitleTextView.setEnabled(true);
								mCaptionTextView.setEnabled(true);
								mNextItemButton.setEnabled(true);
								mPrevItemButton.setEnabled(true);
							} else {
								handled = false;
							}
						}
					}
				} catch (Exception e){}

			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/") || type.startsWith("video/") || type.startsWith("*/")) {
				handled = handleSendMultiple(intent);
			}
		}


		mNewAlbumButton.setEnabled(false);
		mUploadButton.setEnabled(false);
		mTitleTextView.setEnabled(false);
		mCaptionTextView.setEnabled(false);
		mNextItemButton.setEnabled(false);
		mPrevItemButton.setEnabled(false);
		setItemDetails(mCurrentItem);
		UploadService uploadService = AndroidCPG.getUploadService();
		if (!handled && uploadService.mFileUploads.size() == 0) {
			mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
			//mPreviewNotAvailable.setVisibility(View.VISIBLE);
			mAlbumsList.setEnabled(false);
			Toast.makeText(SendShare.this, R.string.nouploads, Toast.LENGTH_LONG).show();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						finish();
					} catch (Exception e){}
					System.exit(0);
				}
			}, 3000);

			return;
		} else {
			if (!handled){
				mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
				mPreviewNotAvailable.setVisibility(View.VISIBLE);
			} else {
				mPreviewNotAvailable.setVisibility(View.GONE);
			}
			createProgress();
			isLoggedIn();
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		AndroidCPG.setSendShareActivity(this);
		mAlbumsList = (Spinner) findViewById(R.id.albums);
		mUploadprogress = (ProgressBar) findViewById(R.id.uploadprogress);

		mUploadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				UploadService uploadService = AndroidCPG.getUploadService();
				if (uploadService == null){
					startService(new Intent(SendShare.this, UploadService.class));
				}
				//ensure that our uploadService is running
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						UploadService uploadService = AndroidCPG.getUploadService();
						if (uploadService == null){
							new Handler().post(this);
							return;
						}
						uploadService.initializeUploadLists();
						mNewAlbumButton.setEnabled(false);
						mUploadButton.setEnabled(false);
						mAlbumsList.setEnabled(false);
						mTitleTextView.setEnabled(false);
						mCaptionTextView.setEnabled(false);
						mNextItemButton.setEnabled(false);
						mPrevItemButton.setEnabled(false);
						uploadService.uploadNextFile();
					}
				});
			}
		});
		mNewAlbumButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showAddNewAlbumDialog();
			}
		});
	}

	@Override
	protected void onStart(){
		super.onStart();
		AndroidCPG.setSendShareActivity(this);
	}

	@Override
	protected void onPause(){
		super.onPause();
		UploadService uploadService = AndroidCPG.getUploadService();
		boolean stoppedUploadService = true;
		if (uploadService != null) {
			stoppedUploadService = uploadService.stopIfNotUploading();
		}

		if (stoppedUploadService){
			try {
				finish();
			} catch (Exception e){}
			System.exit(0);
		}
	}

	@Override
	protected void onStop(){
		super.onStop();
		UploadService uploadService = AndroidCPG.getUploadService();
		boolean stoppedUploadService = true;
		if (uploadService != null) {
			stoppedUploadService = uploadService.stopIfNotUploading();
		}

		if (stoppedUploadService){
			try {
				finish();
			} catch (Exception e){}
			System.exit(0);
		}
	}

	@Override
	protected void onDestroy(){
		AndroidCPG.setSendShareActivity(null);
		super.onDestroy();
		UploadService uploadService = AndroidCPG.getUploadService();
		boolean stoppedUploadService = true;
		if (uploadService != null) {
			stoppedUploadService = uploadService.stopIfNotUploading();
		}

		if (stoppedUploadService){
			try {
				finish();
			} catch (Exception e){}

			System.exit(0);
		}
	}

	private void dissmissProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null) {
					try {
						mProgressDialog.dismiss();
					} catch (Exception e) {
					}
					mProgressDialog = null;
				}
			}
		});
	}

	private void createProgress(){
		if (mProgressDialog == null){
			mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		} else {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
			}
		}
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setTitle("");
		try {
			View emptyView = new View(this);
			try {
				emptyView.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
			} catch (Exception e){}
			emptyView.setVisibility(View.GONE);
			mProgressDialog.setCustomTitle(emptyView);
		} catch (Exception e){}


		mProgressDialog.setMessage(getString(R.string.wait_a_few));
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}


	void isLoggedIn(){
		SharedPreferences settings = AndroidCPG.getSharedPreferences();
		String username = settings.getString("username", "");
		String password = settings.getString("password", "");
		if (username.length() > 0 && password.length() > 0){
			loginAtteps++;
			if (loginAtteps >= 3){
				loginAtteps = 1;
			}
			Utils.isLoggedIn(username,password, new IsLoggedInAsyncTask.IsLoggedInListener() {
				@Override
				public void result(int result, IsLoggedInAsyncTask isLoggedInAsyncTask) {
					Log.i(TAG, "isLoggedIn result: " + result);
					if (result == 1) {
						if (AndroidCPG.isDoubleLogin() && loginAtteps == 1){
							isLoggedIn();
						} else {
							dissmissProgress();
							isLogged();
						}

					} else {
						if (AndroidCPG.isDoubleLogin() && loginAtteps == 1){
							isLoggedIn();
						} else {
							dissmissProgress();
							notLogged();
						}
					}
				}
			});
		} else {
			notLogged();
		}
	}

	void notLogged(){
		Log.i(TAG, "notLogged: ");
	}

	void isLogged(){
		createProgress();
		Utils.fetchAlbums(new FetchAlbumsAsyncTask.FetchAlbumsListener() {
			@Override
			public void result(FetchAlbumsAsyncTask task, int result) {
				dissmissProgress();
				Log.i(TAG, "fetchAlbums result: " + result);
				if (result == 1) {
					populateAlbumsFromTask(task.getAlbums(), task.canCreate(), task.getCatId(), task.getCatPos());
				} else {
					notLogged();
				}
			}
		});
	}

	void populateAlbumsFromTask(List<String[]> albums, boolean canCreateAlbums, int catId, int catPos){
		//createAlbumButton.setEnabled(canCreateAlbums);
		Log.i(TAG, "num fetchAlbums: " + albums.size()+" canCreateMore: "+canCreateAlbums);
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
			System.exit(0);
			return;
		}
		SharedPreferences settings = AndroidCPG.getSharedPreferences();
		String lastAlbum = settings.getString("lastAlbum", "");
		uploadService.mAlbums = albums;
		uploadService.mAlbumsArray = new String[uploadService.mAlbums.size()];
		uploadService.mSelectedAlbumId = -1;
		for (int i = 0; i <  uploadService.mAlbums.size(); i++){
			String[] albumData = uploadService.mAlbums.get(i);
			uploadService.mAlbumsArray[i] = albumData[1]+ (albumData[2].equals("public") ? " (Publico)":"");
			if (albumData[0].equals(lastAlbum)){
				uploadService.mSelectedAlbumId = i;
			}
		}
		mCanCreateAlbums = canCreateAlbums;
		mCatPos = catPos;
		mCatId = catId;
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				populateAlbums();
			}
		});
	}

	void showAddNewAlbumDialog(){
		if (mAddNewAlbumDialog != null){
			try{
				mAddNewAlbumDialog.dismiss();
			} catch (Exception e){}
		}
		mNewAlbumButton.setEnabled(false);
		mUploadButton.setEnabled(false);
		mTitleTextView.setEnabled(false);
		mCaptionTextView.setEnabled(false);
		mNextItemButton.setEnabled(false);
		mPrevItemButton.setEnabled(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.addnewalbum);

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);

		builder.setView(input);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mAddNewAlbumDialog = null;
				createAlbum(input.getText().toString());

			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				try {
					mNewAlbumButton.setEnabled(mCanCreateAlbums);
					mUploadButton.setEnabled(true);
					mTitleTextView.setEnabled(true);
					mCaptionTextView.setEnabled(true);
					mNextItemButton.setEnabled(true);
					mPrevItemButton.setEnabled(true);
				} catch (Exception e){}
				mAddNewAlbumDialog = null;
			}
		});

		mAddNewAlbumDialog = builder.show();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				input.requestFocus();
				//InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
				mAddNewAlbumDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});

		mAddNewAlbumDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				mAddNewAlbumDialog = null;
				try {
					mNewAlbumButton.setEnabled(mCanCreateAlbums);
					mUploadButton.setEnabled(true);
					mTitleTextView.setEnabled(true);
					mCaptionTextView.setEnabled(true);
					mNextItemButton.setEnabled(true);
					mPrevItemButton.setEnabled(true);
				} catch (Exception e){}
			}
		});
		mAddNewAlbumDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				mAddNewAlbumDialog = null;
				try {
					mNewAlbumButton.setEnabled(mCanCreateAlbums);
					mUploadButton.setEnabled(true);
					mTitleTextView.setEnabled(true);
					mCaptionTextView.setEnabled(true);
					mNextItemButton.setEnabled(true);
					mPrevItemButton.setEnabled(true);
				} catch (Exception e){}

			}
		});
	}

	void createAlbum(String albumName){
		albumName = albumName.trim();
		if (albumName.length() == 0){
			return;
		}

		createProgress();
		Utils.createAlbum(albumName, String.valueOf(mCatId), String.valueOf(mCatPos), new CreateAlbumAsyncTask.CreateAlbumListener() {
			@Override
			public void result(CreateAlbumAsyncTask task, int result) {
				dissmissProgress();
				if (result == 1) {
					final int newAlbumId = task.getNewAlbumId();
					if (newAlbumId >= 0) {
						SharedPreferences.Editor editor = AndroidCPG.getSharedPreferences().edit();
						editor.putString("lastAlbum", String.valueOf(newAlbumId));
						editor.commit();
					}
					Toast.makeText(SendShare.this, R.string.successaddingalbum, Toast.LENGTH_SHORT).show();
					createProgress();
					Utils.fetchAlbums(new FetchAlbumsAsyncTask.FetchAlbumsListener() {
						@Override
						public void result(FetchAlbumsAsyncTask task, int result) {
							dissmissProgress();
							Log.i(TAG, "fetchAlbums result: " + result);
							if (result == 1) {
								if (newAlbumId < 0) {
									UploadService uploadService = AndroidCPG.getUploadService();
									if (uploadService == null){
										//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
										System.exit(0);
										return;
									}
									SharedPreferences.Editor editor = AndroidCPG.getSharedPreferences().edit();
									editor.putString("lastAlbum", uploadService.mAlbums.get(0)[0]);
									editor.commit();
								}
								populateAlbumsFromTask(task.getAlbums(), task.canCreate(), task.getCatId(), task.getCatPos());
							} else {
								notLogged();
							}
						}
					});
				} else {
					Toast.makeText(SendShare.this, R.string.failedaddingalbum, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	void populateAlbums(){
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
			System.exit(0);
			return;
		}

		mNewAlbumButton.setEnabled(mCanCreateAlbums);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.simple_spinner_dropdown_item, uploadService.mAlbumsArray);
		mAlbumsList.setAdapter(adapter);

		if (uploadService.mSelectedAlbumId >= 0) {
			mAlbumsList.setSelection(uploadService.mSelectedAlbumId);
			if (!uploadService.isUploading) {
				mUploadButton.setEnabled(true);
				mTitleTextView.setEnabled(true);
				mCaptionTextView.setEnabled(true);
				mNextItemButton.setEnabled(true);
				mPrevItemButton.setEnabled(true);
			}
		} else if (adapter.getCount() > 0){
			uploadService.mSelectedAlbumId = 0;
			mAlbumsList.setSelection(0);
			SharedPreferences.Editor editor = AndroidCPG.getSharedPreferences().edit();
			editor.putString("lastAlbum", uploadService.mAlbums.get(0)[0]);
			editor.commit();
			if (!uploadService.isUploading) {
				mUploadButton.setEnabled(true);
				mTitleTextView.setEnabled(true);
				mCaptionTextView.setEnabled(true);
				mNextItemButton.setEnabled(true);
				mPrevItemButton.setEnabled(true);
			}
		} else {
			uploadService.mSelectedAlbumId = -1;
			mUploadButton.setEnabled(false);
			mTitleTextView.setEnabled(false);
			mCaptionTextView.setEnabled(false);
			mNextItemButton.setEnabled(false);
			mPrevItemButton.setEnabled(false);
		}

		mAlbumsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				UploadService uploadService = AndroidCPG.getUploadService();
				if (uploadService == null){
					//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
					System.exit(0);
					return;
				}
				uploadService.mSelectedAlbumId = i;
				SharedPreferences.Editor editor = AndroidCPG.getSharedPreferences().edit();
				editor.putString("lastAlbum", uploadService.mAlbums.get(i)[0]);
				editor.commit();
				if (!uploadService.isUploading) {
					mUploadButton.setEnabled(true);
					mTitleTextView.setEnabled(true);
					mCaptionTextView.setEnabled(true);
					mNextItemButton.setEnabled(true);
					mPrevItemButton.setEnabled(true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				UploadService uploadService = AndroidCPG.getUploadService();
				if (uploadService == null){
					//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
					System.exit(0);
					return;
				}
				uploadService.mSelectedAlbumId = -1;
				mUploadButton.setEnabled(false);
				mTitleTextView.setEnabled(false);
				mCaptionTextView.setEnabled(false);
				mNextItemButton.setEnabled(false);
				mPrevItemButton.setEnabled(false);
			}
		});


	}

	boolean handleSend(Intent intent){
		String type = intent.getType();
		if (type.startsWith("image/")){
			handleSendImage(intent);
			return true;
		} else if (type.startsWith("video/")) {
			handleSendVideo(intent);
			return true;
		}

		return false;
	}


	boolean handleSendMultiple(Intent intent){
		String type = intent.getType();
		if (type.startsWith("image/")){
			return handleSendMultipleImages(intent);
		} else if (type.startsWith("video/")) {
			return handleSendMultipleVideos(intent);
		} else if (type.startsWith("*/")) {
			return handleSendMultipleFiles(intent);
		}

		return false;
	}

	void handleSendImage(Intent intent) {
		mUploadButton.setText(R.string.uploadphoto);
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
			System.exit(0);
			return;
		}
		uploadService.mFileUploads.add(imageUri);
		uploadService.mFileUploadsDetails.add(new String[2]);
		handleSendImage(imageUri);
	}

	boolean handleSendImage(Uri imageUri) {
		if (imageUri != null) {
			return setImage(imageUri);
		}
		return false;
	}

	void handleSendVideo(Intent intent) {
		mUploadButton.setText(R.string.uploadvideo);
		Uri videoUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
			System.exit(0);
			return;
		}
		uploadService.mFileUploads.add(videoUri);
		uploadService.mFileUploadsDetails.add(new String[2]);
		handleSendVideo(videoUri);
	}

	boolean handleSendVideo(Uri videoUri) {
		if (videoUri != null) {
			setVideo(videoUri);
			return true;
		}
		return false;
	}

	boolean handleSendMultipleImages(Intent intent) {
		ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (imageUris != null && imageUris.size() > 0) {
			UploadService uploadService = AndroidCPG.getUploadService();
			if (uploadService == null){
				//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
				System.exit(0);
				return true;
			}
			for (Uri imageUri:imageUris){
				uploadService.mFileUploads.add(imageUri);
				uploadService.mFileUploadsDetails.add(new String[2]);
			}
			mUploadButton.setText(String.format(getString(R.string.uploadphotos),uploadService.mFileUploads.size()));
			for (Uri imageUri:imageUris){
				if (handleSendImage(imageUri)){
					break;
				}
				mCurrentItem++;
			}
			return true;
		}
		return false;
	}

	boolean handleSendMultipleVideos(Intent intent) {
		ArrayList<Uri> videoUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (videoUris != null && videoUris.size() > 0) {
			UploadService uploadService = AndroidCPG.getUploadService();
			if (uploadService == null){
				//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
				System.exit(0);
				return true;
			}
			for (Uri videoUri:videoUris){
				uploadService.mFileUploads.add(videoUri);
				uploadService.mFileUploadsDetails.add(new String[2]);
			}
			mUploadButton.setText(String.format(getString(R.string.uploadvideos),uploadService.mFileUploads.size()));
			for (Uri videoUri:videoUris){
				if (handleSendVideo(videoUri)){
					break;
				}
				mCurrentItem++;
			}
			return true;
		}
		return false;
	}

	boolean handleSendMultipleFiles(Intent intent) {
		ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (fileUris != null && fileUris.size() > 0) {
			UploadService uploadService = AndroidCPG.getUploadService();
			if (uploadService == null){
				//At this point we should have our UploadService, so, something very wrong is going on here, exit the app
				System.exit(0);
				return true;
			}
			for (Uri fileUri:fileUris){
				String extension = MimeTypeMap.getFileExtensionFromUrl(Utils.getPathFromUri(fileUri)).toLowerCase();
				if (Utils.IMG_EXT.contains(extension)){
					uploadService.mFileUploads.add(fileUri);
					uploadService.mFileUploadsDetails.add(new String[2]);
				} else if (Utils.VID_EXT.contains(extension)){
					uploadService.mFileUploads.add(fileUri);
					uploadService.mFileUploadsDetails.add(new String[2]);
				}
			}
			mUploadButton.setText(String.format(getString(R.string.uploadfiles),uploadService.mFileUploads.size()));
			for (Uri fileUri:fileUris){
				String extension = MimeTypeMap.getFileExtensionFromUrl(Utils.getPathFromUri(fileUri)).toLowerCase();
				if (Utils.IMG_EXT.contains(extension)){
					if (handleSendImage(fileUri)){
						break;
					}
				} else if (Utils.VID_EXT.contains(extension)){
					if (handleSendVideo(fileUri)){
						break;
					}
				}
				mCurrentItem++;
			}
			return true;
		}
		return false;
	}



	boolean setImage(Uri imageUri){
		try {
			mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
			System.gc();
			mPreview.setImageURI(imageUri);
			mPreviewNotAvailable.setVisibility(View.GONE);
			return true;
		} catch (Exception e){
			mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
			mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(imageUri));
			mPreviewNotAvailable.setVisibility(View.VISIBLE);
			e.printStackTrace();
		} catch (OutOfMemoryError e){
			mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
			mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(imageUri));
			mPreviewNotAvailable.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}
		return false;
	}

	boolean setVideo(Uri videoUri){
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
				MediaPlayer mp = MediaPlayer.create(this, videoUri);
				int duration = mp.getDuration();
				mp.release();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
					mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
					System.gc();
					MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
					mediaMetadataRetriever.setDataSource(this, videoUri);
					mPreview.setImageBitmap(mediaMetadataRetriever.getFrameAtTime(duration / 2, MediaMetadataRetriever.OPTION_CLOSEST));
					mediaMetadataRetriever.release();
					mPreviewNotAvailable.setVisibility(View.GONE);
				} else {
					mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(videoUri));
					mPreviewNotAvailable.setVisibility(View.VISIBLE);
				}
			} else {
				mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
				mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(videoUri));
				mPreviewNotAvailable.setVisibility(View.VISIBLE);
			}
			return true;
		} catch (Exception e){
			mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
			mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(videoUri));
			mPreviewNotAvailable.setVisibility(View.VISIBLE);
			e.printStackTrace();
		} catch (OutOfMemoryError e){
			mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
			mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(videoUri));
			mPreviewNotAvailable.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}
		return false;
	}

	void setDetailsFromRemoterUrl(Bitmap thumb, String title, String description){
		mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
		System.gc();
		mPreview.setImageBitmap(thumb);
		String[] details = new String[2];
		if (title != null && title.trim().length() > 0){
			mTitleTextView.setText(title.trim());
			details[0] = mTitleTextView.getText().toString();
		}
		
		if (description != null && description.trim().length() > 0){
			mCaptionTextView.setText(description.trim());
			details[1] = mCaptionTextView.getText().toString();
		}
		
		AndroidCPG.getUploadService().mRemoteVideoUploadDetails = details;
	}

	String buildValidName(String tittle){
		char fileSep = '/';
		int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
		Arrays.sort(illegalChars);
		StringBuilder filename= new StringBuilder();
		int len = tittle.length();
		for (int i = 0; i < Math.min(len,60); i++) {
			char ch = tittle.charAt(i);
			if (ch < ' ' || ch >= 0x7F || ch == fileSep || (ch == '.' && i == 0) || Arrays.binarySearch(illegalChars, ch) >= 0) {
				continue;
			} else {
				filename.append(ch);
			}
		}
		return filename.toString();
	}

	void titleChanged(String newText){
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			return;
		}
		if (mCurrentItem < 0 || mCurrentItem >= uploadService.mFileUploadsDetails.size()){
			if (uploadService.mRemoteVideoUploadDetails != null){
				String[] changedItem = uploadService.mRemoteVideoUploadDetails;
				changedItem[0] = newText;
				uploadService.mRemoteVideoUploadDetails = changedItem;
			}
			return;
		}
		String[] changedItem = uploadService.mFileUploadsDetails.get(mCurrentItem);
		changedItem[0] = newText;
		uploadService.mFileUploadsDetails.set(mCurrentItem, changedItem);
	}

	void captionChanged(String newText){
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			return;
		}
		if (mCurrentItem < 0 || mCurrentItem >= uploadService.mFileUploadsDetails.size()){
			if (uploadService.mRemoteVideoUploadDetails != null){
				String[] changedItem = uploadService.mRemoteVideoUploadDetails;
				changedItem[1] = newText;
				uploadService.mRemoteVideoUploadDetails = changedItem;
			}
			return;
		}
		String[] changedItem = uploadService.mFileUploadsDetails.get(mCurrentItem);
		changedItem[1] = newText;
		uploadService.mFileUploadsDetails.set(mCurrentItem, changedItem);
	}

	public void setItemDetails(int currentItem){
		UploadService uploadService = AndroidCPG.getUploadService();
		if (uploadService == null){
			return;
		}
		if (uploadService.mFileUploadsDetails.size() <= 1){
			mNextItemButton.setVisibility(View.GONE);
			mPrevItemButton.setVisibility(View.GONE);
		} else {
			if (currentItem >= uploadService.mFileUploadsDetails.size()-1){
				mNextItemButton.setVisibility(View.GONE);
			} else{
				mNextItemButton.setVisibility(View.VISIBLE);
			}
			if (currentItem == 0 ){
				mPrevItemButton.setVisibility(View.GONE);
			} else {
				mPrevItemButton.setVisibility(View.VISIBLE);
			}
		}

		if (uploadService.mFileUploadsDetails.size() >= 1){
			//it is supposed to be in file upload mode
			if (currentItem >= 0 && currentItem < uploadService.mFileUploadsDetails.size()){
				String[] item = uploadService.mFileUploadsDetails.get(currentItem);
				mTitleTextView.setText(item[0]);
				mCaptionTextView.setText(item[1]);

				Uri fileUri = uploadService.mFileUploads.get(currentItem);
				String extension = MimeTypeMap.getFileExtensionFromUrl(Utils.getPathFromUri(fileUri)).toLowerCase();
				if (Utils.IMG_EXT.contains(extension)){
					setImage(fileUri);
				} else if (Utils.VID_EXT.contains(extension)){
					setVideo(fileUri);
				} else {
					mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
					mPreviewNotAvailable.setText(getString(R.string.preview_not_available)+"\r\n"+Utils.getPathFromUri(fileUri));
					mPreviewNotAvailable.setVisibility(View.VISIBLE);
				}
			}
		} else {
			//it is supposed to be a remote video upload
			if (uploadService.mRemoteVideoUploadDetails != null){
				mTitleTextView.setText(uploadService.mRemoteVideoUploadDetails[0]);
				mCaptionTextView.setText(uploadService.mRemoteVideoUploadDetails[1]);
			}
		}
	}
}
