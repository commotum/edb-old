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

public final class tools$touch_index_ref
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster", (String)"touch-ref");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"index-ref-key-name");

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        Object object2 = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$touch_index_ref.invokeStatic(object2);
    }
}

