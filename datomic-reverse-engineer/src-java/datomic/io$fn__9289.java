/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$fn__9289
extends AFunction {
    public static Object invokeStatic(Object buff) {
        Object object = buff;
        buff = null;
        ByteBuffer buff2 = ((ByteBuffer)object).duplicate();
        int n = ((Buffer)buff2).remaining();
        byte[] bytes = Numbers.byte_array((Object)n);
        ByteBuffer byteBuffer = buff2;
        buff2 = null;
        byteBuffer.get(bytes);
        Object var3_3 = null;
        return bytes;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$fn__9289.invokeStatic(object2);
    }
}

