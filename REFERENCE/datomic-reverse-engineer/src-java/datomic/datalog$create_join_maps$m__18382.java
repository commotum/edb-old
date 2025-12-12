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
import datomic.datalog$create_join_maps$m__18382$fn__18383;

public final class datalog$create_join_maps$m__18382
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public Object invoke(Object ib, Object jb, Object jbinds) {
        Object object = ib;
        ib = null;
        Object object2 = jb;
        jb = null;
        Object object3 = jbinds;
        jbinds = null;
        datalog$create_join_maps$m__18382 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new datalog$create_join_maps$m__18382$fn__18383(object, object2), (Object)PersistentArrayMap.EMPTY, object3);
    }
}

