/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OLO
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$seek
extends AFunction
implements IFn.OLO {
    public static Object invokeStatic(Object buf, long n) {
        int pos = ((Buffer)buf).position();
        Object object = buf;
        buf = null;
        return ((ByteBuffer)object).duplicate().position(RT.intCast((long)Numbers.add((long)n, (long)pos)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return io$seek.invokeStatic(object3, RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return io$seek.invokeStatic(object2, l);
    }
}

