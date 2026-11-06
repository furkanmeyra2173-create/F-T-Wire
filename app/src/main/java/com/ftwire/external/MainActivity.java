package com.ftwire.external;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ = 1001;
    private TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())), REQ);
                return;
            }
            startService(new Intent(this, OverlayService.class));
            status.setText("Overlay açık");
            Toast.makeText(this, "F-T Wire çalışıyor", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnStop).setOnClickListener(v -> {
            stopService(new Intent(this, OverlayService.class));
            status.setText("Overlay kapalı");
        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ && Settings.canDrawOverlays(this)) {
            startService(new Intent(this, OverlayService.class));
            status.setText("Overlay açık");
        }
    }
}