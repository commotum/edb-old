/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.log$write_excised_log$fn__16409$excise_QMARK___16416;
import datomic.log$write_excised_log$fn__16409$fn__16421;

public final class log$write_excised_log$fn__16409
extends AFunction {
    Object ts;
    Object lookup;
    Object xpreds;
    Object cs;
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"write-excise-val");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"fressianed-dir");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");

    public log$write_excised_log$fn__16409(Object object, Object object2, Object object3, Object object4) {
        this.ts = object;
        this.lookup = object2;
        this.xpreds = object3;
        this.cs = object4;
    }

    public Object invoke(Object m, Object p__16408) {
        Object object = p__16408;
        p__16408 = null;
        Object vec__16410 = object;
        Object dirid = RT.nth((Object)vec__16410, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16410;
        vec__16410 = null;
        Object segids = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        log$write_excised_log$fn__16409$excise_QMARK___16416 excise_QMARK_ = new log$write_excised_log$fn__16409$excise_QMARK___16416(this_.xpreds);
        Object dir = ((IFn)const__3.getRawRoot()).invoke(this_.lookup, dirid);
        log$write_excised_log$fn__16409$excise_QMARK___16416 log$write_excised_log$fn__16409$excise_QMARK___16416 = excise_QMARK_;
        excise_QMARK_ = null;
        Object object3 = segids;
        segids = null;
        Object object4 = m;
        m = null;
        Object object5 = dir;
        dir = null;
        Object vec__16413 = ((IFn)const__4.getRawRoot()).invoke((Object)new log$write_excised_log$fn__16409$fn__16421(this_.ts, (Object)log$write_excised_log$fn__16409$excise_QMARK___16416, this_.lookup, object3, this_.cs), (Object)Tuple.create((Object)object4, (Object)PersistentVector.EMPTY), object5);
        Object m2 = RT.nth((Object)vec__16413, (int)RT.intCast((long)0L), null);
        Object object6 = vec__16413;
        vec__16413 = null;
        Object newdir = RT.nth((Object)object6, (int)RT.intCast((long)1L), null);
        Object newdid = ((IFn)const__5.getRawRoot()).invoke();
        Object object7 = newdir;
        newdir = null;
        ((IFn)const__6.getRawRoot()).invoke(this_.cs, newdid, ((IFn)const__7.getRawRoot()).invoke(object7));
        Object object8 = m2;
        m2 = null;
        Object object9 = dirid;
        dirid = null;
        Object object10 = newdid;
        newdid = null;
        log$write_excised_log$fn__16409 this_ = null;
        return ((IFn)const__8.getRawRoot()).invoke(object8, object9, object10);
    }
}

