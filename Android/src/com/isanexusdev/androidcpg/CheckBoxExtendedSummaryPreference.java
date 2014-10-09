package com.isanexusdev.androidcpg;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CheckBoxExtendedSummaryPreference extends CheckBoxPreference{
	private static final String TAG = CheckBoxExtendedSummaryPreference.class.getName();
	public CheckBoxExtendedSummaryPreference(Context context) {
		super(context);
	}

	public CheckBoxExtendedSummaryPreference(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckBoxExtendedSummaryPreference(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setSummary(final CharSequence s){
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				CheckBoxExtendedSummaryPreference.super.setSummary(s);
			}
		});
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View mViewForPreference = super.onCreateView(parent);

		TextView textView = (TextView) mViewForPreference.findViewById(android.R.id.summary);
		if (textView != null) {
			textView.setSingleLine(false);
			textView.setMaxLines(30);
		}

		return mViewForPreference;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		TextView summaryView = (TextView) view.findViewById(android.R.id.summary); 
		if (summaryView != null) {
			try {
				XmlResourceParser parser = getContext().getResources().getXml(R.color.summarycolors);
				ColorStateList colors = ColorStateList.createFromXml(getContext().getResources(), parser);
				summaryView.setTextColor(colors);
			} catch (Exception e) {
				// handle exceptions
			}
		}
	}
}
