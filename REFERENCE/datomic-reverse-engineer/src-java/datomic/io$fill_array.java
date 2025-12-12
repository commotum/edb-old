/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.io.InputStream;

public final class io$fill_array
extends AFunction {
    public static Object invokeStatic(Object is, Object ba) {
        int len = ((byte[])ba).length;
        long n = 0L;
        while (n != (long)len) {
            int r = ((InputStream)is).read((byte[])ba, RT.intCast((long)n), RT.intCast((long)Numbers.minus((long)len, (long)n)));
            if (-1L == (long)r) {
                throw (Throwable)new Exception("Premature EOS");
            }
            n = Numbers.add((long)n, (long)r);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$fill_array.invokeStatic(object3, object4);
    }
}

