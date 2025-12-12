/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.impl.db.IDatum;

public final class fulltext$find_matching_assertion$fn__14659
extends AFunction {
    public Object invoke(Object p1__14658_SHARP_) {
        Object object = p1__14658_SHARP_;
        p1__14658_SHARP_ = null;
        return ((IDatum)object).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
    }
}

