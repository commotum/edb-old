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

public final class query$eav$fn__19148
extends AFunction {
    Object eid;
    Object attrid;

    public query$eav$fn__19148(Object object, Object object2) {
        this.eid = object;
        this.attrid = object2;
    }

    public Object invoke(Object p1__19145_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__19150 = Util.equiv((Object)this_.eid, (long)((IDatum)p1__19145_SHARP_).getE());
        if (and__5236__auto__19150) {
            Object object = p1__19145_SHARP_;
            p1__19145_SHARP_ = null;
            query$eav$fn__19148 this_ = null;
            bl = Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__19150 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

