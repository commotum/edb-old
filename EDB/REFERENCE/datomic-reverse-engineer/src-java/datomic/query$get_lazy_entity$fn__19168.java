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

public final class query$get_lazy_entity$fn__19168
extends AFunction {
    Object eid;

    public query$get_lazy_entity$fn__19168(Object object) {
        this.eid = object;
    }

    public Object invoke(Object p1__19160_SHARP_) {
        Object object = p1__19160_SHARP_;
        p1__19160_SHARP_ = null;
        query$get_lazy_entity$fn__19168 this_ = null;
        return Util.equiv((Object)this_.eid, (long)((IDatum)object).getE()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

