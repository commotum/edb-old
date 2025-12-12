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

public final class db$get_entity$fn__13016
extends AFunction {
    Object eid;

    public db$get_entity$fn__13016(Object object) {
        this.eid = object;
    }

    public Object invoke(Object p1__13013_SHARP_) {
        Object object = p1__13013_SHARP_;
        p1__13013_SHARP_ = null;
        db$get_entity$fn__13016 this_ = null;
        return Util.equiv((Object)this_.eid, (long)((IDatum)object).getE()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

