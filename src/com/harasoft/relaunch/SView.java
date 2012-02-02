package com.harasoft.relaunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

public class SView extends View {
	final String TAG = "SView";
	int lpad;
	int scrollW;

	SharedPreferences prefs;
	ReLaunchApp app;
	Paint busyPaint;
	Paint freePaint;
	Paint outlinePaint;

	public int total;
	public int first;
	public int count;

	public SView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		commonConstructor(context);
	}

	public SView(Context context, AttributeSet attrs) {
		super(context, attrs);
		commonConstructor(context);
	}

	public SView(Context context) {
		super(context);
		commonConstructor(context);
	}

	private void commonConstructor(Context context) {
		app = (ReLaunchApp) context.getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		busyPaint = new Paint();
		busyPaint.setColor(getResources().getColor(R.color.scrollbar_busy));
		freePaint = new Paint();
		freePaint.setColor(getResources().getColor(R.color.scrollbar_free));
		outlinePaint = new Paint();
		outlinePaint.setColor(getResources()
				.getColor(R.color.scrollbar_outline));
		try {
			lpad = Integer.parseInt(prefs.getString("scrollPad", "10"));
			scrollW = Integer.parseInt(prefs.getString("scrollWidth", "25"));
		} catch (NumberFormatException e) {
			lpad = 10;
			scrollW = 25;
		}
		if (lpad >= scrollW)
			lpad = 2;
		if (lpad < 0)
			lpad = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float h = getMeasuredHeight() - 1;
		float w = getMeasuredWidth() - 1;
		float clpad = w * lpad / scrollW;
		if (total == 0) // for phones with smooth scroll? may be make choice
			canvas.drawRect(clpad, 0, w, h, freePaint);
		else {
			float curr = 0;
			if (first > 0) {
				float n = (h * first) / total;
				canvas.drawRect(clpad, curr, w, h, freePaint);
				curr += n;
			}
			if (count > 0) {
				float n = (h * count) / total;
				canvas.drawRect(clpad, curr, w, h, busyPaint);
				curr += n;
			}
			if ((first + count) < total)
				canvas.drawRect(clpad, curr, w, h, freePaint);
		}
		// may be here fully go away (make choice) ?
		canvas.drawLine(clpad, 0, w, 0, outlinePaint);
		canvas.drawLine(w, 0, w, h, outlinePaint);
		canvas.drawLine(w, h, clpad, h, outlinePaint);
		canvas.drawLine(clpad, h, clpad, 0, outlinePaint);
	}
}