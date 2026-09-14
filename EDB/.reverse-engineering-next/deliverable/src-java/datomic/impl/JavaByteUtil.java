/*
 * Decompiled with CFR 0.152.
 */
package datomic.impl;

public class JavaByteUtil {
    protected static final char bs = '\\';

    public static byte[] to7Bit(byte[] ba) {
        int inputOffset = 0;
        int outputOffset = 0;
        int len = ba.length;
        byte[] out = new byte[len % 7 == 0 ? len * 8 / 7 : len * 8 / 7 + 1];
        int lastBits = 0;
        int i = 0;
        while (inputOffset < len) {
            int b = ba[inputOffset] & 0xFF;
            out[outputOffset++] = (byte)((b << i | lastBits) & 0x7F);
            if (i == 7) {
                i = 0;
                lastBits = 0;
            } else {
                lastBits = b >>> 7 - i;
                ++inputOffset;
                ++i;
            }
            if (inputOffset != len) continue;
            out[outputOffset] = (byte)lastBits;
        }
        return out;
    }

    public static byte[] to8Bit(byte[] ba) {
        int len = ba.length;
        byte[] out = new byte[len * 7 / 8];
        int i = 0;
        int inputOffset = 0;
        int outputOffset = 0;
        while (inputOffset < len - 1) {
            out[outputOffset++] = (byte)((ba[inputOffset] & 0xFF) >>> i | (ba[++inputOffset] & 0xFF) << 7 - i);
            if (++i != 7) continue;
            i = 0;
            ++inputOffset;
        }
        return out;
    }

    public static String makePackedStringSource(byte[] bi) {
        byte[] ba = JavaByteUtil.to7Bit(bi);
        StringBuffer buf = new StringBuffer("String XXX = \\\n\"");
        int lineCount = 0;
        int len = ba.length;
        for (int i = 0; i < len; ++i) {
            int b = ba[i] & 0xFF;
            if (b == 8) {
                lineCount += 2;
                buf.append('\\');
                buf.append('b');
            } else if (b == 9) {
                lineCount += 2;
                buf.append('\\');
                buf.append('t');
            } else if (b == 10) {
                lineCount += 2;
                buf.append('\\');
                buf.append('n');
            } else if (b == 12) {
                lineCount += 2;
                buf.append('\\');
                buf.append('f');
            } else if (b == 13) {
                lineCount += 2;
                buf.append('\\');
                buf.append('r');
            } else if (b < 32) {
                lineCount += 4;
                buf.append('\\');
                buf.append('0');
                if (b < 8) {
                    buf.append('0');
                    buf.append((char)(48 + b));
                } else {
                    String octalRep = Integer.toString(b, 8);
                    buf.append(octalRep);
                }
            } else if (b == 34 || b == 92) {
                lineCount += 2;
                buf.append('\\');
                buf.append((char)b);
            } else {
                ++lineCount;
                buf.append((char)b);
            }
            if (lineCount <= 59) continue;
            lineCount = 0;
            buf.append("\" +\n\"");
        }
        if (lineCount != 0) {
            buf.append("\";\n");
        }
        return buf.toString();
    }

    public static byte[] decodePackedString(String s) {
        return JavaByteUtil.to8Bit(s.getBytes());
    }
}

