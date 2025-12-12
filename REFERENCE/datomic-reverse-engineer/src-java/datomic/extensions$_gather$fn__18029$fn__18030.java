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

public final class extensions$_gather$fn__18029$fn__18030
extends AFunction {
    Object attrid;
    Object eid;

    public extensions$_gather$fn__18029$fn__18030(Object object, Object object2) {
        this.attrid = object;
        this.eid = object2;
    }

    public Object invoke(Object p1__18028_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__18032 = Util.equiv((Object)this_.eid, (long)((IDatum)p1__18028_SHARP_).getE());
        if (and__5236__auto__18032) {
            Object object = p1__18028_SHARP_;
            p1__18028_SHARP_ = null;
            extensions$_gather$fn__18029$fn__18030 this_ = null;
            bl = Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__18032 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

