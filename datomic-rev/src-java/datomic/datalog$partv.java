/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOO
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import datomic.datalog$partv$pv__18118;

public final class datalog$partv
extends AFunction
implements IFn.LOO {
    public static Object invokeStatic(long n, Object object) {
        datalog$partv$pv__18118 pv;
        datalog$partv$pv__18118 datalog$partv$pv__18118 = pv = new datalog$partv$pv__18118(n);
        pv = null;
        Object object2 = object;
        object = null;
        return ((IFn)datalog$partv$pv__18118).invoke(((Iterable)object2).iterator());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object2;
        object2 = null;
        return datalog$partv.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)), object3);
    }

    public final Object invokePrim(long l, Object object) {
        Object object2 = object;
        object = null;
        return datalog$partv.invokeStatic(l, object2);
    }
}

