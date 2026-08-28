/*
 * Decompiled with CFR 0.152.
 */
package datomic.impl;

public class MurmurHash {
    public static int hash(byte[] data2, int length, int seed) {
        int m = 1540483477;
        int r = 24;
        int h = seed ^ length;
        int len_4 = length >> 2;
        for (int i = 0; i < len_4; ++i) {
            int i_4 = i << 2;
            int k = data2[i_4 + 3];
            k <<= 8;
            k |= data2[i_4 + 2] & 0xFF;
            k <<= 8;
            k |= data2[i_4 + 1] & 0xFF;
            k <<= 8;
            k |= data2[i_4 + 0] & 0xFF;
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }
        int len_m = len_4 << 2;
        int left = length - len_m;
        if (left != 0) {
            if (left >= 3) {
                h ^= data2[length - 3] << 16;
            }
            if (left >= 2) {
                h ^= data2[length - 2] << 8;
            }
            if (left >= 1) {
                h ^= data2[length - 1];
            }
            h *= m;
        }
        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;
        return h;
    }
}
