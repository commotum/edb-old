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
import datomic.common$pooled_mapv$fn__9238;

public final class common$pooled_mapv
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object exec, Object f, Object coll) {
        Object object = f;
        f = null;
        Object object2 = exec;
        exec = null;
        Object object3 = coll;
        coll = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke((Object)new common$pooled_mapv$fn__9238(object, object2), object3));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return common$pooled_mapv.invokeStatic(object4, object5, object6);
    }
}

