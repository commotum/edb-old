/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$sparse_es_2
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"sparse-e-xf");

    public static Object invokeStatic(Object olookup, Object idx, Object es) {
        Object object = olookup;
        olookup = null;
        Object object2 = idx;
        idx = null;
        Object object3 = es;
        es = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__1.getRawRoot()).invoke(object, object2), object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$sparse_es_2.invokeStatic(object4, object5, object6);
    }
}

