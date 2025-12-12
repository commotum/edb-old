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
import datomic.db$calc_tempids$fn__13968;

public final class db$calc_tempids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.set", (String)"map-invert");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"transient");

    public static Object invokeStatic(Object gid__GT_lid, Object lid__GT_eid) {
        Object lid__GT_gid;
        Object object = gid__GT_lid;
        gid__GT_lid = null;
        Object object2 = lid__GT_gid = ((IFn)const__0.getRawRoot()).invoke(object);
        lid__GT_gid = null;
        Object object3 = lid__GT_eid;
        lid__GT_eid = null;
        return ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new db$calc_tempids$fn__13968(object2), ((IFn)const__3.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$calc_tempids.invokeStatic(object3, object4);
    }
}

