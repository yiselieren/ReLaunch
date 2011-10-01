package com.harasoft.relaunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SView extends View {
    final String                  TAG = "SView";
    int                           lpad;

    SharedPreferences             prefs;
    ReLaunchApp                   app;
    Paint                         busyPaint;
    Paint                         freePaint;
    
    public int                    total;
    public int                    first;
    public int                    count;

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

    private void commonConstructor(Context context)
    {
        app = (ReLaunchApp)context.getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);  
        busyPaint = new Paint();
        busyPaint.setColor(getResources().getColor(R.color.scrollbar_busy));
        freePaint = new Paint();
        freePaint.setColor(getResources().getColor(R.color.scrollbar_free));
        int scrollW;
        try {
            lpad = Integer.parseInt(prefs.getString("scrollPad", "10"));
            scrollW = Integer.parseInt(prefs.getString("scrollWidth", "25"));
        } catch(NumberFormatException e) {
            lpad = 10;
            scrollW = 25;
        }
        if (lpad >= scrollW)
            lpad = scrollW-2;
        if (lpad < 0)
            lpad = 0;
        

    }
    @Override
    protected void onDraw(Canvas canvas) {
        int h = getHeight();
        int w = getWidth() - lpad;
        //Log.d(TAG, "SV --  first:" + first + " count:" + count + " total:" + total + " (" + w + " x " + h + ")");
        if (total == 0)
            canvas.drawRect(lpad, 0, w, h, busyPaint);
        else
        {
            int curr = 0;
            if (first > 0)
            {
                int n = (h * first) / total;
                canvas.drawRect(lpad, curr, w, h, freePaint);
                curr += n;
            }
            if (count > 0)
            {
                int n = (h * count) / total;
                canvas.drawRect(lpad, curr, w, h, busyPaint);
                curr += n;
           }
            if ((first + count) < (total - 1))
                canvas.drawRect(lpad, curr, w, h, freePaint);
        }
    }
}