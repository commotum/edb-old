/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Log;
import datomic.extensions$tx_ids$fn__18006;

public final class extensions$tx_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");

    public static Object invokeStatic(Object log2, Object start, Object end) {
        Object object = log2;
        log2 = null;
        Object object2 = start;
        start = null;
        Object object3 = end;
        end = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new extensions$tx_ids$fn__18006(), ((Log)object).txRange(object2, object3));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return extensions$tx_ids.invokeStatic(object4, object5, object6);
    }
}

