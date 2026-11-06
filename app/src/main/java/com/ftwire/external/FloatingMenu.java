package com.ftwire.external;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class FloatingMenu extends View {

    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint btnText = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float menuX = 40f, menuY = 180f;
    private float touchX, touchY;
    private boolean dragging = false;
    private boolean menuOpen = true;

    // toggle states
    public boolean espBox = true;
    public boolean espHealth = true;
    public boolean silent = false;
    public boolean norecoil = true;
    public boolean useMax = false;

    private final float menuW = 280f;
    private final float menuH = 320f;
    private final float btnH = 42f;
    private final float pad = 12f;

    public FloatingMenu(Context c) {
        super(c);
        setBackgroundColor(Color.TRANSPARENT);

        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(2.8f);

        barPaint.setStyle(Paint.Style.FILL);

        menuBg.setColor(0xEE111111);
        menuBg.setStyle(Paint.Style.FILL);

        menuText.setColor(0xFF00FF88);
        menuText.setTextSize(28f);
        menuText.setFakeBoldText(true);

        btnPaint.setStyle(Paint.Style.FILL);
        btnText.setColor(Color.WHITE);
        btnText.setTextSize(24f);
        btnText.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas c) {
        // ===== ESP çizimi =====
        float[] d = NativeBridge.getDrawData();
        if (d != null && d.length > 0) {
            int n = (int) d[0];
            int i = 1;
            for (int k = 0; k < n && i + 5 < d.length; k++) {
                float x1 = d[i++], y1 = d[i++], x2 = d[i++], y2 = d[i++];
                int col = (int) d[i++];
                float hp = d[i++];
                if (espBox) {
                    boxPaint.setColor(col);
                    c.drawRect(x1, y1, x2, y2, boxPaint);
                }
                if (espHealth) {
                    float h = y2 - y1;
                    float bh = h * Math.max(0f, Math.min(1f, hp / 200f));
                    barPaint.setColor(0xFF00FF00);
                    c.drawRect(x1 - 7, y2 - bh, x1 - 3, y2, barPaint);
                    boxPaint.setColor(0xFFFFFFFF);
                    c.drawRect(x1 - 7, y1, x1 - 3, y2, boxPaint);
                }
            }
        }

        // ===== Kayan menü =====
        if (!menuOpen) {
            // sadece küçük top butonu
            btnPaint.setColor(0xFF00AA55);
            c.drawRoundRect(new RectF(menuX, menuY, menuX + 90, menuY + 40), 12, 12, btnPaint);
            btnText.setTextSize(22f);
            c.drawText("MENU", menuX + 45, menuY + 28, btnText);
            return;
        }

        // arka plan
        c.drawRoundRect(new RectF(menuX, menuY, menuX + menuW, menuY + menuH), 16, 16, menuBg);

        // başlık
        c.drawText("F-T Wire", menuX + pad, menuY + 36, menuText);

        // butonlar
        float y = menuY + 55;
        drawBtn(c, "ESP Box", espBox, y); y += btnH + 8;
        drawBtn(c, "ESP Health", espHealth, y); y += btnH + 8;
        drawBtn(c, "Silent Aim", silent, y); y += btnH + 8;
        drawBtn(c, "No Recoil", norecoil, y); y += btnH + 8;
        drawBtn(c, "Max Offsets", useMax, y); y += btnH + 8;

        // kapat
        btnPaint.setColor(0xFFAA2200);
        c.drawRoundRect(new RectF(menuX + pad, y, menuX + menuW - pad, y + btnH), 10, 10, btnPaint);
        btnText.setTextSize(24f);
        c.drawText("Kapat", menuX + menuW / 2, y + 30, btnText);
    }

    private void drawBtn(Canvas c, String label, boolean on, float y) {
        btnPaint.setColor(on ? 0xFF00AA55 : 0xFF444444);
        c.drawRoundRect(new RectF(menuX + pad, y, menuX + menuW - pad, y + btnH), 10, 10, btnPaint);
        btnText.setTextSize(24f);
        c.drawText(label + (on ? "  ON" : "  OFF"), menuX + menuW / 2, y + 30, btnText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getRawX();
        float y = e.getRawY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (hitMenu(x, y)) {
                    touchX = x;
                    touchY = y;
                    dragging = true;

                    if (menuOpen) {
                        float ly = y - menuY;
                        if (ly > 55 && ly < 55 + btnH) { espBox = !espBox; apply(); }
                        else if (ly > 55 + btnH + 8 && ly < 55 + 2 * (btnH + 8)) { espHealth = !espHealth; apply(); }
                        else if (ly > 55 + 2 * (btnH + 8) && ly < 55 + 3 * (btnH + 8)) { silent = !silent; apply(); }
                        else if (ly > 55 + 3 * (btnH + 8) && ly < 55 + 4 * (btnH + 8)) { norecoil = !norecoil; apply(); }
                        else if (ly > 55 + 4 * (btnH + 8) && ly < 55 + 5 * (btnH + 8)) { useMax = !useMax; apply(); }
                        else if (ly > 55 + 5 * (btnH + 8)) { menuOpen = false; }
                    } else {
                        menuOpen = true;
                    }
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    menuX += x - touchX;
                    menuY += y - touchY;
                    touchX = x;
                    touchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                dragging = false;
                break;
        }
        return false;
    }

    private boolean hitMenu(float x, float y) {
        if (!menuOpen) {
            return x >= menuX && x <= menuX + 90 && y >= menuY && y <= menuY + 40;
        }
        return x >= menuX && x <= menuX + menuW && y >= menuY && y <= menuY + menuH;
    }

    private void apply() {
        NativeBridge.setConfig(espBox, espHealth, silent, norecoil, 140f, 250f, useMax);
    }
}