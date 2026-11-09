package com.ftwire.external;

public class NativeBridge {
    static { System.loadLibrary("ftwire"); }

    public static native void   init();
    public static native void   update();
    public static native float[] getDrawData();
    public static native void   shutdown();

    // maps to Java_com_ftwire_external_NativeBridge_setConfig in libftwire.so
    public static native void setConfig(
        boolean espBox,
        boolean espHealth,
        boolean noRecoil,
        boolean silentAim
    );
}