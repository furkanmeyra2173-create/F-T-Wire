package com.ftwire.external;

public class NativeBridge {
    static { System.loadLibrary("ftwire"); }
    public static native void init();
    public static native void update();
    public static native void shutdown();
    public static native float[] getDrawData();
    public static native void setConfig(boolean box, boolean health, boolean silent,
                                        boolean norecoil, float fov, float maxDist, boolean maxOff);
}