/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$values_in_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"unique-values");
    public static final Var const__2 = RT.var((String)"datomic.tools", (String)"uniques-in-ts");

    public static Object invokeStatic(Object db2, Object log2, Object ts) {
        Object object = db2;
        db2 = null;
        Object vids = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(object));
        Object object2 = log2;
        log2 = null;
        Object object3 = ts;
        ts = null;
        Object object4 = vids;
        vids = null;
        return ((IFn)const__2.getRawRoot()).invoke(object2, object3, object4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return tools$values_in_ts.invokeStatic(object4, object5, object6);
    }
}

