/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import datomic.db.Datum;

public final class db$asserting_datum
extends AFunction
implements IFn.LLOLO {
    public static Object invokeStatic(long e, long v, Object object, long l) {
        Object object2 = object;
        object = null;
        return new Datum(e, RT.uncheckedIntCast((long)v), object2, l << (int)1L | 1L);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object3;
        object3 = null;
        return db$asserting_datum.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)), RT.uncheckedLongCast((Object)((Number)object2)), object5, RT.uncheckedLongCast((Object)((Number)object4)));
    }

    public final Object invokePrim(long l, long l2, Object object, long l3) {
        Object object2 = object;
        object = null;
        return db$asserting_datum.invokeStatic(l, l2, object2, l3);
    }
}

