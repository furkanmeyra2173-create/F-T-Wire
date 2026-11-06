#pragma once
#include <cmath>
struct V3{ float x=0,y=0,z=0;
    float len()const{return sqrtf(x*x+y*y+z*z);}
    V3 operator-(const V3&o)const{return{x-o.x,y-o.y,z-o.z};}
    V3 norm()const{float l=len();return l<1e-5f?V3{}:V3{x/l,y/l,z/l};}
};
struct M4{ float m[16]{};
    bool w2s(const V3&w,float*ox,float*oy,float sw,float sh)const{
        float ww=m[3]*w.x+m[7]*w.y+m[11]*w.z+m[15];
        if(ww<0.001f)return false;
        float x=m[0]*w.x+m[4]*w.y+m[8]*w.z+m[12];
        float y=m[1]*w.x+m[5]*w.y+m[9]*w.z+m[13];
        *ox=sw*.5f+(x/ww)*sw*.5f; *oy=sh*.5f-(y/ww)*sh*.5f; return true;
    }
};
struct Ent{ uintptr_t a=0; V3 head,foot; float hp=0,dist=0; bool dead=false,bot=false; };