/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;
import datomic.impl.db.IDatum;
import java.util.Map;

public final class db$create_composite$fn__13925
extends AFunction {
    long t;
    Object eaop_map;
    Object e;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"ea->v");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"->EAOpof");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"compare");

    public db$create_composite$fn__13925(long l, Object object, Object object2, Object object3) {
        this.t = l;
        this.eaop_map = object;
        this.e = object2;
        this.db = object3;
    }

    public Object invoke(Object attr) {
        Object object;
        Object tx_assert;
        Object object2 = attr;
        attr = null;
        Object aid = ((Database)this.db).entid(object2);
        Object db_v = ((IFn)const__0.getRawRoot()).invoke(this.db, this.e, aid);
        Object tx_retract = ((Map)this.eaop_map).get(((IFn)const__1.getRawRoot()).invoke(((IFn.LLOLO)const__2.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)this.e)), RT.uncheckedLongCast((Object)((Number)aid)), null, this.t)));
        Object object3 = aid;
        aid = null;
        Object v = tx_assert = ((Map)this.eaop_map).get(((IFn)const__1.getRawRoot()).invoke(((IFn.LLOLO)const__3.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)this.e)), RT.uncheckedLongCast((Object)((Number)object3)), null, this.t)));
        if (v != null && v != Boolean.FALSE) {
            Object v2 = tx_assert;
            tx_assert = null;
            object = ((IDatum)v2).getV();
        } else {
            Object object4;
            Object and__5236__auto__13927;
            Object v3 = and__5236__auto__13927 = tx_retract;
            if (v3 != null && v3 != Boolean.FALSE) {
                Object v4 = tx_retract;
                tx_retract = null;
                object4 = Numbers.isZero((long)((IFn.OOL)const__5.getRawRoot()).invokePrim(db_v, ((IDatum)v4).getV())) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object4 = and__5236__auto__13927;
                and__5236__auto__13927 = null;
            }
            if (object4 != null && object4 != Boolean.FALSE) {
                object = null;
            } else {
                object = db_v;
                Object var3_3 = null;
            }
        }
        return object;
    }
}

