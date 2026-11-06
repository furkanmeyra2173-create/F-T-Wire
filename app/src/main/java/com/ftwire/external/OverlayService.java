package com.ftwire.external;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

public class OverlayService extends Service {

    private WindowManager wm;
    private FloatingMenu menu;
    private final Handler h = new Handler(Looper.getMainLooper());
    private boolean run;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, notif());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        menu = new FloatingMenu(this);

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= 26 ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        p.gravity = Gravity.TOP | Gravity.START;
        wm.addView(menu, p);

        run = true;
        NativeBridge.init();
        // varsayılan config
        NativeBridge.setConfig(true, true, false, true, 140f, 250f, false);
        tick();
    }

    private void tick() {
        if (!run) return;
        NativeBridge.update();
        menu.invalidate();
        h.postDelayed(this::tick, 16);
    }

    private Notification notif() {
        String id = "ftwire";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(
                    id, "F-T Wire", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(c);
        }
        return new Notification.Builder(this, id)
                .setContentTitle("F-T Wire")
                .setContentText("External active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();
    }

    @Override
    public void onDestroy() {
        run = false;
        h.removeCallbacksAndMessages(null);
        if (menu != null && wm != null) {
            try { wm.removeView(menu); } catch (Exception ignored) {}
        }
        NativeBridge.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}