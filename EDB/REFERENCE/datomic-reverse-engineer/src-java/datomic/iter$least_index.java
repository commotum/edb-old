/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import datomic.iter.Iter;
import java.util.Comparator;

public final class iter$least_index
extends AFunction
implements IFn.OOL {
    public static long invokeStatic(Object cmp, Object iters) {
        Object v1 = ((Iter)RT.aget((Object[])((Object[])iters), (int)RT.intCast((long)0L))).get();
        Object object = iters;
        iters = null;
        Object v2 = ((Iter)RT.aget((Object[])((Object[])object), (int)RT.intCast((long)1L))).get();
        Object object2 = cmp;
        cmp = null;
        Object object3 = v1;
        v1 = null;
        Object object4 = v2;
        v2 = null;
        return (long)((Comparator)object2).compare(object3, object4) < 0L ? 0L : 1L;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(iter$least_index.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return iter$least_index.invokeStatic(object3, object4);
    }
}

