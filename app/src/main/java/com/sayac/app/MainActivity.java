package com.sayac.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView tv;
    Handler h = new Handler(Looper.getMainLooper());
    Runnable r = new Runnable() {
        public void run() { show(); h.postDelayed(this, 1000); }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(48, 120, 48, 48);
        tv = new TextView(this);
        tv.setTextSize(26);
        l.addView(tv);
        Button a = new Button(this);
        a.setText("Erişilebilirlik ayarını aç");
        a.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        l.addView(a);
        Button c = new Button(this);
        c.setText("Sıfırla");
        c.setOnClickListener(v -> {
            getSharedPreferences("s", MODE_PRIVATE).edit().clear().apply();
            show();
        });
        l.addView(c);
        setContentView(l);
    }

    void show() {
        SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        tv.setText("Instagram: " + sp.getInt("ig", 0) + "\n\nTikTok: " + sp.getInt("tt", 0)
                + "\n\nYouTube: " + sp.getInt("yt", 0) + "\n");
    }

    @Override protected void onResume() { super.onResume(); h.post(r); }
    @Override protected void onPause() { super.onPause(); h.removeCallbacks(r); }
}
