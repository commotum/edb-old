/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLL
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;

public final class db$make_eid
extends AFunction
implements IFn.LLL {
    public static long invokeStatic(long part2, long l) {
        return part2 << (int)42L | l & 0x3FFFFFFFFFFL;
    }

    public Object invoke(Object object, Object object2) {
        return new Long(db$make_eid.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)), RT.uncheckedLongCast((Object)((Number)object2))));
    }

    public final long invokePrim(long l, long l2) {
        return db$make_eid.invokeStatic(l, l2);
    }
}

