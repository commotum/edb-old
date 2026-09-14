/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LD
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class monitor$ns__GT_ms
extends AFunction
implements IFn.LD {
    public static double invokeStatic(long nanos) {
        return Numbers.divide((long)(nanos / 10000L), (double)100.0);
    }

    public Object invoke(Object object) {
        return new Double(monitor$ns__GT_ms.invokeStatic(RT.longCast((Object)((Number)object))));
    }

    public final double invokePrim(long l) {
        return monitor$ns__GT_ms.invokeStatic(l);
    }
}

