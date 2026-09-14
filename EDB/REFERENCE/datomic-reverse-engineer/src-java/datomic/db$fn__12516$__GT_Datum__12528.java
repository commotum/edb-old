/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.db.Datum;

public final class db$fn__12516$__GT_Datum__12528
extends AFunction {
    public Object invoke(Object e, Object a, Object v, Object tOp) {
        Object object = e;
        e = null;
        Object object2 = a;
        a = null;
        Object object3 = v;
        v = null;
        Object object4 = tOp;
        tOp = null;
        return new Datum(RT.uncheckedLongCast((Object)((Number)object)), RT.uncheckedIntCast((Object)((Number)object2)), object3, RT.uncheckedLongCast((Object)((Number)object4)));
    }
}

