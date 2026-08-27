/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class integrity$unfindable_datom_seq$fn__22026
extends AFunction {
    public Object invoke(Object p1__22019_SHARP_, Object p2__22020_SHARP_) {
        Object object = p1__22019_SHARP_;
        p1__22019_SHARP_ = null;
        Object object2 = p2__22020_SHARP_;
        p2__22020_SHARP_ = null;
        return ((IDb)object).seekAVET((IDatum)object2);
    }
}

