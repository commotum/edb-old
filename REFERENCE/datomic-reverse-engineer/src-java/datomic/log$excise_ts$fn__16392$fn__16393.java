/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import datomic.impl.db.IDatum;

public final class log$excise_ts$fn__16392$fn__16393
extends AFunction {
    public Object invoke(Object p1__16391_SHARP_) {
        Object object = p1__16391_SHARP_;
        p1__16391_SHARP_ = null;
        return Numbers.num((long)((IDatum)object).getT());
    }
}

