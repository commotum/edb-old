/*
 * Decompiled with CFR 0.152.
 */
package datomic.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

public class MersenneTwisterFast
implements Serializable,
Cloneable {
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = -1727483681;
    private static final int UPPER_MASK = Integer.MIN_VALUE;
    private static final int LOWER_MASK = Integer.MAX_VALUE;
    private static final int TEMPERING_MASK_B = -1658038656;
    private static final int TEMPERING_MASK_C = -272236544;
    private int[] mt;
    private int mti;
    private int[] mag01;
    private double __nextNextGaussian;
    private boolean __haveNextNextGaussian;

    public Object clone() throws CloneNotSupportedException {
        MersenneTwisterFast f = (MersenneTwisterFast)super.clone();
        f.mt = (int[])this.mt.clone();
        f.mag01 = (int[])this.mag01.clone();
        return f;
    }

    public boolean stateEquals(Object o) {
        int x;
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof MersenneTwisterFast)) {
            return false;
        }
        MersenneTwisterFast other = (MersenneTwisterFast)o;
        if (this.mti != other.mti) {
            return false;
        }
        for (x = 0; x < this.mag01.length; ++x) {
            if (this.mag01[x] == other.mag01[x]) continue;
            return false;
        }
        for (x = 0; x < this.mt.length; ++x) {
            if (this.mt[x] == other.mt[x]) continue;
            return false;
        }
        return true;
    }

    public void readState(DataInputStream stream) throws IOException {
        int x;
        int len = this.mt.length;
        for (x = 0; x < len; ++x) {
            this.mt[x] = stream.readInt();
        }
        len = this.mag01.length;
        for (x = 0; x < len; ++x) {
            this.mag01[x] = stream.readInt();
        }
        this.mti = stream.readInt();
        this.__nextNextGaussian = stream.readDouble();
        this.__haveNextNextGaussian = stream.readBoolean();
    }

    public void writeState(DataOutputStream stream) throws IOException {
        int x;
        int len = this.mt.length;
        for (x = 0; x < len; ++x) {
            stream.writeInt(this.mt[x]);
        }
        len = this.mag01.length;
        for (x = 0; x < len; ++x) {
            stream.writeInt(this.mag01[x]);
        }
        stream.writeInt(this.mti);
        stream.writeDouble(this.__nextNextGaussian);
        stream.writeBoolean(this.__haveNextNextGaussian);
    }

    public MersenneTwisterFast() {
        this(System.currentTimeMillis());
    }

    public MersenneTwisterFast(long seed) {
        this.setSeed(seed);
    }

    public MersenneTwisterFast(int[] array) {
        this.setSeed(array);
    }

    public synchronized void setSeed(long seed) {
        this.__haveNextNextGaussian = false;
        this.mt = new int[624];
        this.mag01 = new int[2];
        this.mag01[0] = 0;
        this.mag01[1] = -1727483681;
        this.mt[0] = (int)(seed & 0xFFFFFFFFFFFFFFFFL);
        this.mti = 1;
        while (this.mti < 624) {
            this.mt[this.mti] = 1812433253 * (this.mt[this.mti - 1] ^ this.mt[this.mti - 1] >>> 30) + this.mti;
            int n = this.mti++;
            this.mt[n] = this.mt[n] & 0xFFFFFFFF;
        }
    }

    public synchronized void setSeed(int[] array) {
        int k;
        if (array.length == 0) {
            throw new IllegalArgumentException("Array length must be greater than zero");
        }
        this.setSeed(19650218L);
        int i = 1;
        int j = 0;
        int n = k = 624 > array.length ? 624 : array.length;
        while (k != 0) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1664525) + array[j] + j;
            int n2 = i++;
            this.mt[n2] = this.mt[n2] & 0xFFFFFFFF;
            ++j;
            if (i >= 624) {
                this.mt[0] = this.mt[623];
                i = 1;
            }
            if (j >= array.length) {
                j = 0;
            }
            --k;
        }
        for (k = 623; k != 0; --k) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1566083941) - i;
            int n3 = i++;
            this.mt[n3] = this.mt[n3] & 0xFFFFFFFF;
            if (i < 624) continue;
            this.mt[0] = this.mt[623];
            i = 1;
        }
        this.mt[0] = Integer.MIN_VALUE;
    }

    public final int nextInt() {
        int y;
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        return y;
    }

    public final short nextShort() {
        int y;
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        return (short)(y >>> 16);
    }

    public final char nextChar() {
        int y;
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        return (char)(y >>> 16);
    }

    public final boolean nextBoolean() {
        int y;
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        return (y ^= y >>> 18) >>> 31 != 0;
    }

    public final boolean nextBoolean(float probability) {
        int y;
        if (probability < 0.0f || probability > 1.0f) {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        }
        if (probability == 0.0f) {
            return false;
        }
        if (probability == 1.0f) {
            return true;
        }
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        return (float)((y ^= y >>> 18) >>> 8) / 1.6777216E7f < probability;
    }

    public final boolean nextBoolean(double probability) {
        int z;
        int y;
        int kk;
        int[] mag01;
        int[] mt;
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        }
        if (probability == 0.0) {
            return false;
        }
        if (probability == 1.0) {
            return true;
        }
        if (this.mti >= 624) {
            mt = this.mt;
            mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        if (this.mti >= 624) {
            mt = this.mt;
            mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ z >>> 1 ^ mag01[z & 1];
            }
            while (kk < 623) {
                z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ z >>> 1 ^ mag01[z & 1];
                ++kk;
            }
            z = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ z >>> 1 ^ mag01[z & 1];
            this.mti = 0;
        }
        z = this.mt[this.mti++];
        z ^= z >>> 11;
        z ^= z << 7 & 0x9D2C5680;
        z ^= z << 15 & 0xEFC60000;
        return (double)(((long)(y >>> 6) << 27) + (long)((z ^= z >>> 18) >>> 5)) / 9.007199254740992E15 < probability;
    }

    public final byte nextByte() {
        int y;
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        return (byte)(y >>> 24);
    }

    public final void nextBytes(byte[] bytes) {
        for (int x = 0; x < bytes.length; ++x) {
            int y;
            if (this.mti >= 624) {
                int kk;
                int[] mt = this.mt;
                int[] mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
                }
                while (kk < 623) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                    ++kk;
                }
                y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
                this.mti = 0;
            }
            y = this.mt[this.mti++];
            y ^= y >>> 11;
            y ^= y << 7 & 0x9D2C5680;
            y ^= y << 15 & 0xEFC60000;
            y ^= y >>> 18;
            bytes[x] = (byte)(y >>> 24);
        }
    }

    public final long nextLong() {
        int z;
        int y;
        int kk;
        int[] mag01;
        int[] mt;
        if (this.mti >= 624) {
            mt = this.mt;
            mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        if (this.mti >= 624) {
            mt = this.mt;
            mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ z >>> 1 ^ mag01[z & 1];
            }
            while (kk < 623) {
                z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ z >>> 1 ^ mag01[z & 1];
                ++kk;
            }
            z = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ z >>> 1 ^ mag01[z & 1];
            this.mti = 0;
        }
        z = this.mt[this.mti++];
        z ^= z >>> 11;
        z ^= z << 7 & 0x9D2C5680;
        z ^= z << 15 & 0xEFC60000;
        z ^= z >>> 18;
        return ((long)y << 32) + (long)z;
    }

    public final long nextLong(long n) {
        long val;
        int z;
        int y;
        long bits;
        if (n <= 0L) {
            throw new IllegalArgumentException("n must be > 0");
        }
        do {
            int kk;
            int[] mag01;
            int[] mt;
            if (this.mti >= 624) {
                mt = this.mt;
                mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
                }
                while (kk < 623) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                    ++kk;
                }
                y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
                this.mti = 0;
            }
            y = this.mt[this.mti++];
            y ^= y >>> 11;
            y ^= y << 7 & 0x9D2C5680;
            y ^= y << 15 & 0xEFC60000;
            y ^= y >>> 18;
            if (this.mti >= 624) {
                mt = this.mt;
                mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ z >>> 1 ^ mag01[z & 1];
                }
                while (kk < 623) {
                    z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ z >>> 1 ^ mag01[z & 1];
                    ++kk;
                }
                z = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ z >>> 1 ^ mag01[z & 1];
                this.mti = 0;
            }
            z = this.mt[this.mti++];
            z ^= z >>> 11;
            z ^= z << 7 & 0x9D2C5680;
            z ^= z << 15 & 0xEFC60000;
        } while ((bits = ((long)y << 32) + (long)(z ^= z >>> 18) >>> 1) - (val = bits % n) + (n - 1L) < 0L);
        return val;
    }

    public final double nextDouble() {
        int z;
        int y;
        int kk;
        int[] mag01;
        int[] mt;
        if (this.mti >= 624) {
            mt = this.mt;
            mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        if (this.mti >= 624) {
            mt = this.mt;
            mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ z >>> 1 ^ mag01[z & 1];
            }
            while (kk < 623) {
                z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ z >>> 1 ^ mag01[z & 1];
                ++kk;
            }
            z = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ z >>> 1 ^ mag01[z & 1];
            this.mti = 0;
        }
        z = this.mt[this.mti++];
        z ^= z >>> 11;
        z ^= z << 7 & 0x9D2C5680;
        z ^= z << 15 & 0xEFC60000;
        z ^= z >>> 18;
        return (double)(((long)(y >>> 6) << 27) + (long)(z >>> 5)) / 9.007199254740992E15;
    }

    public final double nextGaussian() {
        int b;
        int a;
        double v2;
        int z;
        int y;
        double v1;
        double s;
        if (this.__haveNextNextGaussian) {
            this.__haveNextNextGaussian = false;
            return this.__nextNextGaussian;
        }
        do {
            int kk;
            int[] mag01;
            int[] mt;
            if (this.mti >= 624) {
                mt = this.mt;
                mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
                }
                while (kk < 623) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                    ++kk;
                }
                y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
                this.mti = 0;
            }
            y = this.mt[this.mti++];
            y ^= y >>> 11;
            y ^= y << 7 & 0x9D2C5680;
            y ^= y << 15 & 0xEFC60000;
            y ^= y >>> 18;
            if (this.mti >= 624) {
                mt = this.mt;
                mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ z >>> 1 ^ mag01[z & 1];
                }
                while (kk < 623) {
                    z = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ z >>> 1 ^ mag01[z & 1];
                    ++kk;
                }
                z = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ z >>> 1 ^ mag01[z & 1];
                this.mti = 0;
            }
            z = this.mt[this.mti++];
            z ^= z >>> 11;
            z ^= z << 7 & 0x9D2C5680;
            z ^= z << 15 & 0xEFC60000;
            z ^= z >>> 18;
            if (this.mti >= 624) {
                mt = this.mt;
                mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    a = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ a >>> 1 ^ mag01[a & 1];
                }
                while (kk < 623) {
                    a = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ a >>> 1 ^ mag01[a & 1];
                    ++kk;
                }
                a = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ a >>> 1 ^ mag01[a & 1];
                this.mti = 0;
            }
            a = this.mt[this.mti++];
            a ^= a >>> 11;
            a ^= a << 7 & 0x9D2C5680;
            a ^= a << 15 & 0xEFC60000;
            a ^= a >>> 18;
            if (this.mti >= 624) {
                mt = this.mt;
                mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    b = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ b >>> 1 ^ mag01[b & 1];
                }
                while (kk < 623) {
                    b = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ b >>> 1 ^ mag01[b & 1];
                    ++kk;
                }
                b = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ b >>> 1 ^ mag01[b & 1];
                this.mti = 0;
            }
            b = this.mt[this.mti++];
            b ^= b >>> 11;
            b ^= b << 7 & 0x9D2C5680;
            b ^= b << 15 & 0xEFC60000;
        } while ((s = (v1 = 2.0 * ((double)(((long)(y >>> 6) << 27) + (long)(z >>> 5)) / 9.007199254740992E15) - 1.0) * v1 + (v2 = 2.0 * ((double)(((long)(a >>> 6) << 27) + (long)((b ^= b >>> 18) >>> 5)) / 9.007199254740992E15) - 1.0) * v2) >= 1.0 || s == 0.0);
        double multiplier = Math.sqrt(-2.0 * Math.log(s) / s);
        this.__nextNextGaussian = v2 * multiplier;
        this.__haveNextNextGaussian = true;
        return v1 * multiplier;
    }

    public final float nextFloat() {
        int y;
        if (this.mti >= 624) {
            int kk;
            int[] mt = this.mt;
            int[] mag01 = this.mag01;
            for (kk = 0; kk < 227; ++kk) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
            }
            while (kk < 623) {
                y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                ++kk;
            }
            y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
            mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
            this.mti = 0;
        }
        y = this.mt[this.mti++];
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        return (float)(y >>> 8) / 1.6777216E7f;
    }

    public final int nextInt(int n) {
        int val;
        int y;
        int bits;
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }
        if ((n & -n) == n) {
            int y2;
            if (this.mti >= 624) {
                int kk;
                int[] mt = this.mt;
                int[] mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    y2 = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ y2 >>> 1 ^ mag01[y2 & 1];
                }
                while (kk < 623) {
                    y2 = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ y2 >>> 1 ^ mag01[y2 & 1];
                    ++kk;
                }
                y2 = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ y2 >>> 1 ^ mag01[y2 & 1];
                this.mti = 0;
            }
            y2 = this.mt[this.mti++];
            y2 ^= y2 >>> 11;
            y2 ^= y2 << 7 & 0x9D2C5680;
            y2 ^= y2 << 15 & 0xEFC60000;
            y2 ^= y2 >>> 18;
            return (int)((long)n * (long)(y2 >>> 1) >> 31);
        }
        do {
            if (this.mti >= 624) {
                int kk;
                int[] mt = this.mt;
                int[] mag01 = this.mag01;
                for (kk = 0; kk < 227; ++kk) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + 397] ^ y >>> 1 ^ mag01[y & 1];
                }
                while (kk < 623) {
                    y = mt[kk] & Integer.MIN_VALUE | mt[kk + 1] & Integer.MAX_VALUE;
                    mt[kk] = mt[kk + -227] ^ y >>> 1 ^ mag01[y & 1];
                    ++kk;
                }
                y = mt[623] & Integer.MIN_VALUE | mt[0] & Integer.MAX_VALUE;
                mt[623] = mt[396] ^ y >>> 1 ^ mag01[y & 1];
                this.mti = 0;
            }
            y = this.mt[this.mti++];
            y ^= y >>> 11;
            y ^= y << 7 & 0x9D2C5680;
            y ^= y << 15 & 0xEFC60000;
        } while ((bits = (y ^= y >>> 18) >>> 1) - (val = bits % n) + (n - 1) < 0);
        return val;
    }

    public static void main(String[] args) {
        int j;
        MersenneTwisterFast r = new MersenneTwisterFast(new int[]{291, 564, 837, 1110});
        System.out.println("Output of MersenneTwisterFast with new (2002/1/26) seeding mechanism");
        for (j = 0; j < 1000; ++j) {
            long l = r.nextInt();
            if (l < 0L) {
                l += 0x100000000L;
            }
            Object s = String.valueOf(l);
            while (((String)s).length() < 10) {
                s = " " + (String)s;
            }
            System.out.print((String)s + " ");
            if (j % 5 != 4) continue;
            System.out.println();
        }
        long SEED = 4357L;
        System.out.println("\nTime to test grabbing 100000000 ints");
        Random rr = new Random(4357L);
        int xx = 0;
        long ms = System.currentTimeMillis();
        for (j = 0; j < 100000000; ++j) {
            xx += rr.nextInt();
        }
        System.out.println("java.util.Random: " + (System.currentTimeMillis() - ms) + "          Ignore this: " + xx);
        r = new MersenneTwisterFast(4357L);
        ms = System.currentTimeMillis();
        xx = 0;
        for (j = 0; j < 100000000; ++j) {
            xx += r.nextInt();
        }
        System.out.println("Mersenne Twister Fast: " + (System.currentTimeMillis() - ms) + "          Ignore this: " + xx);
        System.out.println("\nGrab the first 1000 booleans");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextBoolean() + " ");
            if (j % 8 != 7) continue;
            System.out.println();
        }
        if (j % 8 != 7) {
            System.out.println();
        }
        System.out.println("\nGrab 1000 booleans of increasing probability using nextBoolean(double)");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextBoolean((double)j / 999.0) + " ");
            if (j % 8 != 7) continue;
            System.out.println();
        }
        if (j % 8 != 7) {
            System.out.println();
        }
        System.out.println("\nGrab 1000 booleans of increasing probability using nextBoolean(float)");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextBoolean((float)j / 999.0f) + " ");
            if (j % 8 != 7) continue;
            System.out.println();
        }
        if (j % 8 != 7) {
            System.out.println();
        }
        byte[] bytes = new byte[1000];
        System.out.println("\nGrab the first 1000 bytes using nextBytes");
        r = new MersenneTwisterFast(4357L);
        r.nextBytes(bytes);
        for (j = 0; j < 1000; ++j) {
            System.out.print(bytes[j] + " ");
            if (j % 16 != 15) continue;
            System.out.println();
        }
        if (j % 16 != 15) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 bytes -- must be same as nextBytes");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            byte b = r.nextByte();
            System.out.print(b + " ");
            if (b != bytes[j]) {
                System.out.print("BAD ");
            }
            if (j % 16 != 15) continue;
            System.out.println();
        }
        if (j % 16 != 15) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 shorts");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextShort() + " ");
            if (j % 8 != 7) continue;
            System.out.println();
        }
        if (j % 8 != 7) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 ints");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextInt() + " ");
            if (j % 4 != 3) continue;
            System.out.println();
        }
        if (j % 4 != 3) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 ints of different sizes");
        r = new MersenneTwisterFast(4357L);
        int max2 = 1;
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextInt(max2) + " ");
            if ((max2 *= 2) <= 0) {
                max2 = 1;
            }
            if (j % 4 != 3) continue;
            System.out.println();
        }
        if (j % 4 != 3) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 longs");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextLong() + " ");
            if (j % 3 != 2) continue;
            System.out.println();
        }
        if (j % 3 != 2) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 longs of different sizes");
        r = new MersenneTwisterFast(4357L);
        long max22 = 1L;
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextLong(max22) + " ");
            if ((max22 *= 2L) <= 0L) {
                max22 = 1L;
            }
            if (j % 4 != 3) continue;
            System.out.println();
        }
        if (j % 4 != 3) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 floats");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextFloat() + " ");
            if (j % 4 != 3) continue;
            System.out.println();
        }
        if (j % 4 != 3) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 doubles");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextDouble() + " ");
            if (j % 3 != 2) continue;
            System.out.println();
        }
        if (j % 3 != 2) {
            System.out.println();
        }
        System.out.println("\nGrab the first 1000 gaussian doubles");
        r = new MersenneTwisterFast(4357L);
        for (j = 0; j < 1000; ++j) {
            System.out.print(r.nextGaussian() + " ");
            if (j % 3 != 2) continue;
            System.out.println();
        }
        if (j % 3 != 2) {
            System.out.println();
        }
    }
}

