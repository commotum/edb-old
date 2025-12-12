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

public final class integrity$unfindable_datom_seq$fn__22030
extends AFunction {
    public Object invoke(Object p1__22021_SHARP_, Object p2__22022_SHARP_) {
        Object object = p1__22021_SHARP_;
        p1__22021_SHARP_ = null;
        Object object2 = p2__22022_SHARP_;
        p2__22022_SHARP_ = null;
        return ((IDb)object).seekRAET((IDatum)object2);
    }
}

