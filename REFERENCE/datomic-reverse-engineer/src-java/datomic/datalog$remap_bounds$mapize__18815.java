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
import datomic.datalog$remap_bounds$mapize__18815$fn__18816;

public final class datalog$remap_bounds$mapize__18815
extends AFunction {
    Object ia;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map-indexed");

    public datalog$remap_bounds$mapize__18815(Object object) {
        this.ia = object;
    }

    public Object invoke(Object p1__18814_SHARP_) {
        Object object = p1__18814_SHARP_;
        p1__18814_SHARP_ = null;
        datalog$remap_bounds$mapize__18815 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new datalog$remap_bounds$mapize__18815$fn__18816(this_.ia), object));
    }
}

