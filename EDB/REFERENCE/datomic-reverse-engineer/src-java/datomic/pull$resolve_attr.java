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

public final class pull$resolve_attr
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"attribute");

    public static Object invokeStatic(Object db2, Object kw) {
        Object object;
        Object G__18995;
        Object object2;
        Object G__189952;
        Object object3;
        Object object4 = kw;
        kw = null;
        Object G__189953 = object4;
        if (Util.identical((Object)G__189953, null)) {
            object3 = null;
        } else {
            G__189953 = null;
            object3 = G__189952 = ((IFn)const__1.getRawRoot()).invoke(G__189953);
        }
        if (Util.identical(G__189952, null)) {
            object2 = null;
        } else {
            G__189952 = null;
            object2 = G__18995 = ((IFn)const__2.getRawRoot()).invoke(db2, G__189952);
        }
        if (Util.identical(G__18995, null)) {
            object = null;
        } else {
            Object object5 = db2;
            db2 = null;
            Object object6 = G__18995;
            G__18995 = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object5, object6);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$resolve_attr.invokeStatic(object3, object4);
    }
}

