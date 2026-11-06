#pragma once
#include "Memory.hpp"
#include "Offsets.hpp"
#include "Entity.hpp"
#include <vector>
#include <mutex>
#include <atomic>

struct Cfg{ bool box=true,health=true,silent=false,norecoil=true,useMax=false;
            float fov=140.f,maxD=250.f; };

struct Eng {
    Mem mem; Cfg cfg; std::mutex mx; std::vector<Ent> es;
    uintptr_t loc=0; V3 lp{}; M4 vm{}; float sw=1080,sh=2400;
    std::atomic<bool> ok{false};

    bool Init(){
        if(!mem.Attach("com.dts.freefireth") && !mem.Attach("com.dts.freefiremax")) return false;
        mem.base=mem.Mod("libil2cpp.so"); ok=mem.base!=0; return ok;
    }
    uintptr_t Fac(){ return mem.r<uintptr_t>(mem.base+(cfg.useMax?OffMax::GF:Off::GF)); }
    uintptr_t Mat(){
        auto f=Fac(); if(!f)return 0;
        auto p2=mem.r<uintptr_t>(f+(cfg.useMax?OffMax::GF2:Off::GF2)); if(!p2)return 0;
        return mem.r<uintptr_t>(p2+(cfg.useMax?OffMax::Match:Off::Match));
    }
    V3 Bone(uintptr_t p,uintptr_t o){
        auto tf=mem.r<uintptr_t>(p+o); if(!tf)return{};
        V3 pos{}; mem.R(tf+0x90,&pos,sizeof(pos));
        if(pos.len()<0.01f){
            auto ml=mem.r<uintptr_t>(tf+0x38); int ix=mem.r<int>(tf+0x40);
            if(ml&&ix>=0) mem.R(ml+ix*0x30+0x80,&pos,sizeof(pos));
        }
        return pos;
    }
    float HP(uintptr_t p){
        auto c=mem.r<uintptr_t>(p+(cfg.useMax?OffMax::HP:Off::HP)); if(!c)return 0;
        auto l=mem.r<uintptr_t>(c+(cfg.useMax?OffMax::HPL:Off::HPL)); if(!l)return 0;
        auto e=mem.r<uintptr_t>(l+(cfg.useMax?OffMax::HPE:Off::HPE)); if(!e)return 0;
        return mem.r<float>(e+(cfg.useMax?OffMax::HPV:Off::HPV));
    }
    void Matx(uintptr_t p){
        auto cam=mem.r<uintptr_t>(p+(cfg.useMax?OffMax::Cam:Off::Cam)); if(!cam)return;
        auto fo=mem.r<uintptr_t>(cam+(cfg.useMax?OffMax::CFol:Off::CFol)); if(!fo)return;
        auto raw=mem.r<uintptr_t>(fo+(cfg.useMax?OffMax::CPtr:Off::CPtr)); if(!raw)return;
        mem.R(raw+(cfg.useMax?OffMax::CMat:Off::CMat),&vm,sizeof(vm));
    }
    void Upd(){
        if(!ok)return;
        auto m=Mat(); if(!m){ std::lock_guard<std::mutex> lk(mx); es.clear(); loc=0; return; }
        loc=mem.r<uintptr_t>(m+(cfg.useMax?OffMax::Local:Off::Local)); if(!loc)return;
        Matx(loc); lp=Bone(loc,cfg.useMax?OffMax::Head:Off::Head);
        auto d=mem.r<uintptr_t>(m+(cfg.useMax?OffMax::Dict:Off::Dict)); if(!d)return;
        int cnt=mem.r<int>(d+(cfg.useMax?OffMax::DCnt:Off::DCnt));
        auto ent=mem.r<uintptr_t>(d+(cfg.useMax?OffMax::DEnt:Off::DEnt));
        if(!ent||cnt<=0||cnt>80)return;
        std::vector<Ent> t;
        for(int i=0;i<cnt;i++){
            auto e=ent+(cfg.useMax?OffMax::DHdr:Off::DHdr)+i*(cfg.useMax?OffMax::DStr:Off::DStr);
            auto p=mem.r<uintptr_t>(e+(cfg.useMax?OffMax::DVal:Off::DVal));
            if(!p||p==loc)continue;
            if(mem.r<bool>(p+(cfg.useMax?OffMax::Dead:Off::Dead)))continue;
            Ent x; x.a=p; x.bot=mem.r<bool>(p+(cfg.useMax?OffMax::Bot:Off::Bot));
            x.hp=HP(p); x.head=Bone(p,cfg.useMax?OffMax::Head:Off::Head);
            x.foot=Bone(p,cfg.useMax?OffMax::Foot:Off::Foot);
            x.dist=(x.head-lp).len(); if(x.dist>cfg.maxD)continue;
            t.push_back(x);
        }
        { std::lock_guard<std::mutex> lk(mx); es=std::move(t); }
        if(cfg.norecoil&&loc){
            auto w=mem.r<uintptr_t>(loc+(cfg.useMax?OffMax::Wep:Off::Wep));
            if(w){ auto d=mem.r<uintptr_t>(w+(cfg.useMax?0x88:Off::WD)); if(d)mem.w(d+Off::WR,0.f); }
        }
        if(cfg.silent&&loc){
            Ent* best=nullptr; float bf=cfg.fov;
            for(auto& e:es){
                float sx,sy; if(!vm.w2s(e.head,&sx,&sy,sw,sh))continue;
                float f=sqrtf((sx-sw*.5f)*(sx-sw*.5f)+(sy-sh*.5f)*(sy-sh*.5f));
                if(f<bf){bf=f;best=&e;}
            }
            if(best){
                auto ai=mem.r<uintptr_t>(loc+(cfg.useMax?OffMax::Aim:Off::Aim));
                if(ai){ V3 dir=(best->head-lp).norm();
                    mem.w(ai+(cfg.useMax?OffMax::ADir:Off::ADir),dir);
                    mem.w(ai+(cfg.useMax?OffMax::APos:Off::APos),lp);
                }
            }
        }
    }
    std::vector<float> Draw(){
        std::lock_guard<std::mutex> lk(mx);
        std::vector<float> o; o.push_back(0); int n=0;
        for(auto& e:es){
            float hx,hy,fx,fy;
            if(!vm.w2s(e.head,&hx,&hy,sw,sh)||!vm.w2s(e.foot,&fx,&fy,sw,sh))continue;
            float h=fy-hy,w=h*.45f,l=hx-w*.5f,r=hx+w*.5f;
            int col=e.bot?0xFFFFFF00:0xFF00FF00;
            o.push_back(l);o.push_back(hy);o.push_back(r);o.push_back(fy);
            o.push_back((float)col);o.push_back(e.hp); n++;
        }
        o[0]=(float)n; return o;
    }
};