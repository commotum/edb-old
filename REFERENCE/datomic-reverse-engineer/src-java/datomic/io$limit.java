/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OLO
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import java.nio.ByteBuffer;

public final class io$limit
extends AFunction
implements IFn.OLO {
    public static Object invokeStatic(Object buf, long limit2) {
        Object object = buf;
        buf = null;
        return ((ByteBuffer)object).duplicate().limit(RT.intCast((long)limit2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return io$limit.invokeStatic(object3, RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return io$limit.invokeStatic(object2, l);
    }
}

