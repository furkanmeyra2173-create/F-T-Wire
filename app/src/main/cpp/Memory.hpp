#pragma once
#include <unistd.h>
#include <sys/uio.h>
#include <dirent.h>
#include <fcntl.h>
#include <cstdio>
#include <cstring>
#include <cstdint>

struct Mem {
    int pid=-1; uintptr_t base=0;
    bool Attach(const char* pkg) {
        DIR* d=opendir("/proc"); if(!d) return false;
        dirent* e; while((e=readdir(d))) {
            if(e->d_type!=DT_DIR) continue;
            char p[64]; snprintf(p,64,"/proc/%s/cmdline",e->d_name);
            int fd=open(p,O_RDONLY); if(fd<0) continue;
            char c[256]{}; read(fd,c,255); close(fd);
            if(strstr(c,pkg)){ pid=atoi(e->d_name); break; }
        }
        closedir(d); return pid>0;
    }
    uintptr_t Mod(const char* m) {
        char maps[64]; snprintf(maps,64,"/proc/%d/maps",pid);
        FILE* f=fopen(maps,"r"); if(!f) return 0;
        char l[512]; uintptr_t b=0;
        while(fgets(l,512,f)) if(strstr(l,m)){ sscanf(l,"%lx-",&b); break; }
        fclose(f); return b;
    }
    bool R(uintptr_t a,void* b,size_t s){
        if(!a||!b||!s||pid<=0) return false;
        iovec lo{b,s}, re{(void*)a,s};
        return process_vm_readv(pid,&lo,1,&re,1,0)==(ssize_t)s;
    }
    template<typename T> T r(uintptr_t a){ T v{}; R(a,&v,sizeof(T)); return v; }
    bool W(uintptr_t a,const void* b,size_t s){
        if(!a||!b||!s||pid<=0) return false;
        iovec lo{(void*)b,s}, re{(void*)a,s};
        return process_vm_writev(pid,&lo,1,&re,1,0)==(ssize_t)s;
    }
    template<typename T> bool w(uintptr_t a,const T& v){ return W(a,&v,sizeof(T)); }
};