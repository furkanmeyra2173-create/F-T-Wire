#pragma once
#include <cstdint>

namespace Off {
    // GameFacade chain
    constexpr uintptr_t GF          = 0xA72E4E0;
    constexpr uintptr_t GF2         = 0xB8;
    constexpr uintptr_t Match       = 0x90;
    constexpr uintptr_t Timer       = 0x20;
    constexpr uintptr_t TimerDelta  = 0x2C;
    constexpr uintptr_t MatchRun    = 0xCC;
    constexpr uintptr_t Dict        = 0xC0;
    constexpr uintptr_t Local       = 0xD8;

    // Dictionary
    constexpr uintptr_t DEnt        = 0x18;
    constexpr uintptr_t DCnt        = 0x20;
    constexpr uintptr_t DHdr        = 0x20;
    constexpr uintptr_t DStr        = 0x18;
    constexpr uintptr_t DVal        = 0x10;

    // Player
    constexpr uintptr_t Rot         = 0x614;
    constexpr uintptr_t Head        = 0x6A0;
    constexpr uintptr_t Foot        = 0x6E8;
    constexpr uintptr_t Cam         = 0x690;
    constexpr uintptr_t IsFiring    = 0x848;
    constexpr uintptr_t IsSight     = 0x898;
    constexpr uintptr_t HP          = 0x70;
    constexpr uintptr_t Armor       = 0x74;
    constexpr uintptr_t Dead        = 0x7C;
    constexpr uintptr_t Knocked     = 0x1258;
    constexpr uintptr_t KnockedFrz  = 0xB0;
    constexpr uintptr_t Avatar      = 0x770;
    constexpr uintptr_t HedCol      = 0x738;
    constexpr uintptr_t ColWrite    = 0x80;
    constexpr uintptr_t DeathInfo   = 0x24A8;
    constexpr uintptr_t Bot         = 0x4A0;
    constexpr uintptr_t Name        = 0x490;
    constexpr uintptr_t Aim         = 0xE50;
    constexpr uintptr_t Vel         = 0xF0;
    constexpr uintptr_t Grounded    = 0x11C;
    constexpr uintptr_t FOV         = 0x84;

    // Avatar / Uma
    constexpr uintptr_t Uma         = 0x138;
    constexpr uintptr_t Vis         = 0x101;
    constexpr uintptr_t UmaData     = 0x28;
    constexpr uintptr_t UmaSkip     = 0x91;

    // Camera
    constexpr uintptr_t CFol        = 0x28;
    constexpr uintptr_t CPtr        = 0x10;
    constexpr uintptr_t CMat        = 0x100;

    // HP chain
    constexpr uintptr_t HPL         = 0x10;
    constexpr uintptr_t HPE         = 0x20;
    constexpr uintptr_t HPV         = 0x18;

    // Weapon
    constexpr uintptr_t Wep         = 0x600;
    constexpr uintptr_t WD          = 0x80;
    constexpr uintptr_t WR          = 0x18;

    // AimInfo
    constexpr uintptr_t ADir        = 0x40;
    constexpr uintptr_t APos        = 0x4C;

    // Bones
    constexpr uintptr_t BHead       = 0x6A0;
    constexpr uintptr_t BHip        = 0x6A8;
    constexpr uintptr_t BBreast     = 0x6B0;
    constexpr uintptr_t BRoot       = 0x6C8;
    constexpr uintptr_t BLShoulder  = 0x708;
    constexpr uintptr_t BRShoulder  = 0x710;
    constexpr uintptr_t BLAnkle     = 0x6D8;
    constexpr uintptr_t BRAnkle     = 0x6E0;
    constexpr uintptr_t BLToe       = 0x6E8;
    constexpr uintptr_t BRToe       = 0x6F0;
    constexpr uintptr_t BRForeArm   = 0x728;
    constexpr uintptr_t BLForeArm   = 0x730;
    constexpr uintptr_t BRHand      = 0x718;
    constexpr uintptr_t BLHand      = 0x720;
}

namespace OffMax {
    // önceki Max set (gerekirse kullan)
    constexpr uintptr_t GF          = 0xAD68E18;
    constexpr uintptr_t GF2         = 0xB8;
    constexpr uintptr_t Match       = 0x90;
    constexpr uintptr_t Dict        = 0xC0;
    constexpr uintptr_t Local       = 0xD8;
    constexpr uintptr_t DEnt        = 0x18;
    constexpr uintptr_t DCnt        = 0x20;
    constexpr uintptr_t DHdr        = 0x20;
    constexpr uintptr_t DStr        = 0x18;
    constexpr uintptr_t DVal        = 0x10;
    constexpr uintptr_t Head        = 0x6a8;
    constexpr uintptr_t Foot        = 0x6d0;
    constexpr uintptr_t Cam         = 0x698;
    constexpr uintptr_t HP          = 0x70;
    constexpr uintptr_t Dead        = 0x7C;
    constexpr uintptr_t Bot         = 0x4a8;
    constexpr uintptr_t Aim         = 0xe58;
    constexpr uintptr_t Wep         = 0x608;
    constexpr uintptr_t Uma         = 0x138;
    constexpr uintptr_t Vis         = 0x101;
    constexpr uintptr_t CFol        = 0x30;
    constexpr uintptr_t CPtr        = 0x10;
    constexpr uintptr_t CMat        = 0x100;
    constexpr uintptr_t HPL         = 0x10;
    constexpr uintptr_t HPE         = 0x20;
    constexpr uintptr_t HPV         = 0x18;
    constexpr uintptr_t ADir        = 0x40;
    constexpr uintptr_t APos        = 0x4C;
}