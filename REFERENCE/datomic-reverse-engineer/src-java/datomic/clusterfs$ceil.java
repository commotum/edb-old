/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OL
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;

public final class clusterfs$ceil
extends AFunction
implements IFn.OL {
    public static long invokeStatic(Object num) {
        Object object = num;
        num = null;
        return RT.longCast((double)Math.ceil(RT.doubleCast((Object)object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return new Long(clusterfs$ceil.invokeStatic(object2));
    }

    public final long invokePrim(Object object) {
        Object object2 = object;
        object = null;
        return clusterfs$ceil.invokeStatic(object2);
    }
}

