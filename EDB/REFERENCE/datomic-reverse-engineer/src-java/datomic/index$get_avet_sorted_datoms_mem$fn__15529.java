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

public final class index$get_avet_sorted_datoms_mem$fn__15529
extends AFunction {
    Object attrid;

    public index$get_avet_sorted_datoms_mem$fn__15529(Object object) {
        this.attrid = object;
    }

    public Object invoke(Object p1__15522_SHARP_) {
        Object object = p1__15522_SHARP_;
        p1__15522_SHARP_ = null;
        index$get_avet_sorted_datoms_mem$fn__15529 this_ = null;
        return Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

