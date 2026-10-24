package com.sayac.app;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;

public class CounterService extends AccessibilityService {
    long last = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_VIEW_SCROLLED || e.getPackageName() == null) return;
        String p = e.getPackageName().toString();
        String k;
        if (p.equals("com.instagram.android")) k = "ig";
        else if (p.equals("com.zhiliaoapp.musically") || p.equals("com.ss.android.ugc.trill")) k = "tt";
        else if (p.equals("com.google.android.youtube")) k = "yt";
        else return;
        long now = System.currentTimeMillis();
        if (now - last < 800) return;
        last = now;
        SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        sp.edit().putInt(k, sp.getInt(k, 0) + 1).apply();
    }

    @Override
    public void onInterrupt() {}
}
