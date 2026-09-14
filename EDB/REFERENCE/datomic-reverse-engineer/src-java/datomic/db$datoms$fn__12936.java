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

public final class db$datoms$fn__12936
extends AFunction {
    public Object invoke(Object p1__12913_SHARP_, Object p2__12914_SHARP_) {
        Object object = p1__12913_SHARP_;
        p1__12913_SHARP_ = null;
        Object object2 = p2__12914_SHARP_;
        p2__12914_SHARP_ = null;
        return ((IDb)object).seekRAET((IDatum)object2);
    }
}

