package com.ftwire.external;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class FloatingMenu extends View {

    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint btnText = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float menuX = 50f, menuY = 200f;
    private float touchX, touchY;
    private boolean dragging = false;
    private boolean menuOpen = true;

    public boolean espBox = true;
    public boolean espHealth = true;
    public boolean silent = false;
    public boolean norecoil = true;
    public boolean useMax = false;

    private final float menuW = 340f;
    private final float menuH = 420f;
    private final float btnH = 52f;
    private final float pad = 16f;
    private final float titleH = 50f;

    public FloatingMenu(Context c) {
        super(c);
        setBackgroundColor(Color.TRANSPARENT);

        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(2.8f);
        barPaint.setStyle(Paint.Style.FILL);

        menuBg.setColor(0xF0111111);
        menuBg.setStyle(Paint.Style.FILL);

        menuText.setColor(0xFF00FF88);
        menuText.setTextSize(32f);
        menuText.setFakeBoldText(true);

        btnPaint.setStyle(Paint.Style.FILL);
        btnText.setColor(Color.WHITE);
        btnText.setTextSize(26f);
        btnText.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas c) {
        // ESP
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
                    c.drawRect(x1 - 8, y2 - bh, x1 - 3, y2, barPaint);
                    boxPaint.setColor(0xFFFFFFFF);
                    c.drawRect(x1 - 8, y1, x1 - 3, y2, boxPaint);
                }
            }
        }

        if (!menuOpen) {
            // küçük aç butonu
            btnPaint.setColor(0xFF00AA55);
            c.drawRoundRect(new RectF(menuX, menuY, menuX + 110, menuY + 48), 14, 14, btnPaint);
            btnText.setTextSize(24f);
            c.drawText("MENU", menuX + 55, menuY + 32, btnText);
            return;
        }

        // menü arka plan
        c.drawRoundRect(new RectF(menuX, menuY, menuX + menuW, menuY + menuH), 18, 18, menuBg);

        // başlık
        c.drawText("F-T Wire", menuX + pad, menuY + 38, menuText);

        float y = menuY + titleH + 8;
        drawBtn(c, "ESP Box", espBox, y); y += btnH + 10;
        drawBtn(c, "ESP Health", espHealth, y); y += btnH + 10;
        drawBtn(c, "Silent Aim", silent, y); y += btnH + 10;
        drawBtn(c, "No Recoil", norecoil, y); y += btnH + 10;
        drawBtn(c, "Max Offsets", useMax, y); y += btnH + 14;

        // kapat butonu
        btnPaint.setColor(0xFFCC2200);
        c.drawRoundRect(new RectF(menuX + pad, y, menuX + menuW - pad, y + btnH), 12, 12, btnPaint);
        btnText.setTextSize(26f);
        c.drawText("Kapat", menuX + menuW / 2f, y + 34, btnText);
    }

    private void drawBtn(Canvas c, String label, boolean on, float y) {
        btnPaint.setColor(on ? 0xFF00AA55 : 0xFF333333);
        c.drawRoundRect(new RectF(menuX + pad, y, menuX + menuW - pad, y + btnH), 12, 12, btnPaint);
        btnText.setTextSize(26f);
        c.drawText(label + (on ? "  ON" : "  OFF"), menuX + menuW / 2f, y + 34, btnText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getRawX();
        float y = e.getRawY();

        // menü dışında dokunuş → oyuna bırak
        if (!hitMenu(x, y)) {
            return false;
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = x;
                touchY = y;
                dragging = true;

                if (!menuOpen) {
                    menuOpen = true;
                    invalidate();
                    return true;
                }

                // buton tıklama hesapla
                float localY = y - menuY;
                float start = titleH + 8;

                if (localY >= start && localY < start + btnH) {
                    espBox = !espBox; apply();
                } else if (localY >= start + (btnH + 10) && localY < start + 2 * (btnH + 10)) {
                    espHealth = !espHealth; apply();
                } else if (localY >= start + 2 * (btnH + 10) && localY < start + 3 * (btnH + 10)) {
                    silent = !silent; apply();
                } else if (localY >= start + 3 * (btnH + 10) && localY < start + 4 * (btnH + 10)) {
                    norecoil = !norecoil; apply();
                } else if (localY >= start + 4 * (btnH + 10) && localY < start + 5 * (btnH + 10)) {
                    useMax = !useMax; apply();
                } else if (localY >= start + 5 * (btnH + 10) + 4) {
                    menuOpen = false;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    menuX += x - touchX;
                    menuY += y - touchY;
                    touchX = x;
                    touchY = y;
                    // ekran dışına kaçmasın
                    menuX = Math.max(0, Math.min(menuX, getWidth() - 80));
                    menuY = Math.max(0, Math.min(menuY, getHeight() - 80));
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
        }
        return true;
    }

    private boolean hitMenu(float x, float y) {
        if (!menuOpen) {
            return x >= menuX && x <= menuX + 110 && y >= menuY && y <= menuY + 48;
        }
        return x >= menuX && x <= menuX + menuW && y >= menuY && y <= menuY + menuH;
    }

    private void apply() {
        NativeBridge.setConfig(espBox, espHealth, silent, norecoil, 140f, 250f, useMax);
    }
}