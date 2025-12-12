/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Indexed
 *  datomic.query.support.MapOnIndexed
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IPersistentVector;
import clojure.lang.Indexed;
import datomic.query.support.MapOnIndexed;

public final class query$q_STAR_$fn__19523
extends AFunction {
    Object as;

    public query$q_STAR_$fn__19523(Object object) {
        this.as = object;
    }

    public Object invoke(Object p1__19517_SHARP_) {
        Object object = p1__19517_SHARP_;
        p1__19517_SHARP_ = null;
        return new MapOnIndexed((IPersistentVector)this.as, (Indexed)object);
    }
}

