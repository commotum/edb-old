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

public final class db$scan_aevt$fn__13258
extends AFunction {
    Object attrid;

    public db$scan_aevt$fn__13258(Object object) {
        this.attrid = object;
    }

    public Object invoke(Object p1__13257_SHARP_) {
        Object object = p1__13257_SHARP_;
        p1__13257_SHARP_ = null;
        db$scan_aevt$fn__13258 this_ = null;
        return Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

