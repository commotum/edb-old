/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$index_summary$fn__17838;

public final class stats$index_summary
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object db2, Object index2, Object idx, Object partfn) {
        Object object = partfn;
        partfn = null;
        Object object2 = idx;
        idx = null;
        Object object3 = index2;
        index2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new stats$index_summary$fn__17838(object), (Object)PersistentArrayMap.EMPTY, ((IFn)object2).invoke(object3));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return stats$index_summary.invokeStatic(object5, object6, object7, object8);
    }
}

