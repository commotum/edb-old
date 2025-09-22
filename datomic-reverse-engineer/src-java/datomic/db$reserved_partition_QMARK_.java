/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LO
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class db$reserved_partition_QMARK_
extends AFunction
implements IFn.LO {
    public static Object invokeStatic(long part2) {
        return Numbers.lt((long)part2, (long)4L) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        return db$reserved_partition_QMARK_.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return db$reserved_partition_QMARK_.invokeStatic(l);
    }
}

