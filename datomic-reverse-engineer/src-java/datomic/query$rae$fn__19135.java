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

public final class query$rae$fn__19135
extends AFunction {
    Object attrid;
    Object rid;

    public query$rae$fn__19135(Object object, Object object2) {
        this.attrid = object;
        this.rid = object2;
    }

    public Object invoke(Object p1__19132_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__19137 = Util.equiv((Object)this_.rid, (Object)((IDatum)p1__19132_SHARP_).getV());
        if (and__5236__auto__19137) {
            Object object = p1__19132_SHARP_;
            p1__19132_SHARP_ = null;
            query$rae$fn__19135 this_ = null;
            bl = Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__19137 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

