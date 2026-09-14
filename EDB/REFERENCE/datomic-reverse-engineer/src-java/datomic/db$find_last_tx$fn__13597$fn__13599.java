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

public final class db$find_last_tx$fn__13597$fn__13599
extends AFunction {
    long eid;

    public db$find_last_tx$fn__13597$fn__13599(long l) {
        this.eid = l;
    }

    public Object invoke(Object p1__13596_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__13602 = Util.equiv((long)this_.eid, (long)((IDatum)p1__13596_SHARP_).getE());
        if (and__5236__auto__13602) {
            boolean and__5236__auto__13601 = Util.equiv((long)50L, (long)((IDatum)p1__13596_SHARP_).getA());
            if (and__5236__auto__13601) {
                Object object = p1__13596_SHARP_;
                p1__13596_SHARP_ = null;
                db$find_last_tx$fn__13597$fn__13599 this_ = null;
                bl = Util.equiv((long)this_.eid, (long)((IDatum)object).getTx()) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__13601 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__13602 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

