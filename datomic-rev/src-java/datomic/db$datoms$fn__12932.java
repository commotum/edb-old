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

public final class db$datoms$fn__12932
extends AFunction {
    public Object invoke(Object p1__12911_SHARP_, Object p2__12912_SHARP_) {
        Object object = p1__12911_SHARP_;
        p1__12911_SHARP_ = null;
        Object object2 = p2__12912_SHARP_;
        p2__12912_SHARP_ = null;
        return ((IDb)object).seekAVET((IDatum)object2);
    }
}

