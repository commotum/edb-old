/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$OLO
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
import datomic.db.Db;
import datomic.db.GetPartition;

public final class db$get_ids$genid__13875
extends AFunction {
    Object db;
    Object default_part_ref;
    Object part_reqs;
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"valid-partbits?");
    public static final Keyword const__15 = RT.keyword(null, (String)"else");
    public static final Var const__16 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__17 = RT.keyword((String)"db.error", (String)"not-a-partition");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"str");

    public db$get_ids$genid__13875(Object object, Object object2, Object object3) {
        this.db = object;
        this.default_part_ref = object2;
        this.part_reqs = object3;
    }

    public Object invoke(Object e, Object p__13874) {
        Object object;
        Object object2 = p__13874;
        p__13874 = null;
        Object vec__13876 = object2;
        Object m = RT.nth((Object)vec__13876, (int)RT.uncheckedIntCast((long)0L), null);
        Object p = RT.nth((Object)vec__13876, (int)RT.uncheckedIntCast((long)1L), null);
        Object t = RT.nth((Object)vec__13876, (int)RT.uncheckedIntCast((long)2L), null);
        Object u = RT.nth((Object)vec__13876, (int)RT.uncheckedIntCast((long)3L), null);
        Object z = RT.nth((Object)vec__13876, (int)RT.uncheckedIntCast((long)4L), null);
        Object object3 = vec__13876;
        vec__13876 = null;
        Object mpt = object3;
        Object object4 = ((IFn)const__6.getRawRoot()).invoke(m, e);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = mpt;
            mpt = null;
        } else {
            Object object5;
            Object part2 = ((GetPartition)this_.part_reqs).getPart(e);
            if (Util.equiv((Object)part2, (long)16L)) {
                object5 = ((IFn)const__9.getRawRoot()).invoke(this_.default_part_ref);
            } else {
                object5 = part2;
                part2 = null;
            }
            Object part3 = object5;
            if (Numbers.isZero((Object)part3)) {
                Object object6 = m;
                m = null;
                Object object7 = e;
                e = null;
                Object object8 = part3;
                part3 = null;
                Object object9 = ((IFn)const__11.getRawRoot()).invoke(object6, object7, (Object)Numbers.num((long)((IFn.LLL)const__12.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object8)), RT.uncheckedLongCast((Object)((Number)p)))));
                Object object10 = p;
                p = null;
                Object object11 = t;
                t = null;
                Object object12 = u;
                u = null;
                object = Tuple.create((Object)object9, (Object)Numbers.unchecked_inc((Object)object10), (Object)object11, (Object)object12, (Object)Boolean.TRUE);
            } else if (Util.equiv((long)3L, (Object)part3)) {
                Object object13 = m;
                m = null;
                Object object14 = e;
                e = null;
                Object object15 = part3;
                part3 = null;
                Object object16 = p;
                p = null;
                Object object17 = t;
                t = null;
                Object object18 = u;
                u = null;
                Object object19 = z;
                z = null;
                object = Tuple.create((Object)((IFn)const__11.getRawRoot()).invoke(object13, object14, (Object)Numbers.num((long)((IFn.LLL)const__12.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object15)), ((Db)this_.db).nextT()))), (Object)object16, (Object)object17, (Object)object18, (Object)object19);
            } else {
                Object object20 = ((IFn.OLO)const__14.getRawRoot()).invokePrim(this_.db, RT.uncheckedLongCast((Object)((Number)part3)));
                if (object20 != null && object20 != Boolean.FALSE) {
                    Object object21 = m;
                    m = null;
                    Object object22 = e;
                    e = null;
                    Object object23 = part3;
                    part3 = null;
                    Object object24 = ((IFn)const__11.getRawRoot()).invoke(object21, object22, (Object)Numbers.num((long)((IFn.LLL)const__12.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object23)), RT.uncheckedLongCast((Object)((Number)t)))));
                    Object object25 = p;
                    p = null;
                    Object object26 = t;
                    t = null;
                    Object object27 = u;
                    u = null;
                    Object object28 = z;
                    z = null;
                    object = Tuple.create((Object)object24, (Object)object25, (Object)Numbers.unchecked_inc((Object)object26), (Object)object27, (Object)object28);
                } else {
                    Keyword keyword = const__15;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object29 = e;
                        e = null;
                        db$get_ids$genid__13875 this_ = null;
                        object = ((IFn)const__16.getRawRoot()).invoke((Object)const__17, ((IFn)const__18.getRawRoot()).invoke((Object)"Entity id ", object29, (Object)" is not in a valid partition"));
                    } else {
                        object = null;
                    }
                }
            }
        }
        return object;
    }
}

