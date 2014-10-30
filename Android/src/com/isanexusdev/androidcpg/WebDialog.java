package com.isanexusdev.androidcpg;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WebDialog extends Dialog {

	static final FrameLayout.LayoutParams LAYOUT_FILL = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT);

	private String mContent;
	private int mMode = 0;
	TextView mTextView;
	Button mModeButton;
	Button mCopyButton;

	public WebDialog(final Context context, final String content) {
		super(context, android.R.style.Theme_NoTitleBar);
		mContent = content;
	}

	@Override
	public void onBackPressed(){
		dismiss();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View mFrameLayout = LayoutInflater.from(getContext()).inflate(R.layout.webdialog, null); 
		addContentView(mFrameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mTextView = (TextView)mFrameLayout.findViewById(R.id.webview_content);
		ImageView closeImage = (ImageView)mFrameLayout.findViewById(R.id.webview_close);
		closeImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mModeButton = (Button)mFrameLayout.findViewById(R.id.webview_mode_button);
		mModeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMode == 0){
					mMode = 1;
					mTextView.setText(mContent);
					mModeButton.setText(R.string.webview_mode_button_html);
				} else {
					mMode = 0;
					mTextView.setText(Html.fromHtml(mContent));
					mModeButton.setText(R.string.webview_mode_button_code);
				}
			}
		});
		
		mCopyButton = (Button)mFrameLayout.findViewById(R.id.webview_copy_button);
		mCopyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				    final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				    final android.content.ClipData clipData = android.content.ClipData.newPlainText("AndroidCPG Login Error", mContent);
				    clipboardManager.setPrimaryClip(clipData);
				} else {
				    final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				    clipboardManager.setText(mContent);
				};
			}
		});
		
		if (!mContent.trim().startsWith("<")){
			mModeButton.setVisibility(View.GONE);
			mTextView.setText(mContent);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mCopyButton.setLayoutParams(lp);
		} else {
			mModeButton.setVisibility(View.VISIBLE);
			mTextView.setText(Html.fromHtml(mContent));
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.LEFT_OF, R.id.webview_mode_button);
			mCopyButton.setLayoutParams(lp);
		}
	}
}