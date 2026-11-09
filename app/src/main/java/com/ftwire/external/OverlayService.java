package com.ftwire.external;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.WindowManager;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private FloatingMenu floatingMenu;
    private Handler handler;
    private static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "ftwire";

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingMenu = new FloatingMenu(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // FIX: FLAG_NOT_FOCUSABLE keeps touches passing through,
            //      FLAG_LAYOUT_IN_SCREEN + FLAG_LAYOUT_NO_LIMITS anchors
            //      coordinate space to raw screen origin (top-left = 0,0),
            //      eliminating the status-bar Y offset bug.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        );
        lp.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        lp.x = 0;
        lp.y = 0;

        windowManager.addView(floatingMenu, lp);

        NativeBridge.init();
        handler = new Handler(Looper.getMainLooper());
        scheduleTick();
    }

    private void scheduleTick() {
        handler.postDelayed(tick, 16); // ~60fps
    }

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (floatingMenu != null) {
                floatingMenu.update(NativeBridge.getDrawData());
                floatingMenu.invalidate();
            }
            scheduleTick();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NativeBridge.shutdown();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (floatingMenu != null && windowManager != null) {
            windowManager.removeView(floatingMenu);
        }
    }

    private void startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "F-T Wire", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
        Notification notif = new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("F-T Wire")
            .setContentText("External active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build();
        startForeground(NOTIF_ID, notif);
    }
}