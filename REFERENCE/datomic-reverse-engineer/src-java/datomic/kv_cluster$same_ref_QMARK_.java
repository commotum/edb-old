/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class kv_cluster$same_ref_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"require-keys");
    public static final Var const__2 = RT.var((String)"datomic.kv-cluster", (String)"ref-identity-keys");

    public static Object invokeStatic(Object r1, Object r2) {
        Object object = r1;
        r1 = null;
        Object object2 = r2;
        r2 = null;
        return Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(object, const__2.getRawRoot()), (Object)((IFn)const__1.getRawRoot()).invoke(object2, const__2.getRawRoot())) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return kv_cluster$same_ref_QMARK_.invokeStatic(object3, object4);
    }
}

