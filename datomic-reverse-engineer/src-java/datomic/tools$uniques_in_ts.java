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
import datomic.tools$uniques_in_ts$fn__21860;
import datomic.tools$uniques_in_ts$fn__21863;

public final class tools$uniques_in_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"avof-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mapcat");

    public static Object invokeStatic(Object log2, Object ts, Object ids) {
        Object object = ids;
        ids = null;
        Object object2 = log2;
        log2 = null;
        Object object3 = ts;
        ts = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new tools$uniques_in_ts$fn__21860(object), ((IFn)const__2.getRawRoot()).invoke((Object)new tools$uniques_in_ts$fn__21863(object2), object3)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return tools$uniques_in_ts.invokeStatic(object4, object5, object6);
    }
}

