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

public final class db$find_eavt$fn__12986
extends AFunction {
    Object attrid;
    Object a;
    Object v;
    Object e;
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public db$find_eavt$fn__12986(Object object, Object object2, Object object3, Object object4) {
        this.attrid = object;
        this.a = object2;
        this.v = object3;
        this.e = object4;
    }

    public Object invoke(Object it) {
        Boolean bl;
        boolean and__5236__auto__12992;
        boolean or__5238__auto__12988 = Util.identical((Object)this_.e, null);
        boolean bl2 = and__5236__auto__12992 = or__5238__auto__12988 ? or__5238__auto__12988 : Util.equiv((Object)this_.e, (long)((IDatum)it).getE());
        if (and__5236__auto__12992) {
            boolean and__5236__auto__12991;
            boolean or__5238__auto__12989 = Util.identical((Object)this_.a, null);
            boolean bl3 = and__5236__auto__12991 = or__5238__auto__12989 ? or__5238__auto__12989 : Util.equiv((Object)this_.attrid, (long)((IDatum)it).getA());
            if (and__5236__auto__12991) {
                boolean or__5238__auto__12990 = Util.identical((Object)this_.v, null);
                if (or__5238__auto__12990) {
                    bl = or__5238__auto__12990 ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    Object object = it;
                    it = null;
                    db$find_eavt$fn__12986 this_ = null;
                    bl = Numbers.isZero((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(this_.v, ((IDatum)object).getV())) ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12991 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__12992 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

