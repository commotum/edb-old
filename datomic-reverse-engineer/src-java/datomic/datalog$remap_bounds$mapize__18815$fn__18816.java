/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Tuple;
import clojure.lang.Util;

public final class datalog$remap_bounds$mapize__18815$fn__18816
extends AFunction {
    Object ia;

    public datalog$remap_bounds$mapize__18815$fn__18816(Object object) {
        this.ia = object;
    }

    public Object invoke(Object i, Object c) {
        IPersistentVector iPersistentVector;
        if (Util.identical((Object)c, null)) {
            iPersistentVector = null;
        } else {
            Object object = i;
            i = null;
            Object object2 = c;
            c = null;
            iPersistentVector = Tuple.create((Object)((IFn)this.ia).invoke(object), (Object)object2);
        }
        return iPersistentVector;
    }
}

