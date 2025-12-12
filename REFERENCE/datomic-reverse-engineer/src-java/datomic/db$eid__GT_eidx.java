/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;

public final class db$eid__GT_eidx
extends AFunction
implements IFn.LL {
    public static long invokeStatic(long eid) {
        long ret = eid & 0x3FFFFFFFFFFL;
        return (ret & 0x20000000000L) == 0L ? ret : ret | 0xFFFFFC0000000000L;
    }

    public Object invoke(Object object) {
        return new Long(db$eid__GT_eidx.invokeStatic(RT.uncheckedLongCast((Object)((Number)object))));
    }

    public final long invokePrim(long l) {
        return db$eid__GT_eidx.invokeStatic(l);
    }
}

