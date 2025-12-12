/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datafy$vals_to_data$fn__17248;

public final class datafy$vals_to_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"transient");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new datafy$vals_to_data$fn__17248(), ((IFn)const__2.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$vals_to_data.invokeStatic(object2);
    }
}

