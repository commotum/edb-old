/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IProcess;
import java.util.Map;

public final class db$with_tx$inject_all__14099$fn__14100
extends AFunction {
    Object local_tempids;

    public db$with_tx$inject_all__14099$fn__14100(Object object) {
        this.local_tempids = object;
    }

    public Object invoke(Object p1__14097_SHARP_, Object p2__14098_SHARP_) {
        Object object = p1__14097_SHARP_;
        p1__14097_SHARP_ = null;
        Object object2 = p2__14098_SHARP_;
        p2__14098_SHARP_ = null;
        return ((IProcess)object).inject(object2, (Map)this.local_tempids);
    }
}

