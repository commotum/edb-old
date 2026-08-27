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
import datomic.integrity$eavt_dquark_seq$fn__22346$fn__22351;

public final class integrity$eavt_dquark_seq$fn__22346
extends AFunction {
    Object db;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"iter-seq");

    public integrity$eavt_dquark_seq$fn__22346(Object object) {
        this.db = object;
    }

    public Object invoke(Object p__22345) {
        Object object = p__22345;
        p__22345 = null;
        Object vec__22347 = object;
        Object index2 = RT.nth((Object)vec__22347, (int)RT.intCast((long)0L), null);
        Object object2 = vec__22347;
        vec__22347 = null;
        Object iter2 = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = index2;
        index2 = null;
        Object object4 = iter2;
        iter2 = null;
        integrity$eavt_dquark_seq$fn__22346 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke((Object)new integrity$eavt_dquark_seq$fn__22346$fn__22351(this_.db, object3), ((IFn)const__4.getRawRoot()).invoke(object4));
    }
}

