package com.isanexusdev.androidcpg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.webkit.MimeTypeMap;
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
	public List<Uri> mFileUploads = new ArrayList<Uri>();
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

	public void setVideoThumb(String id){
		if (id == null || id.length() == 0){
			return;
		}

		Utils.getYoutubeThumb(id, new GetYoutubeVideoThumbAsyncTask.GetYoutubeVideoThumbListener() {
			@Override
			public void result(final Bitmap thumb) {
				final SendShare sendShareActivity = AndroidCPG.getSendShareActivity();
				if (sendShareActivity != null) {
					sendShareActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sendShareActivity.setThumbUrl(thumb);
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
			Utils.uploadRemoteVideo(mAlbums.get(mSelectedAlbumId)[0], mRemoteVideoUploadName, mRemoteVideoUpload, Utils.extractYoutubeVideoId(mRemoteVideoUpload), new UploadRemoteVideoAsyncTask.UploadRemoteVideoListener() {
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
			final String filePath = Utils.getPathFromUri(currentUri);
			if (sendShareActivity != null) {
				sendShareActivity.mUploadprogress.setVisibility(View.VISIBLE);
				sendShareActivity.mUploadprogress.setProgress(0);
				String extension = MimeTypeMap.getFileExtensionFromUrl(filePath).toLowerCase();
				if (Utils.IMG_EXT.contains(extension)) {
					sendShareActivity.setImage(currentUri);
				} else if (Utils.VID_EXT.contains(extension)) {
					sendShareActivity.setVideo(currentUri);
				} else {
					sendShareActivity.mPreview.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
				}
			}

			Utils.uploadFile(mAlbums.get(mSelectedAlbumId)[0], filePath, new UploadFileAsyncTask.UploadFileListener() {
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
		Intent intent = new Intent(context, SendShare.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		@SuppressWarnings("static-access")

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(title);
		builder.setContentText(text);
		if(ticker.length() > 0)
			builder.setTicker(ticker);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentIntent(contentIntent);
		if (progress > 0) {
			builder.setProgress(100, progress, false);
		}
		builder.setOngoing(service);

		startForeground(notificationId, builder.build());
	}

	private void postFinishedNotification(int notificationId) {
		if (mFileUploads.size() == 0 || mCurrentFileIndex == 0){
			return;
		}
		String text = String.format(getString(R.string.finishednotificationdetails), mFileUploads.size(), mFileUploadsSuccess.size(), mFileUploadsFailed.size());
		Context context = AndroidCPG.getAppContext();
		PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, null, PendingIntent.FLAG_CANCEL_CURRENT);

		@SuppressWarnings("static-access")

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(getString(R.string.finisheduploading));
		builder.setContentText(text);
		builder.setTicker(getString(R.string.finisheduploading));
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentIntent(contentIntent);
		builder.setOngoing(true);

		NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		nm.notify(notificationId, builder.build());
	}
}
