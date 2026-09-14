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

public final class db$add_avet$fn__13199
extends AFunction {
    Object aid;

    public db$add_avet$fn__13199(Object object) {
        this.aid = object;
    }

    public Object invoke(Object p1__13198_SHARP_) {
        Object object = p1__13198_SHARP_;
        p1__13198_SHARP_ = null;
        db$add_avet$fn__13199 this_ = null;
        return Util.equiv((Object)this_.aid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

