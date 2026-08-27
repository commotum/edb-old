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

public final class db$eid__GT_part
extends AFunction
implements IFn.LL {
    public static long invokeStatic(long eid) {
        return (eid & 0x3FFFFFFFFFFFFFFFL) >> (int)42L;
    }

    public Object invoke(Object object) {
        return new Long(db$eid__GT_part.invokeStatic(RT.uncheckedLongCast((Object)((Number)object))));
    }

    public final long invokePrim(long l) {
        return db$eid__GT_part.invokeStatic(l);
    }
}

