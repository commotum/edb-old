/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Util;
import datomic.impl.db.IDatum;

public final class db$find_raet$fn__13002
extends AFunction {
    Object rid;
    Object attrid;
    Object a;

    public db$find_raet$fn__13002(Object object, Object object2, Object object3) {
        this.rid = object;
        this.attrid = object2;
        this.a = object3;
    }

    public Object invoke(Object it) {
        Boolean bl;
        boolean and__5236__auto__13005 = Util.equiv((Object)this_.rid, (Object)((IDatum)it).getV());
        if (and__5236__auto__13005) {
            boolean or__5238__auto__13004 = Util.identical((Object)this_.a, null);
            if (or__5238__auto__13004) {
                bl = or__5238__auto__13004 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object = it;
                it = null;
                db$find_raet$fn__13002 this_ = null;
                bl = Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__13005 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

