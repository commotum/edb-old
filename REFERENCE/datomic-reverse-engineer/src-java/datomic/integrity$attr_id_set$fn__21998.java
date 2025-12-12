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

public final class integrity$attr_id_set$fn__21998
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"datomic.api", (String)"attribute");

    public integrity$attr_id_set$fn__21998(Object object) {
        this.db = object;
    }

    public Object invoke(Object p__21997) {
        Object e;
        Object vec__21999;
        Object object = p__21997;
        p__21997 = null;
        Object object2 = vec__21999 = object;
        vec__21999 = null;
        Object object3 = e = RT.nth((Object)object2, (int)RT.intCast((long)0L), null);
        e = null;
        integrity$attr_id_set$fn__21998 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(this_.db, object3);
    }
}

