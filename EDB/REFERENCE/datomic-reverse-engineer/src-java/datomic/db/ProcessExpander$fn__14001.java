/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.db;

import clojure.lang.AFunction;
import datomic.db.IProcess;
import java.util.Map;

public final class ProcessExpander$fn__14001
extends AFunction {
    Object local_tempids;

    public ProcessExpander$fn__14001(Object object) {
        this.local_tempids = object;
    }

    public Object invoke(Object p1__13997_SHARP_, Object p2__13998_SHARP_) {
        Object object = p1__13997_SHARP_;
        p1__13997_SHARP_ = null;
        Object object2 = p2__13998_SHARP_;
        p2__13998_SHARP_ = null;
        return ((IProcess)object).inject(object2, (Map)this.local_tempids);
    }
}

