#include <jni.h>
#include <vector>
#include "ESP.hpp"
static Eng g;
static std::vector<float> gd;

extern "C" {
JNIEXPORT void JNICALL Java_com_ftwire_external_NativeBridge_init(JNIEnv*,jclass){ g.Init(); }
JNIEXPORT void JNICALL Java_com_ftwire_external_NativeBridge_update(JNIEnv*,jclass){
    g.Upd(); gd=g.Draw();
}
JNIEXPORT void JNICALL Java_com_ftwire_external_NativeBridge_shutdown(JNIEnv*,jclass){ g.ok=false; }
JNIEXPORT jfloatArray JNICALL Java_com_ftwire_external_NativeBridge_getDrawData(JNIEnv*e,jclass){
    if(gd.empty())return nullptr;
    jfloatArray a=e->NewFloatArray(gd.size());
    e->SetFloatArrayRegion(a,0,gd.size(),gd.data()); return a;
}
JNIEXPORT void JNICALL Java_com_ftwire_external_NativeBridge_setConfig(JNIEnv*,jclass,
    jboolean box,jboolean health,jboolean silent,jboolean norecoil,jfloat fov,jfloat maxD,jboolean maxOff){
    g.cfg.box=box; g.cfg.health=health; g.cfg.silent=silent; g.cfg.norecoil=norecoil;
    g.cfg.fov=fov; g.cfg.maxD=maxD; g.cfg.useMax=maxOff;
}
}