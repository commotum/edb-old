/*
 * Decompiled with CFR 0.152.
 */
package datomic.impl;

public class ModifiedFNVHash {
    public static final int p = 16777619;
    public static final int seed = -2128831035;

    private static int postProcess(int hash2) {
        hash2 += hash2 << 13;
        hash2 ^= hash2 >> 7;
        hash2 += hash2 << 3;
        hash2 ^= hash2 >> 17;
        hash2 += hash2 << 5;
        return hash2;
    }

    public static int hash(String s) {
        int hash2 = -2128831035;
        for (int n = 0; n < s.length(); ++n) {
            hash2 = (hash2 ^ s.charAt(n)) * 16777619;
        }
        return ModifiedFNVHash.postProcess(hash2);
    }

    public static int hash(byte[] data2, int offset, int length) {
        int hash2 = -2128831035;
        for (int n = offset; n < offset + length; ++n) {
            hash2 = (hash2 ^ data2[n]) * 16777619;
        }
        return ModifiedFNVHash.postProcess(hash2);
    }
}

