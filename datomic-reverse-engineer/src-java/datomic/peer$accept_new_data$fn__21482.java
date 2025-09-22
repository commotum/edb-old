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

public final class peer$accept_new_data$fn__21482
extends AFunction {
    long nextT;

    public peer$accept_new_data$fn__21482(long l) {
        this.nextT = l;
    }

    public Object invoke(Object p1__21481_SHARP_) {
        Object object = p1__21481_SHARP_;
        p1__21481_SHARP_ = null;
        peer$accept_new_data$fn__21482 this_ = null;
        return Numbers.lt((long)((IDatum)object).getT(), (long)this_.nextT) ? Boolean.TRUE : Boolean.FALSE;
    }
}

