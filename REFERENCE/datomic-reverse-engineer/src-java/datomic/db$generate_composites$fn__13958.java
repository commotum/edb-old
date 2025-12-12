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

public final class db$generate_composites$fn__13958
extends AFunction {
    Object eaop_map;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"create-composite");

    public db$generate_composites$fn__13958(Object object, Object object2) {
        this.eaop_map = object;
        this.db = object2;
    }

    public Object invoke(Object p1__13953_SHARP_) {
        Object object = p1__13953_SHARP_;
        p1__13953_SHARP_ = null;
        db$generate_composites$fn__13958 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.eaop_map, object);
    }
}

