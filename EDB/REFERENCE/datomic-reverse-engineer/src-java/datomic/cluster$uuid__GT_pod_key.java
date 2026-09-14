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

public final class cluster$uuid__GT_pod_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object uuid) {
        Object object = uuid;
        uuid = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)"pod-", object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$uuid__GT_pod_key.invokeStatic(object2);
    }
}

