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

public final class tools$catalog
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"system-cluster");
    public static final Var const__2 = RT.var((String)"datomic.catalog", (String)"get-catalog");

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object G__21813;
        Object object2;
        Object object3 = uri2;
        uri2 = null;
        Object G__218132 = object3;
        if (Util.identical((Object)G__218132, null)) {
            object2 = null;
        } else {
            G__218132 = null;
            object2 = G__21813 = ((IFn)const__1.getRawRoot()).invoke(G__218132);
        }
        if (Util.identical(G__21813, null)) {
            object = null;
        } else {
            Object object4 = G__21813;
            G__21813 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object4);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$catalog.invokeStatic(object2);
    }
}

