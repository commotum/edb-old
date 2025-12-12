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
import datomic.integrity$aevt_dquark_seq$fn__22326$fn__22331;

public final class integrity$aevt_dquark_seq$fn__22326
extends AFunction {
    Object db;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"iter-seq");

    public integrity$aevt_dquark_seq$fn__22326(Object object) {
        this.db = object;
    }

    public Object invoke(Object p__22325) {
        Object object = p__22325;
        p__22325 = null;
        Object vec__22327 = object;
        Object index2 = RT.nth((Object)vec__22327, (int)RT.intCast((long)0L), null);
        Object object2 = vec__22327;
        vec__22327 = null;
        Object iter2 = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = index2;
        index2 = null;
        Object object4 = iter2;
        iter2 = null;
        integrity$aevt_dquark_seq$fn__22326 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke((Object)new integrity$aevt_dquark_seq$fn__22326$fn__22331(object3, this_.db), ((IFn)const__4.getRawRoot()).invoke(object4));
    }
}

