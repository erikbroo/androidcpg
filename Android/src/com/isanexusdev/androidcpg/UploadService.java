package com.isanexusdev.androidcpg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class UploadService extends Service {
	private final int MAX_UPDATE_TICKER_INTERVAL = 30 * 1000;
	private final int MAX_UPDATE_NOTIFICATION_INTERVAL = 3 * 1000;
	long lastProgressUpdateTime = 0;

	public int mCurrentFileIndex = -1;
	public List<String[]> mAlbums = new ArrayList<String[]>();
	public String[] mAlbumsArray = new String[0];
	public String mRemoteVideoUpload = new String();
	public String mRemoteVideoUploadName = new String();
	public String[] mRemoteVideoUploadDetails = null;
	public int mRemoteVideoUploadResult = 0;
	public List<Uri> mFileUploads = new ArrayList<Uri>();
	public List<String[]> mFileUploadsDetails = new ArrayList<String[]>();
	public List<Uri> mFileUploadsFailed = new ArrayList<Uri>();
	public List<Uri> mFileUploadsSuccess = new ArrayList<Uri>();
	public int mSelectedAlbumId = -1;
	boolean isUploading = false;

	public UploadService() {
		AndroidCPG.setUploadService(this);
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDestroy() {
		AndroidCPG.setUploadService(null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void initializeUploadLists(){
		mFileUploadsFailed = new ArrayList<Uri>();
		mFileUploadsSuccess = new ArrayList<Uri>();
		mCurrentFileIndex = 0;
	}

	public boolean stopIfNotUploading(){
		if (isUploading){
			return false;
		}
		stopSelf();
		return true;
	}

	public void setYoutubeVideoDetails(String id){
		if (id == null || id.length() == 0){
			return;
		}

		Utils.getYoutubeVideoDetails(id, new GetYoutubeVideoDetailsAsyncTask.GetYoutubeVideoDetailsListener() {
			@Override
			public void result(final Bitmap thumb, final String title, final String description) {
				final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
				if (sendShareActivity != null) {
					sendShareActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sendShareActivity.setDetailsFromRemoterUrl(thumb,title,description);
						}
					});
				}
			}
		});
	}

	public void setVimeoVideoDetails(String id){
		if (id == null || id.length() == 0){
			return;
		}

		Utils.getVimeoVideoDetails(id, new GetVimeoVideoDetailsAsyncTask.GetVimeoVideoDetailsListener() {
			@Override
			public void result(final Bitmap thumb, final String title, final String description) {
				final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
				if (sendShareActivity != null) {
					sendShareActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sendShareActivity.setDetailsFromRemoterUrl(thumb,title,description);
						}
					});
				}
			}
		});
	}
	
	public void setVineVideoDetails(String id){
		if (id == null || id.length() == 0){
			return;
		}

		Utils.getVineVideoDetails(id, new GetVineVideoDetailsAsyncTask.GetVineVideoDetailsListener() {
			@Override
			public void result(final Bitmap thumb, final String title, final String description) {
				final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
				if (sendShareActivity != null) {
					sendShareActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sendShareActivity.setDetailsFromRemoterUrl(thumb,title,description);
						}
					});
				}
			}
		});
	}

	void uploadNextFile() {
		isUploading = true;
		SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
		if (mCurrentFileIndex >= mFileUploads.size() && mRemoteVideoUpload.length() == 0){
			if (sendShareActivity != null) {
				sendShareActivity.finish();
			}
			if (mCurrentFileIndex > 0) {
				saveLastUpload();
				postFinishedNotification(123456432);
			}

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					stopSelf();
					isUploading = false;
					System.exit(0);
				}
			}, 3000);

			return;
		}

		if (mRemoteVideoUpload.length() > 0){
			Utils.uploadRemoteVideo(mAlbums.get(mSelectedAlbumId)[0], mRemoteVideoUploadName, mRemoteVideoUpload, Utils.extractYoutubeVideoId(mRemoteVideoUpload), mRemoteVideoUploadDetails[0], mRemoteVideoUploadDetails[1], new UploadRemoteVideoAsyncTask.UploadRemoteVideoListener() {
				@Override
				public void result(final int result) {
					final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
					lastProgressUpdateTime = 0;
					mRemoteVideoUploadResult = result;
					if (sendShareActivity != null) {
						sendShareActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (result == 1) {
									sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
									sendShareActivity.mUploadprogress.setProgress(100);
									try {
										Toast.makeText(sendShareActivity, String.format(getString(R.string.successuploading), mRemoteVideoUpload), Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
									}
								} else {
									sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
									sendShareActivity.mUploadprogress.setProgress(0);
									try {
										Toast.makeText(sendShareActivity, String.format(getString(R.string.faileduploading), mRemoteVideoUpload), Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
									}
								}
								fileLoadedResultNotification(result);
								mRemoteVideoUpload = "";
								mCurrentFileIndex++;
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										uploadNextFile();
									}
								});
							}
						});
					} else {
						if (result == 1) {
							try {
								Toast.makeText(AndroidCPG.getAppContext(), String.format(getString(R.string.successuploading), mRemoteVideoUpload), Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
							}
						} else {
							try {
								Toast.makeText(AndroidCPG.getAppContext(), String.format(getString(R.string.faileduploading), mRemoteVideoUpload), Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
							}
						}
						fileLoadedResultNotification(result);
						mRemoteVideoUpload = "";
						mCurrentFileIndex++;
						new Handler().post(new Runnable() {
							@Override
							public void run() {
								uploadNextFile();
							}
						});
					}
				}

				@Override
				public void progress(final int progress) {
					final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
					updateProgressNotification(progress);
					if (sendShareActivity != null) {
						sendShareActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
								sendShareActivity.mUploadprogress.setProgress(progress);
							}
						});
					}
				}
			});
		} else {
			final Uri currentUri = mFileUploads.get(mCurrentFileIndex);
			final String[] details = mFileUploadsDetails.get(mCurrentFileIndex); 
			final String filePath = Utils.getPathFromUri(currentUri);
			if (sendShareActivity != null) {
				sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
				sendShareActivity.mUploadprogress.setProgress(0);
				if (mCurrentFileIndex >= 0 && mCurrentFileIndex < mFileUploadsDetails.size()){
					sendShareActivity.setItemDetails(mCurrentFileIndex);
				}
			}

			Utils.uploadFile(mAlbums.get(mSelectedAlbumId)[0], filePath, details[0], details[1], new UploadFileAsyncTask.UploadFileListener() {
				@Override
				public void result(final int result) {
					final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
					lastProgressUpdateTime = 0;
					if (sendShareActivity != null) {
						sendShareActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (result == 1) {
									sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
									sendShareActivity.mUploadprogress.setProgress(100);
									mFileUploadsSuccess.add(currentUri);
									try {
										Toast.makeText(sendShareActivity, String.format(getString(R.string.successuploading), new File(filePath).getName()), Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
									}
								} else {
									sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
									sendShareActivity.mUploadprogress.setProgress(0);
									mFileUploadsFailed.add(currentUri);
									try {
										Toast.makeText(sendShareActivity, String.format(getString(R.string.faileduploading), new File(filePath).getName()), Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
									}
								}
								fileLoadedResultNotification(result);
								mCurrentFileIndex++;
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										uploadNextFile();
									}
								});
							}
						});
					} else {
						if (result == 1) {
							mFileUploadsSuccess.add(currentUri);
							try {
								Toast.makeText(AndroidCPG.getAppContext(), String.format(getString(R.string.successuploading), new File(filePath).getName()), Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
							}
						} else {
							mFileUploadsFailed.add(currentUri);
							try {
								Toast.makeText(AndroidCPG.getAppContext(), String.format(getString(R.string.faileduploading), new File(filePath).getName()), Toast.LENGTH_SHORT).show();
							} catch (Exception e) {
							}
						}
						fileLoadedResultNotification(result);
						mCurrentFileIndex++;
						new Handler().post(new Runnable() {
							@Override
							public void run() {
								uploadNextFile();
							}
						});
					}
				}

				@Override
				public void progress(final int progress) {
					final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
					updateProgressNotification(progress);
					if (sendShareActivity != null) {
						sendShareActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
								sendShareActivity.mUploadprogress.setProgress(progress);
							}
						});
					}
				}
			});
		}
	}

	private void updateProgressNotification(int progress){
		if (System.currentTimeMillis() - lastProgressUpdateTime < MAX_UPDATE_NOTIFICATION_INTERVAL){
			return;
		}
		String ticker = "";
		String notificationText = "";
		if (mRemoteVideoUpload.length() == 0) {
			final Uri currentUri = mFileUploads.get(mCurrentFileIndex);
			final String filePath = Utils.getPathFromUri(currentUri);
			notificationText = String.format(getString(R.string.uploadprogressnotification), new File(filePath).getName(), progress);
		} else {
			notificationText = String.format(getString(R.string.uploadprogressnotification), mRemoteVideoUpload, progress);
		}

		if (System.currentTimeMillis() - lastProgressUpdateTime > MAX_UPDATE_TICKER_INTERVAL) {
			ticker = notificationText;
			lastProgressUpdateTime = System.currentTimeMillis();
		}
		postNotification(123456432, getString(R.string.uploading),notificationText,ticker,true, progress);
	}

	private void fileLoadedResultNotification(int result){
		String notificationText;
		if (mRemoteVideoUpload.length() == 0) {
			final Uri currentUri = mFileUploads.get(mCurrentFileIndex);
			final String filePath = Utils.getPathFromUri(currentUri);
			if (result == 1) {
				notificationText = String.format(getString(R.string.successuploading), new File(filePath).getName());
			} else {
				notificationText = String.format(getString(R.string.faileduploading), new File(filePath).getName());
			}
		} else {
			if (result == 1) {
				notificationText = String.format(getString(R.string.successuploading), mRemoteVideoUpload);
			} else {
				notificationText = String.format(getString(R.string.faileduploading), mRemoteVideoUpload);
			}
		}

		postNotification(123456432, getString(R.string.upload),notificationText,notificationText,true,0);
	}

	private void postNotification(int notificationId, String title, String text, String ticker, boolean service, int progress) {
		Context context = AndroidCPG.getAppContext();
		try {
			Intent intent = new Intent(this, SendShare.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
			if (progress > 0){
				notification.contentIntent = contentIntent;
				notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_with_progress);
				notification.contentView.setImageViewResource(R.id.notification_with_progress_icon, R.drawable.ic_launcher);
				notification.contentView.setTextViewText(R.id.notification_with_progress_text, text);
				notification.contentView.setTextViewText(R.id.notification_with_progress_percent, String.valueOf(progress)+"%");
				notification.contentView.setProgressBar(R.id.notification_with_progress_bar, 100, progress, false);
			} else {
				notification.setLatestEventInfo(context, title, text, contentIntent);
			}
			if(ticker.length() > 0){
				notification.tickerText= ticker;
			}
			NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notificationId, notification);
		} catch (Exception e){}
	}

	void postFinishedNotification(int notificationId) {
		if ((mFileUploads.size() == 0  && mRemoteVideoUploadName.length() == 0) || mCurrentFileIndex == 0){
			return;
		}
		Context context = AndroidCPG.getAppContext();
		try {
			String message = "";
			if (mRemoteVideoUploadName.length() == 0){
				message = String.format(getString(R.string.finishednotificationdetails), mFileUploads.size(), mFileUploadsSuccess.size(), mFileUploadsFailed.size());
			} else {
				message = String.format(getString(R.string.finishednotificationdetails), 1, (mRemoteVideoUploadResult == 1 ? 1:0), (mRemoteVideoUploadResult == 1 ? 0:1));
			}
			String title = getString(R.string.finisheduploading);

			Intent intent = new Intent(this, ResultsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
			notification.setLatestEventInfo(context, title, message, contentIntent);
			notification.tickerText= getString(R.string.finisheduploading);
			NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notificationId, notification);
		} catch (Exception e){}
	}

	void saveLastUpload(){
		Editor settings = AndroidCPG.getSharedPreferences().edit();
		settings.remove("lastUploads");
		settings.commit();
		JSONObject uploads = new JSONObject();
		JSONArray successUploadsArray = new JSONArray();
		JSONArray failedUploadsArray = new JSONArray();
		if (mFileUploads.size() > 0){
			for (Uri fileUploadSuccess: mFileUploadsSuccess){
				successUploadsArray.put(Utils.getPathFromUri(fileUploadSuccess));
			}
			for (Uri fileUploadFailed: mFileUploadsFailed){
				failedUploadsArray.put(Utils.getPathFromUri(fileUploadFailed));
			}
		} else {
			if (mRemoteVideoUploadResult == 1){
				successUploadsArray.put(mRemoteVideoUploadName);
			} else {
				failedUploadsArray.put(mRemoteVideoUploadName);
			}
		}

		try {
			uploads.put("successUploadsArray", successUploadsArray);
			uploads.put("failedUploadsArray", failedUploadsArray);
			uploads.put("time", System.currentTimeMillis());
		} catch (Exception e) {
			return;
		}

		settings.putString("lastUploads", uploads.toString());
		settings.commit();
	}
}
