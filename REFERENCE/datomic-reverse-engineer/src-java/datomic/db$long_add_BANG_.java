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
import java.util.concurrent.atomic.LongAdder;

public final class db$long_add_BANG_
extends AFunction
implements IFn.OLO {
    public static Object invokeStatic(Object a, long v) {
        Object object = a;
        a = null;
        ((LongAdder)object).add(v);
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return db$long_add_BANG_.invokeStatic(object3, RT.uncheckedLongCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return db$long_add_BANG_.invokeStatic(object2, l);
    }
}

