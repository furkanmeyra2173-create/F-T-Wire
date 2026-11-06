package com.ftwire.external;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class FloatingMenu extends View {
    private final Paint box = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FloatingMenu(Context c) {
        super(c);
        setBackgroundColor(Color.TRANSPARENT);
        box.setStyle(Paint.Style.STROKE);
        box.setStrokeWidth(2.8f);
        bar.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        float[] d = NativeBridge.getDrawData();
        if (d == null || d.length < 1) return;
        int n = (int) d[0];
        int i = 1;
        for (int k = 0; k < n && i + 5 < d.length; k++) {
            float x1 = d[i++], y1 = d[i++], x2 = d[i++], y2 = d[i++];
            int col = (int) d[i++];
            float hp = d[i++];
            box.setColor(col);
            c.drawRect(x1, y1, x2, y2, box);
            float h = y2 - y1;
            float bh = h * Math.max(0f, Math.min(1f, hp / 200f));
            bar.setColor(0xFF00FF00);
            c.drawRect(x1 - 7, y2 - bh, x1 - 3, y2, bar);
            box.setColor(0xFFFFFFFF);
            c.drawRect(x1 - 7, y1, x1 - 3, y2, box);
        }
    }
}