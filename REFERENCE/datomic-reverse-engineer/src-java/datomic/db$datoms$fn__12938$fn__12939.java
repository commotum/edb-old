/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$datoms$fn__12938$fn__12939
extends AFunction {
    Object t;
    Object attrid;
    Object eid;
    Object v;
    Object a;
    Object e;
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public db$datoms$fn__12938$fn__12939(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.t = object;
        this.attrid = object2;
        this.eid = object3;
        this.v = object4;
        this.a = object5;
        this.e = object6;
    }

    public Object invoke(Object p1__12915_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__12947;
        boolean or__5238__auto__12941 = Util.identical((Object)this_.e, null);
        boolean bl2 = and__5236__auto__12947 = or__5238__auto__12941 ? or__5238__auto__12941 : Util.equiv((Object)this_.eid, (long)((IDatum)p1__12915_SHARP_).getE());
        if (and__5236__auto__12947) {
            boolean and__5236__auto__12946;
            boolean or__5238__auto__12942 = Util.identical((Object)this_.a, null);
            boolean bl3 = and__5236__auto__12946 = or__5238__auto__12942 ? or__5238__auto__12942 : Util.equiv((Object)this_.attrid, (long)((IDatum)p1__12915_SHARP_).getA());
            if (and__5236__auto__12946) {
                boolean and__5236__auto__12945;
                boolean or__5238__auto__12943 = Util.identical((Object)this_.v, null);
                boolean bl4 = and__5236__auto__12945 = or__5238__auto__12943 ? or__5238__auto__12943 : Numbers.isZero((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(this_.v, ((IDatum)p1__12915_SHARP_).getV()));
                if (and__5236__auto__12945) {
                    boolean or__5238__auto__12944 = Util.identical((Object)this_.t, null);
                    if (or__5238__auto__12944) {
                        bl = or__5238__auto__12944 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        Object object = p1__12915_SHARP_;
                        p1__12915_SHARP_ = null;
                        db$datoms$fn__12938$fn__12939 this_ = null;
                        bl = Util.equiv((Object)this_.t, (long)((IDatum)object).getT()) ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__12945 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12946 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__12947 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

