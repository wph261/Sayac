package com.sayac.app;

import android.accessibilityservice.AccessibilityService;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

public class CounterService extends AccessibilityService {
    // 1-10 iyi, 11-25 gozler kizarir, 26-35 gozler kapanir,
    // 36-45 kapali, 46-50 bekler, 51-60 deri gider, 61+ iskelet
    static final int[] T = {10, 25, 35, 45, 50, 60};
    // bu sayidan sonra agiz acilir ve yazi doner
    static final int FINISH = 100;

    static float cl(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    static class HeadView extends View {
        static final String TXT = "Bugünlük bu kadar yeter";
        float red = 0f, open = 1f, skin = 1f, mouth = 0f, phase = 0f, hsize = 56f;
        boolean ring = false;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rf = new RectF();
        Path path = new Path();

        HeadView(Context c) {
            super(c);
        }

        void set(float r, float o, float s, float m, boolean ringOn) {
            red = r;
            open = o;
            skin = s;
            mouth = m;
            ring = ringOn;
            invalidate();
        }

        int mix(int a, int b, float t) {
            return Color.rgb(
                    (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                    (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                    (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
        }

        void fill(int color, float alpha) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setAlpha((int) (255 * cl(alpha)));
        }

        void line(int color, float alpha, float width) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(width);
            p.setColor(color);
            p.setAlpha((int) (255 * cl(alpha)));
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = hsize;
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float r = w * 0.45f;
            int boneC = 0xFFE9E4D4;
            int skinC = 0xFFF0C08A;

            fill(mix(boneC, skinC, skin), 1f);
            rf.set(cx - r * 0.85f, cy - r, cx + r * 0.85f, cy + r);
            c.drawOval(rf, p);
            line(0xFF5A4636, 1f, w * 0.03f);
            c.drawOval(rf, p);

            float ex = r * 0.38f;
            float ey = cy - r * 0.1f;

            if (skin > 0.02f) {
                float rx = r * 0.26f;
                float ry = r * 0.2f * open;
                for (int i = -1; i <= 1; i += 2) {
                    float x = cx + i * ex;
                    if (open > 0.06f) {
                        fill(mix(0xFFFFFFFF, 0xFFE0302A, red), skin);
                        rf.set(x - rx, ey - ry, x + rx, ey + ry);
                        c.drawOval(rf, p);
                        fill(0xFF2B2B2B, skin);
                        c.drawCircle(x, ey, Math.min(r * 0.1f, ry * 0.9f), p);
                    } else {
                        line(0xFF5A4636, skin, w * 0.035f);
                        c.drawLine(x - rx, ey, x + rx, ey, p);
                    }
                }
                float stress = Math.max(red, 1f - open);
                line(0xFF8A3B30, skin, w * 0.04f);
                path.reset();
                float my = cy + r * 0.5f;
                path.moveTo(cx - r * 0.3f, my);
                path.quadTo(cx, my + r * 0.3f * (1f - 1.6f * stress), cx + r * 0.3f, my);
                c.drawPath(path, p);
            }

            float b = 1f - skin;
            if (b > 0.02f) {
                fill(0xFF2B2B2B, b);
                for (int i = -1; i <= 1; i += 2) {
                    float x = cx + i * ex;
                    rf.set(x - r * 0.26f, ey - r * 0.26f, x + r * 0.26f, ey + r * 0.26f);
                    c.drawOval(rf, p);
                }
                path.reset();
                path.moveTo(cx, cy + r * 0.12f);
                path.lineTo(cx - r * 0.1f, cy + r * 0.34f);
                path.lineTo(cx + r * 0.1f, cy + r * 0.34f);
                path.close();
                c.drawPath(path, p);

                float tw = r * 0.38f;
                float th = r * 0.14f;
                float jaw = r * 0.32f * mouth;
                if (mouth > 0.02f) {
                    fill(0xFF1A1A1A, b);
                    rf.set(cx - tw, cy + r * 0.52f + th, cx + tw, cy + r * 0.52f + th + jaw + 1f);
                    c.drawRect(rf, p);
                }
                for (int k = 0; k < 2; k++) {
                    float top = cy + r * 0.52f + (k == 0 ? 0f : th + jaw);
                    rf.set(cx - tw, top, cx + tw, top + th);
                    fill(0xFFF5F2E8, b);
                    c.drawRect(rf, p);
                    line(0xFF2B2B2B, b, w * 0.02f);
                    c.drawRect(rf, p);
                    for (int i = 1; i < 4; i++) {
                        float x = rf.left + rf.width() * i / 4f;
                        c.drawLine(x, rf.top, x, rf.bottom, p);
                    }
                }
            }

            if (ring) {
                float R = w * 0.78f;
                float circ = (float) (2 * Math.PI * R);
                path.reset();
                path.addCircle(cx, cy, R, Path.Direction.CW);
                tp.setTextSize(w * 0.2f);
                tp.setTypeface(Typeface.DEFAULT_BOLD);
                tp.setStrokeJoin(Paint.Join.ROUND);
                float off = circ * (1f - phase);
                for (int pass = 0; pass < 2; pass++) {
                    if (pass == 0) {
                        tp.setStyle(Paint.Style.STROKE);
                        tp.setStrokeWidth(w * 0.08f);
                        tp.setColor(0xFF000000);
                    } else {
                        tp.setStyle(Paint.Style.FILL);
                        tp.setColor(0xFFFFFFFF);
                    }
                    c.drawTextOnPath(TXT, path, off, 0f, tp);
                    c.drawTextOnPath(TXT, path, off - circ, 0f, tp);
                }
            }
        }
    }

    long last = 0;
    WindowManager wm;
    WindowManager.LayoutParams lp;
    LinearLayout box;
    HeadView head;
    TextView num;
    ObjectAnimator anim;
    ValueAnimator orbit;
    boolean shown = false;
    Handler h = new Handler(Looper.getMainLooper());
    Runnable hideRun = new Runnable() {
        public void run() {
            if (anim != null) anim.cancel();
            if (orbit != null) orbit.cancel();
            if (shown) {
                try { wm.removeView(box); } catch (Exception e) {}
                shown = false;
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

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
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
        SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        int n = sp.getInt("n", 0);
        if (sp.getInt("day", 0) != today) n = 0;
        n++;
        sp.edit().putInt("day", today).putInt("n", n).putInt(k, sp.getInt(k, 0) + 1).apply();
        try { showHead(n); } catch (Exception ex) {}
    }

    void showHead(int n) {
        if (wm == null) wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        boolean fin = n >= FINISH;
        int hs = dp(56);
        int size = fin ? dp(112) : hs;
        if (box == null) {
            box = new LinearLayout(this);
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setGravity(Gravity.CENTER_VERTICAL);
            head = new HeadView(this);
            head.hsize = hs;
            box.addView(head, new LinearLayout.LayoutParams(size, size));
            num = new TextView(this);
            num.setTextSize(15);
            num.setTypeface(null, Typeface.BOLD);
            num.setPadding(dp(8), dp(2), dp(8), dp(2));
            LinearLayout.LayoutParams nl = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            nl.leftMargin = dp(4);
            box.addView(num, nl);
        }
        head.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        float red = cl((n - T[0]) / (float) (T[1] - T[0]));
        float open = 1f - cl((n - T[1]) / (float) (T[2] - T[1]));
        float skin = 1f - cl((n - T[4]) / (float) (T[5] - T[4]));
        head.set(red, open, skin, fin ? 1f : 0f, fin);

        float t = cl(n / (float) T[5]);
        int col = Color.HSVToColor(new float[]{120f * (1f - t), 0.85f, 0.95f});
        num.setText(String.valueOf(n));
        num.setTextColor(col);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xB3000000);
        bg.setCornerRadius(dp(10));
        num.setBackground(bg);

        float amp;
        long dur;
        if (n <= T[0]) { amp = dp(10); dur = 600; }
        else if (n <= T[1]) { amp = dp(8); dur = 850; }
        else if (n <= T[3]) { amp = dp(5); dur = 1300; }
        else { amp = dp(3); dur = 2000; }
        if (anim != null) anim.cancel();
        box.setTranslationY(0f);
        anim = ObjectAnimator.ofFloat(box, "translationY", 0f, -amp);
        anim.setDuration(dur);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.start();

        if (fin) {
            if (orbit == null || !orbit.isRunning()) {
                orbit = ValueAnimator.ofFloat(0f, 1f);
                orbit.setDuration(5000);
                orbit.setRepeatCount(ValueAnimator.INFINITE);
                orbit.setInterpolator(new LinearInterpolator());
                orbit.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator a) {
                        head.phase = (Float) a.getAnimatedValue();
                        head.invalidate();
                    }
                });
                orbit.start();
            }
        } else if (orbit != null) {
            orbit.cancel();
        }

        if (lp == null) {
            lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        }
        lp.y = fin ? dp(12) : dp(40);
        if (!shown) {
            wm.addView(box, lp);
            shown = true;
        } else {
            wm.updateViewLayout(box, lp);
        }
        h.removeCallbacks(hideRun);
        h.postDelayed(hideRun, fin ? 5000 : 3500);
    }

    @Override
    public void onInterrupt() {}
                                        }
