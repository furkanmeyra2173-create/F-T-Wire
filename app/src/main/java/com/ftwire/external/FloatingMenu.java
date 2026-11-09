package com.ftwire.external;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class FloatingMenu extends View {

    // ---- layout constants (pixels, same as original bytecode) ----
    private static final float CLOSED_W  = 120f;
    private static final float CLOSED_H  = 52f;
    private static final float MENU_W    = 420f;
    private static final float MENU_H    = 340f;
    private static final float TITLE_H   = 40f;
    private static final float BTN_H     = 44f;
    private static final float PAD       = 10f;

    // ---- state ----
    private float menuX = 40f, menuY = 100f;   // raw screen coords
    private boolean menuOpen = false;
    private boolean dragging = false;
    private float touchX, touchY;
    private float localY; // drag offset inside header

    // ---- toggles ----
    private boolean espBox    = false;
    private boolean espHealth = false;
    private boolean norecoil  = false;
    private boolean silent    = false;

    // ---- draw data from native (packed float array) ----
    private float[] drawData = null;

    // ---- paints ----
    private final Paint menuBg    = makePaint(0xDD1A1A2E, Paint.Style.FILL);
    private final Paint boxPaint  = makePaint(0xFF00FF88, Paint.Style.STROKE, 2f);
    private final Paint barPaint  = makePaint(0xFF00FF00, Paint.Style.FILL);
    private final Paint btnPaint  = makePaint(0xFF2A2A4E, Paint.Style.FILL);
    private final Paint shadow    = makePaint(0x44000000, Paint.Style.FILL);
    private final Paint menuText;
    private final Paint btnText;

    private final WindowManager wm;

    public FloatingMenu(Context ctx) {
        super(ctx);
        wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        menuText = new Paint(Paint.ANTI_ALIAS_FLAG);
        menuText.setColor(0xFFFFFFFF);
        menuText.setTextSize(28f);
        menuText.setFakeBoldText(true);

        btnText = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnText.setColor(0xFFCCCCCC);
        btnText.setTextSize(24f);
    }

    /** Called from OverlayService tick with native draw payload */
    public void update(float[] data) {
        this.drawData = data;
    }

    // ------------------------------------------------------------------
    //  Touch
    // ------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float rx = ev.getRawX();
        float ry = ev.getRawY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = rx;
                touchY = ry;
                localY = ry - menuY;

                int hit = hitMenu(rx, ry);
                if (hit == 0) {           // toggle button (closed/title bar)
                    menuOpen = !menuOpen;
                } else if (hit > 0) {     // feature button
                    toggleFeature(hit);
                } else {                  // miss → start drag
                    dragging = true;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    menuX += rx - touchX;
                    menuY += ry - touchY;
                    touchX = rx;
                    touchY = ry;
                    clampPosition();
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * Returns:
     *  -1  → no hit (drag area)
     *   0  → toggle open/close
     *   1  → ESP Box
     *   2  → ESP Health
     *   3  → No Recoil
     *   4  → Silent Aim
     *
     * Coordinates are raw screen pixels.
     * With FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_NO_LIMITS on the window,
     * rawX/rawY == view-local X/Y (window is at 0,0 in screen space).
     */
    private int hitMenu(float rx, float ry) {
        // view-local coords (window pinned to 0,0 raw screen)
        float lx = rx - menuX;
        float ly = ry - menuY;

        if (!menuOpen) {
            // hit the closed pill
            if (lx >= 0 && lx <= CLOSED_W && ly >= 0 && ly <= CLOSED_H) return 0;
            return -1;
        }

        // title bar → toggle close
        if (lx >= 0 && lx <= MENU_W && ly >= 0 && ly <= TITLE_H) return 0;

        // feature buttons
        float[] btnTops = btnTops();
        int[] ids = {1, 2, 3, 4};
        for (int i = 0; i < btnTops.length; i++) {
            float top = btnTops[i];
            if (lx >= PAD && lx <= MENU_W - PAD && ly >= top && ly <= top + BTN_H)
                return ids[i];
        }
        return -1;
    }

    private float[] btnTops() {
        float y = TITLE_H + PAD;
        float step = BTN_H + PAD;
        return new float[]{y, y + step, y + 2*step, y + 3*step};
    }

    private void toggleFeature(int id) {
        switch (id) {
            case 1: espBox    = !espBox;    break;
            case 2: espHealth = !espHealth; break;
            case 3: norecoil  = !norecoil;  break;
            case 4: silent    = !silent;    break;
        }
        NativeBridge.setConfig(espBox, espHealth, norecoil, silent);
    }

    private void clampPosition() {
        int sw = getResources().getDisplayMetrics().widthPixels;
        int sh = getResources().getDisplayMetrics().heightPixels;
        float w = menuOpen ? MENU_W : CLOSED_W;
        float h = menuOpen ? MENU_H : CLOSED_H;
        menuX = Math.max(0, Math.min(menuX, sw - w));
        menuY = Math.max(0, Math.min(menuY, sh - h));
    }

    // ------------------------------------------------------------------
    //  Draw
    // ------------------------------------------------------------------
    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        // ESP world-space boxes/health from native
        drawESP(c);

        if (!menuOpen) {
            drawClosedButton(c);
        } else {
            drawOpenMenu(c);
        }
    }

    private void drawClosedButton(Canvas c) {
        RectF r = new RectF(menuX, menuY, menuX + CLOSED_W, menuY + CLOSED_H);
        c.drawRoundRect(r, 16f, 16f, shadow);
        c.drawRoundRect(r, 16f, 16f, menuBg);
        menuText.setTextAlign(Paint.Align.CENTER);
        c.drawText("F-T", menuX + CLOSED_W / 2f, menuY + CLOSED_H * 0.68f, menuText);
    }

    private void drawOpenMenu(Canvas c) {
        RectF r = new RectF(menuX, menuY, menuX + MENU_W, menuY + MENU_H);
        c.drawRoundRect(new RectF(menuX+4, menuY+4, menuX+MENU_W+4, menuY+MENU_H+4), 18f, 18f, shadow);
        c.drawRoundRect(r, 18f, 18f, menuBg);

        // title
        menuText.setTextAlign(Paint.Align.LEFT);
        menuText.setColor(0xFF00FF88);
        c.drawText("F-T Wire", menuX + PAD, menuY + TITLE_H - 8f, menuText);
        menuText.setColor(0xFFFFFFFF);

        // buttons
        String[] labels = {"ESP Box", "ESP Health", "No Recoil", "Silent Aim"};
        boolean[] states = {espBox, espHealth, norecoil, silent};
        float[] tops = btnTops();
        for (int i = 0; i < 4; i++) {
            drawBtn(c, menuX + PAD, menuY + tops[i],
                    MENU_W - 2*PAD, BTN_H, labels[i], states[i]);
        }
    }

    private void drawBtn(Canvas c, float x, float y, float w, float h,
                         String label, boolean on) {
        RectF r = new RectF(x, y, x + w, y + h);
        btnPaint.setColor(on ? 0xFF1E3A2F : 0xFF2A2A4E);
        c.drawRoundRect(r, 10f, 10f, btnPaint);
        btnText.setColor(on ? 0xFF00FF88 : 0xFF888888);
        btnText.setTextAlign(Paint.Align.LEFT);
        c.drawText(label, x + 12f, y + h * 0.68f, btnText);
        btnText.setColor(on ? 0xFF00FF44 : 0xFF555555);
        btnText.setTextAlign(Paint.Align.RIGHT);
        c.drawText(on ? "ON" : "OFF", x + w - 12f, y + h * 0.68f, btnText);
    }

    private void drawESP(Canvas c) {
        if (drawData == null || drawData.length == 0) return;
        // format: [count, x1,y1,w1,h1,hp1,  x2,y2,w2,h2,hp2, ...]
        int count = (int) drawData[0];
        int idx = 1;
        for (int i = 0; i < count && idx + 5 <= drawData.length; i++, idx += 5) {
            float ex = drawData[idx],   ey = drawData[idx+1];
            float ew = drawData[idx+2], eh = drawData[idx+3];
            float hp = drawData[idx+4]; // 0.0–1.0

            if (espBox) {
                RectF br = new RectF(ex, ey, ex+ew, ey+eh);
                c.drawRect(br, boxPaint);
            }
            if (espHealth) {
                float barW = 4f;
                float filled = eh * hp;
                barPaint.setColor(hpColor(hp));
                c.drawRect(ex - barW - 2f, ey + (eh - filled),
                           ex - 2f,        ey + eh, barPaint);
            }
        }
    }

    private static int hpColor(float hp) {
        if (hp > 0.6f) return 0xFF00FF00;
        if (hp > 0.3f) return 0xFFFFFF00;
        return 0xFFFF3300;
    }

    // ------------------------------------------------------------------
    //  Helpers
    // ------------------------------------------------------------------
    private static Paint makePaint(int color, Paint.Style style) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(style);
        return p;
    }

    private static Paint makePaint(int color, Paint.Style style, float sw) {
        Paint p = makePaint(color, style);
        p.setStrokeWidth(sw);
        return p;
    }
}