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

public final class db$ident_setting_datoms$fn__13576
extends AFunction {
    public Object invoke(Object p1__13572_SHARP_) {
        Object object = p1__13572_SHARP_;
        p1__13572_SHARP_ = null;
        db$ident_setting_datoms$fn__13576 this_ = null;
        return Util.equiv((long)10L, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

