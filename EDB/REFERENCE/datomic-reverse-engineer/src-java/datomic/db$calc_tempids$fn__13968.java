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

public final class db$calc_tempids$fn__13968
extends AFunction {
    Object lid__GT_gid;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc!");

    public db$calc_tempids$fn__13968(Object object) {
        this.lid__GT_gid = object;
    }

    public Object invoke(Object m, Object p__13967) {
        Object object = p__13967;
        p__13967 = null;
        Object vec__13969 = object;
        Object lid = RT.nth((Object)vec__13969, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__13969;
        vec__13969 = null;
        Object eid = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = m;
        m = null;
        Object object4 = lid;
        Object object5 = lid;
        lid = null;
        Object object6 = eid;
        eid = null;
        db$calc_tempids$fn__13968 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3, RT.get((Object)this_.lid__GT_gid, (Object)object4, (Object)object5), object6);
    }
}

