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
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import datomic.pull$ea__GT_v$mk_iter__18919$fn__18920;
import datomic.pull$ea__GT_v$mk_iter__18919$fn__18922;

public final class pull$ea__GT_v$mk_iter__18919
extends AFunction {
    Object db;
    Object attrid;
    Object eid;
    Object use_aevt_QMARK_;
    Object d;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"map");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"windowed");

    public pull$ea__GT_v$mk_iter__18919(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.db = object;
        this.attrid = object2;
        this.eid = object3;
        this.use_aevt_QMARK_ = object4;
        this.d = object5;
    }

    public Object invoke() {
        Object object = this_.use_aevt_QMARK_;
        pull$ea__GT_v$mk_iter__18919 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new pull$ea__GT_v$mk_iter__18919$fn__18920(), ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)new pull$ea__GT_v$mk_iter__18919$fn__18922(this_.attrid, this_.eid), (Object)(object != null && object != Boolean.FALSE ? ((IDb)this_.db).seekAEVT((IDatum)this_.d) : ((IDb)this_.db).seekEAVT((IDatum)this_.d))));
    }
}

