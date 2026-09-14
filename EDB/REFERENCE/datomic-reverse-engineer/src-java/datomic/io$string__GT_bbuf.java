/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.ByteBuffer;

public final class io$string__GT_bbuf
extends AFunction {
    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return ByteBuffer.wrap(((String)object).getBytes("UTF-8"));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$string__GT_bbuf.invokeStatic(object2);
    }
}

