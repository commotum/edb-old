/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.Db;
import datomic.impl.db.IDatum;

public final class db$get_ids$fn__13881
extends AFunction {
    Object db;
    Object genid;
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"tempid?");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"require-attr");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"find-avet");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"assoc-in");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__20 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__21 = RT.keyword((String)"db.error", (String)"invalid-entity-id");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__23 = RT.keyword(null, (String)"else");

    public db$get_ids$fn__13881(Object object, Object object2) {
        this.db = object;
        this.genid = object2;
    }

    public Object invoke(Object p__13880, Object d) {
        db$get_ids$fn__13881 this_;
        Object object;
        Object object2 = p__13880;
        p__13880 = null;
        Object vec__13882 = object2;
        Object m = RT.nth((Object)vec__13882, (int)RT.uncheckedIntCast((long)0L), null);
        Object p = RT.nth((Object)vec__13882, (int)RT.uncheckedIntCast((long)1L), null);
        Object t = RT.nth((Object)vec__13882, (int)RT.uncheckedIntCast((long)2L), null);
        Object u = RT.nth((Object)vec__13882, (int)RT.uncheckedIntCast((long)3L), null);
        Object z = RT.nth((Object)vec__13882, (int)RT.uncheckedIntCast((long)4L), null);
        Object object3 = vec__13882;
        vec__13882 = null;
        Object mpt = object3;
        long e = ((IDatum)d).getE();
        int a = ((IDatum)d).getA();
        Object object4 = ((IFn.LO)const__6.getRawRoot()).invokePrim(e);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object attr = ((IFn)const__7.getRawRoot()).invoke(this_.db, (Object)a);
            Object object5 = d;
            d = null;
            Object v = ((IDatum)object5).getV();
            Object object6 = attr;
            attr = null;
            if (Util.equiv((Object)((Attribute)object6).unique, (long)38L)) {
                Object temp__5455__auto__13890;
                Object object7 = temp__5455__auto__13890 = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(this_.db, (Object)a, v));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = temp__5455__auto__13890;
                    temp__5455__auto__13890 = null;
                    Object it = object8;
                    Object object9 = m;
                    m = null;
                    Object object10 = it;
                    it = null;
                    Object object11 = p;
                    p = null;
                    Object object12 = t;
                    t = null;
                    Object object13 = u;
                    u = null;
                    Object object14 = z;
                    z = null;
                    object = Tuple.create((Object)((IFn)const__12.getRawRoot()).invoke(object9, (Object)Numbers.num((long)e), (Object)Numbers.num((long)((IDatum)object10).getE())), (Object)object11, (Object)object12, (Object)object13, (Object)object14);
                } else {
                    Object temp__5455__auto__13889;
                    Object object15 = temp__5455__auto__13889 = ((IFn)const__13.getRawRoot()).invoke(u, (Object)Tuple.create((Object)a, (Object)v));
                    if (object15 != null && object15 != Boolean.FALSE) {
                        Object object16 = temp__5455__auto__13889;
                        temp__5455__auto__13889 = null;
                        Object ue = object16;
                        Object object17 = m;
                        m = null;
                        Object object18 = ue;
                        ue = null;
                        Object object19 = p;
                        p = null;
                        Object object20 = t;
                        t = null;
                        Object object21 = u;
                        u = null;
                        Object object22 = z;
                        z = null;
                        object = Tuple.create((Object)((IFn)const__12.getRawRoot()).invoke(object17, (Object)Numbers.num((long)e), object18), (Object)object19, (Object)object20, (Object)object21, (Object)object22);
                    } else {
                        Object object23 = mpt;
                        mpt = null;
                        Object vec__13885 = ((IFn)this_.genid).invoke((Object)Numbers.num((long)e), object23);
                        Object m2 = RT.nth((Object)vec__13885, (int)RT.uncheckedIntCast((long)0L), null);
                        Object p2 = RT.nth((Object)vec__13885, (int)RT.uncheckedIntCast((long)1L), null);
                        Object t2 = RT.nth((Object)vec__13885, (int)RT.uncheckedIntCast((long)2L), null);
                        Object object24 = vec__13885;
                        vec__13885 = null;
                        Object u2 = RT.nth((Object)object24, (int)RT.uncheckedIntCast((long)3L), null);
                        Object object25 = m2;
                        Object object26 = p2;
                        p2 = null;
                        Object object27 = t2;
                        t2 = null;
                        Object object28 = u2;
                        u2 = null;
                        Object object29 = v;
                        v = null;
                        Object object30 = m2;
                        m2 = null;
                        Object object31 = z;
                        z = null;
                        object = Tuple.create((Object)object25, (Object)object26, (Object)object27, (Object)((IFn)const__14.getRawRoot()).invoke(object28, (Object)Tuple.create((Object)a, (Object)object29), RT.get((Object)object30, (Object)Numbers.num((long)e))), (Object)object31);
                    }
                }
            } else {
                Object object32 = mpt;
                mpt = null;
                this_ = null;
                object = ((IFn)this_.genid).invoke((Object)Numbers.num((long)e), object32);
            }
        } else {
            boolean and__5236__auto__13891 = Numbers.gte((long)((IFn.LL)const__17.getRawRoot()).invokePrim(e), (long)((Db)this_.db).nextT());
            if (and__5236__auto__13891 ? Numbers.lte((long)1000L, (long)((Db)this_.db).nextT()) : and__5236__auto__13891) {
                this_ = null;
                object = ((IFn)const__20.getRawRoot()).invoke((Object)const__21, ((IFn)const__22.getRawRoot()).invoke((Object)"Invalid entity id: ", (Object)Numbers.num((long)e)));
            } else {
                Keyword keyword = const__23;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = mpt;
                    mpt = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }
}

