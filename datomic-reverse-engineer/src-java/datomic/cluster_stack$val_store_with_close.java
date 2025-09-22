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

public final class cluster_stack$val_store_with_close
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster-stack", (String)"->ValStoreWithClose");

    public static Object invokeStatic(Object store, Object close2) {
        Object object = store;
        store = null;
        Object object2 = close2;
        close2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cluster_stack$val_store_with_close.invokeStatic(object3, object4);
    }
}

